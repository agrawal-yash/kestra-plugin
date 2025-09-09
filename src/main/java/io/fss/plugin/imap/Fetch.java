package io.fss.plugin.imap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Fetch emails from IMAP server",
    description = "Connects to an IMAP server and fetches unread emails."
)
@Plugin(
    examples = {
        @io.kestra.core.models.annotations.Example(
            title = "Fetch emails from Gmail",
            code = {
                "host: imap.gmail.com",
                "port: 993",
                "username: 22102074.yash@gmail.com", 
                "password: wldiapvwdokdedqn",
                "folder: INBOX",
                "maxEmails: 10",
                "markAsRead: false"
            }
        )
    }
)
public class Fetch extends Task implements RunnableTask<Fetch.Output> {
    
    @Schema(
        title = "IMAP server host",
        description = "The IMAP server hostname (e.g., imap.gmail.com)"
    )
    @PluginProperty(dynamic = true)
    private String host;

    @Schema(
        title = "IMAP server port",
        description = "The IMAP server port (typically 993 for SSL, 143 for non-SSL)"
    )
    @PluginProperty(dynamic = true)
    @Builder.Default
    private Integer port = 993;

    @Schema(
        title = "Username",
        description = "The email username for authentication"
    )
    @PluginProperty(dynamic = true)
    private String username;

    @Schema(
        title = "Password",
        description = "The email password for authentication"
    )
    @PluginProperty(dynamic = true)
    private String password;

    @Schema(
        title = "Folder name",
        description = "The email folder to fetch from"
    )
    @PluginProperty(dynamic = true)
    @Builder.Default
    private String folder = "INBOX";

    @Schema(
        title = "Maximum emails to fetch",
        description = "Maximum number of emails to fetch in one run"
    )
    @PluginProperty(dynamic = true)
    @Builder.Default
    private Integer maxEmails = 10;

    @Schema(
        title = "Mark emails as read",
        description = "Whether to mark fetched emails as read"
    )
    @PluginProperty(dynamic = true)
    @Builder.Default
    private Boolean markAsRead = false;

    @Schema(
        title = "Use SSL",
        description = "Whether to use SSL/TLS connection"
    )
    @PluginProperty(dynamic = true)
    @Builder.Default
    private Boolean useSsl = true;

    @Override
    public Fetch.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        // Render dynamic properties
        String renderedHost = runContext.render(host);
        String renderedUsername = runContext.render(username);
        String renderedPassword = runContext.render(password);
        String renderedFolder = runContext.render(folder);

        logger.info("Connecting to IMAP server: {}:{}", renderedHost, port);

        List<Email> fetchedEmails = new ArrayList<>();
        int processedCount = 0;

        // Set up mail session properties
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", renderedHost);
        props.setProperty("mail.imaps.port", String.valueOf(port));
        
        if (useSsl) {
            props.setProperty("mail.imaps.ssl.enable", "true");
            props.setProperty("mail.imaps.ssl.trust", "*");
        }

        Session session = Session.getInstance(props);
        Store store = null;
        Folder emailFolder = null;

        try {
            // Connect to the store
            store = session.getStore("imaps");
            store.connect(renderedHost, renderedUsername, renderedPassword);
            
            logger.info("Successfully connected to IMAP server");

            // Open folder
            emailFolder = store.getFolder(renderedFolder);
            emailFolder.open(markAsRead ? Folder.READ_WRITE : Folder.READ_ONLY);
            
            logger.info("Opened folder: {}", renderedFolder);

            // Search for unread messages
            Message[] messages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            logger.info("Found {} unread emails", messages.length);

            // Process messages (latest first)
            int messagesToProcess = Math.min(messages.length, maxEmails);
            for (int i = messages.length - 1; i >= messages.length - messagesToProcess; i--) {
                try {
                    Message message = messages[i];
                    Email email = processMessage(message, logger);
                    
                    if (email != null) {
                        fetchedEmails.add(email);
                        processedCount++;
                        
                        // Mark as read if requested
                        if (markAsRead) {
                            message.setFlag(Flags.Flag.SEEN, true);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing message {}: {}", i, e.getMessage());
                }
            }

        } finally {
            // Clean up connections
            if (emailFolder != null && emailFolder.isOpen()) {
                emailFolder.close(false);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        }

        logger.info("Email processing completed. Processed: {}", processedCount);

        return Output.builder()
            .emails(fetchedEmails)
            .totalEmails(fetchedEmails.size())
            .processedCount(processedCount)
            .build();
    }

    private Email processMessage(Message message, Logger logger) throws Exception {
        try {
            Email email = new Email();
            
            // Extract basic information
            email.setMessageId(message.getHeader("Message-ID") != null ? 
                              message.getHeader("Message-ID")[0] : "unknown");
            email.setSubject(message.getSubject() != null ? message.getSubject() : "No Subject");
            
            // Extract sender information
            Address[] fromAddresses = message.getFrom();
            if (fromAddresses != null && fromAddresses.length > 0) {
                String sender = fromAddresses[0].toString();
                email.setSender(sender);
                email.setSenderEmail(extractEmailAddress(sender));
            }
            
            // Extract body
            email.setBody(getTextContent(message));
            
            // Set received date
            Date receivedDate = message.getReceivedDate();
            if (receivedDate != null) {
                email.setReceivedDate(receivedDate.toInstant());
            }
            
            email.setRead(message.isSet(Flags.Flag.SEEN));
            
            // Process attachments
            email.setAttachments(extractAttachments(message));
            
            logger.info("Processing email from: {} - Subject: {}", 
                       email.getSenderEmail(), email.getSubject());
                    
            
            return email;
            
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage());
            return null;
        }
    }

    private String extractEmailAddress(String sender) {
        Pattern pattern = Pattern.compile("<([^>]+)>");
        Matcher matcher = pattern.matcher(sender);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return sender.trim();
    }

    private String getTextContent(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent().toString());
            } else if (bodyPart.isMimeType("text/html")) {
                String html = bodyPart.getContent().toString();
                result.append(html);
            } else if (bodyPart.isMimeType("multipart/*")) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private List<String> extractAttachments(Message message) throws Exception {
        List<String> attachments = new ArrayList<>();
        if (message.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    attachments.add(bodyPart.getFileName());
                }
            }
        }
        return attachments;
    }


    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "List of fetched emails"
        )
        private final List<Email> emails;
        
        @Schema(
            title = "Total number of emails fetched"
        )
        private final Integer totalEmails;
        
        @Schema(
            title = "Number of emails successfully processed"
        )
        private final Integer processedCount;
    
    }
}