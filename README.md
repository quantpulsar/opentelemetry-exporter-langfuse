# OpenTelemetry Langfuse Exporter

[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5+-green.svg)](https://spring.io/projects/spring-boot)
[![OpenTelemetry](https://img.shields.io/badge/OpenTelemetry-2.17+-purple.svg)](https://opentelemetry.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A Spring Boot auto-configured [OpenTelemetry](https://opentelemetry.io/) span exporter for [Langfuse](https://langfuse.com/) - the open-source LLM engineering platform.

## Overview

This module provides `LangfuseSpanExporter`, an implementation of the `io.opentelemetry.sdk.trace.export.SpanExporter` interface that sends traces to Langfuse via the OTLP HTTP protocol.

**Key Features:**
- Spring Boot auto-configuration with zero-code setup
- Full OpenTelemetry SDK setup (TracerProvider, W3C propagation)
- HTTP Basic authentication with Langfuse API keys
- Automatic span enrichment with Langfuse observation types
- Embabel-only filtering mode to reduce noise from non-LLM spans
- Compatible with Spring AI, Embabel Agent, and other OpenTelemetry instrumented libraries

## Installation

### Maven

```xml
<dependency>
    <groupId>com.quantpulsar</groupId>
    <artifactId>opentelemetry-exporter-langfuse</artifactId>
    <version>0.4.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.quantpulsar:opentelemetry-exporter-langfuse:0.4.0'
```

### With Embabel Agent

If using [Embabel Agent](https://github.com/embabel/embabel-agent), add both dependencies:

```xml
<dependency>
    <groupId>com.embabel.agent</groupId>
    <artifactId>embabel-agent-starter-observability</artifactId>
    <version>0.3.4-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.quantpulsar</groupId>
    <artifactId>opentelemetry-exporter-langfuse</artifactId>
    <version>0.4.0</version>
</dependency>
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
| `management.langfuse.embabel-only` | `false` | When `true`, only export spans with Embabel or GenAI attributes |

### Embabel-Only Mode

When `embabel-only` is enabled, the exporter filters out non-relevant spans (e.g., HTTP server spans, health checks, actuator endpoints) and only exports spans that carry Embabel or GenAI attributes. This reduces noise in your Langfuse dashboard.

```yaml
management:
  langfuse:
    embabel-only: true
```

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

The exporter automatically enriches spans with the `langfuse.observation.type` attribute. Detection follows a priority order:

### 1. Embabel Event Type (`embabel.event.type`)

If the span carries an `embabel.event.type` attribute, it is mapped first:

| Event Type | Langfuse Type |
| ---------- | ------------- |
| `agent_process` | `agent` |
| `action` | `chain` |
| `tool_call` | `tool` |
| `embedding` | `embedding` |
| `retriever` | `retriever` |
| `evaluator` | `evaluator` |
| `guardrail` | `guardrail` |
| `planning`, `goal_achieved`, `planning_ready`, `plan_formulated`, `replanning`, `state_transition`, `lifecycle_waiting`, `lifecycle_paused`, `lifecycle_stuck`, `object_added`, `object_bound` | `event` |

### 2. Individual Embabel Attributes

If no event type is present, individual attributes are checked:

| Source Attribute | Langfuse Type |
| ---------------- | ------------- |
| `embabel.agent.name` | `agent` |
| `embabel.tool.name` | `tool` |
| `embabel.action.short_name` | `chain` |
| `embabel.embedding.name` | `embedding` |
| `embabel.retriever.name` | `retriever` |
| `embabel.evaluator.name` | `evaluator` |
| `embabel.guardrail.name` | `guardrail` |
| `embabel.goal.short_name` | `event` |
| `embabel.state.to` | `event` |
| `embabel.lifecycle.state` | `event` |

### 3. GenAI Attributes

| Condition | Langfuse Type |
| --------- | ------------- |
| `gen_ai.operation.name = chat` | `generation` |

### 4. Default

All other spans are tagged as `span`.

## Auto-Configuration

The exporter auto-configures the following Spring beans:

- **`langfuseSpanExporter`** (`SpanExporter`): OTLP HTTP exporter wrapped with Langfuse enrichment logic
- **`openTelemetry`** (`OpenTelemetry`): Full SDK with `SdkTracerProvider`, `BatchSpanProcessor`, and W3C trace context propagation (created only if no other `OpenTelemetry` bean exists)

No code required. Simply add the dependency and configure the properties.

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
- [Embabel Agent](https://github.com/embabel/embabel-agent)

## License

Apache License 2.0
