<p align="center">
  <a href="https://www.kestra.io">
    <img src="https://kestra.io/banner.png"  alt="Kestra workflow orchestrator" />
  </a>
</p>

<h1 align="center" style="border-bottom: none">
    Event-Driven Declarative Orchestrator
</h1>

<div align="center">
 <a href="https://github.com/kestra-io/kestra/releases"><img src="https://img.shields.io/github/tag-pre/kestra-io/kestra.svg?color=blueviolet" alt="Last Version" /></a>
  <a href="https://github.com/kestra-io/kestra/blob/develop/LICENSE"><img src="https://img.shields.io/github/license/kestra-io/kestra?color=blueviolet" alt="License" /></a>
  <a href="https://github.com/kestra-io/kestra/stargazers"><img src="https://img.shields.io/github/stars/kestra-io/kestra?color=blueviolet&logo=github" alt="Github star" /></a> <br>
<a href="https://kestra.io"><img src="https://img.shields.io/badge/Website-kestra.io-192A4E?color=blueviolet" alt="Kestra infinitely scalable orchestration and scheduling platform"></a>
<a href="https://kestra.io/slack"><img src="https://img.shields.io/badge/Slack-Join%20Community-blueviolet?logo=slack" alt="Slack"></a>
</div>

<br />

<p align="center">
    <a href="https://twitter.com/kestra_io"><img height="25" src="https://kestra.io/twitter.svg" alt="twitter" /></a> &nbsp;
    <a href="https://www.linkedin.com/company/kestra/"><img height="25" src="https://kestra.io/linkedin.svg" alt="linkedin" /></a> &nbsp;
<a href="https://www.youtube.com/@kestra-io"><img height="25" src="https://kestra.io/youtube.svg" alt="youtube" /></a> &nbsp;
</p>

<br />
<p align="center">
    <a href="https://go.kestra.io/video/product-overview" target="_blank">
        <img src="https://kestra.io/startvideo.png" alt="Get started in 4 minutes with Kestra" width="640px" />
    </a>
</p>
<p align="center" style="color:grey;"><i>Get started with Kestra in 4 minutes.</i></p>


# Kestra Plugin Template

> A template for creating Kestra plugins

This repository serves as a general template for creating a new [Kestra](https://github.com/kestra-io/kestra) plugin. It should take only a few minutes! Use this repository as a scaffold to ensure that you've set up the plugin correctly, including unit tests and CI/CD workflows.

![Kestra orchestrator](https://kestra.io/video.gif)

## Running the project in local
### Prerequisites
- Java 21
- Docker

### Running tests
```
./gradlew check --parallel
```

### Development

`VSCode`:

Follow the README.md within the `.devcontainer` folder for a quick and easy way to get up and running with developing plugins if you are using VSCode.

`Other IDEs`:

```
./gradlew shadowJar && docker build -t kestra-custom . && docker run --rm -p 8080:8080 kestra-custom server local
```
> [!NOTE]
> You need to relaunch this whole command everytime you make a change to your plugin

go to http://localhost:8080, your plugin will be available to use

## Documentation
* Full documentation can be found under: [kestra.io/docs](https://kestra.io/docs)
* Documentation for developing a plugin is included in the [Plugin Developer Guide](https://kestra.io/docs/plugin-developer-guide/)


## License
Apache 2.0 © [Kestra Technologies](https://kestra.io)


## Stay up to date

We release new versions every month. Give the [main repository](https://github.com/kestra-io/kestra) a star to stay up to date with the latest releases and get notified about future updates.

![Star the repo](https://kestra.io/star.gif)

# Kestra FSS IMAP Plugin

A custom Kestra plugin for FSS IMAP operations.

## Installation in Kestra (Docker)

### Step 1: Create GitHub Token
1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token with `read:packages` scope
3. Copy the token

### Step 2: Configure Kestra

Add this to your Kestra `application.yml` or `docker-compose.yml`:

```yaml
# If using docker-compose.yml environment variables
environment:
  GITHUB_USERNAME: "agrawal-yash"
  GITHUB_TOKEN: "your_github_token_here"

# If using application.yml
kestra:
  plugins:
    repositories:
      github:
        url: https://maven.pkg.github.com/agrawal-yash/plugin-fss-imap
        username: ${GITHUB_USERNAME:agrawal-yash}
        password: ${GITHUB_TOKEN}
    configurations:
      - type: maven
        repositories:
          - github
        dependencies:
          - group: io.github.agrawal-yash
            name: plugin-fss-imap
            version: 1.0.0
```

### Step 3: Docker Compose Example

```yaml
version: "3.8"
services:
  kestra:
    image: kestra/kestra:latest
    environment:
      GITHUB_USERNAME: "agrawal-yash"
      GITHUB_TOKEN: "your_github_token_here"
    volumes:
      - ./application.yml:/app/application.yml
    ports:
      - "8080:8080"
```

## Development and Publishing

### Building Locally
```bash
./gradlew build
```

### Publishing to GitHub Packages
```bash
./gradlew publish
```

## Usage Example

```yaml
id: imap-example
namespace: dev

tasks:
  - id: imap-task
    type: io.github.agrawal_yash.imap.YourTaskClass
    # Add your task properties here
```