package io.fss.plugin.imap;

import java.time.Instant;
import java.util.*;
import lombok.*;

@Data
public class Email {
    private String messageId;
    private String subject;
    private String sender;
    private String senderEmail;
    private String body;
    private Instant receivedDate;
    private boolean isRead;
    private List<String> attachments;
}