# OpenTelemetry Langfuse Exporter

[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5+-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A Spring Boot auto-configured [OpenTelemetry](https://opentelemetry.io/) span exporter for [Langfuse](https://langfuse.com/) - the open-source LLM engineering platform.

## Overview

This module provides `LangfuseSpanExporter`, an implementation of the `io.opentelemetry.sdk.trace.export.SpanExporter` interface that sends traces to Langfuse via the OTLP HTTP protocol.

**Key Features:**
- Spring Boot auto-configuration with zero-code setup
- HTTP Basic authentication with Langfuse API keys
- Automatic span enrichment with Langfuse observation types
- Compatible with Spring AI and other OpenTelemetry instrumented libraries

## Installation

This exporter is designed to work with [embabel-agent-observability](https://github.com/embabel/embabel-agent). Add both dependencies to your project:

### Maven

```xml
<dependency>
    <groupId>com.embabel.agent</groupId>
    <artifactId>embabel-agent-observability</artifactId>
    <version>0.3.2-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.quantpulsar</groupId>
    <artifactId>opentelemetry-exporter-langfuse</artifactId>
    <version>0.3.2-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.embabel.agent:embabel-agent-observability:0.3.2-SNAPSHOT'
implementation 'com.quantpulsar:opentelemetry-exporter-langfuse:0.3.2-SNAPSHOT'
```

## Configuration

Add the following properties to your `application.yml`:

```yaml
management:
  langfuse:
    enabled: true
    endpoint: https://cloud.langfuse.com/api/public/otel
    public-key: ${LANGFUSE_PUBLIC_KEY}
    secret-key: ${LANGFUSE_SECRET_KEY}
```

### Configuration Properties

| Property | Default | Description |
| -------- | ------- | ----------- |
| `management.langfuse.enabled` | `true` | Enable/disable the exporter |
| `management.langfuse.endpoint` | `https://cloud.langfuse.com/api/public/otel` | Langfuse OTLP endpoint |
| `management.langfuse.public-key` | - | Langfuse public key (required) |
| `management.langfuse.secret-key` | - | Langfuse secret key (required) |
| `management.langfuse.service-name` | `embabel-agent` | Service name for traces |
| `management.langfuse.connect-timeout-ms` | `10000` | Connection timeout (ms) |
| `management.langfuse.export-timeout-ms` | `30000` | Export timeout (ms) |

### Langfuse Endpoints

| Environment | Endpoint |
| ----------- | -------- |
| Cloud (EU) | `https://cloud.langfuse.com/api/public/otel` |
| Cloud (US) | `https://us.cloud.langfuse.com/api/public/otel` |
| Self-hosted | `http://localhost:3000/api/public/otel` |

> **Note:** The `/v1/traces` suffix is automatically appended to the endpoint.

## Authentication

Langfuse uses HTTP Basic Authentication:
- **Username**: Public key (`pk-lf-...`)
- **Password**: Secret key (`sk-lf-...`)

The exporter handles Base64 encoding of credentials automatically.

## Span Enrichment

The exporter automatically enriches spans with `langfuse.observation.type` attribute based on span attributes:

| Source Attribute | Langfuse Type |
| ---------------- | ------------- |
| `embabel.agent.name` | `agent` |
| `embabel.tool.name` | `tool` |
| `embabel.action.short_name` | `chain` |
| `embabel.embedding.name` | `embedding` |
| `embabel.retriever.name` | `retriever` |
| `gen_ai.operation.name=chat` | `generation` |
| Default | `span` |

## Usage

No code required. Simply add the dependency and configure the properties. The exporter is auto-configured by Spring Boot and will automatically export spans to Langfuse.

## Requirements

- Java 21+
- Spring Boot 3.5+
- Langfuse v3.22.0+ (for self-hosted)

## Building

```bash
# Build
mvn clean package

# Build without tests
mvn clean package -DskipTests

# Run tests
mvn test
```

## Related Projects

- [OpenTelemetry Java SDK](https://github.com/open-telemetry/opentelemetry-java)
- [Langfuse Documentation](https://langfuse.com/docs)
- [Langfuse OpenTelemetry Integration](https://langfuse.com/docs/integrations/opentelemetry)

## License

Apache License 2.0
