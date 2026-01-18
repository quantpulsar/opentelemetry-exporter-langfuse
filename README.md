# OpenTelemetry Exporter Langfuse

OpenTelemetry OTLP exporter for [Langfuse](https://langfuse.com), designed to work with [embabel-agent-observability](https://github.com/embabel/embabel-agent-observability).

## Features

- **Complete OpenTelemetry SDK**: Provides a fully configured `OpenTelemetry` bean
- **OTLP HTTP export**: Uses OpenTelemetry's OTLP HTTP exporter for Langfuse
- **Automatic authentication**: HTTP Basic Auth with public/secret keys
- **Spring Boot auto-configuration**: Zero-code setup with configuration properties
- **Works with any Spring AI/Embabel tracing**: No ordering issues with core observability

## Quick Start

### 1. Add Dependencies

```xml
<dependency>
    <groupId>com.embabel.agent</groupId>
    <artifactId>embabel-agent-observability</artifactId>
    <version>0.3.2-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.embabel.agent</groupId>
    <artifactId>opentelemetry-exporter-langfuse</artifactId>
    <version>0.3.2-SNAPSHOT</version>
</dependency>
```

### 2. Configure

```yaml
management:
  langfuse:
    enabled: true
    endpoint: https://cloud.langfuse.com/api/public/otel
    public-key: pk-lf-your-public-key
    secret-key: sk-lf-your-secret-key
    service-name: my-agent-app
```

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `management.langfuse.enabled` | `true` | Enable/disable Langfuse exporter |
| `management.langfuse.endpoint` | `https://cloud.langfuse.com/api/public/otel` | Langfuse OTLP endpoint (base URL) |
| `management.langfuse.public-key` | - | Langfuse public key (required) |
| `management.langfuse.secret-key` | - | Langfuse secret key (required) |
| `management.langfuse.service-name` | `embabel-agent` | Service name in traces |
| `management.langfuse.connect-timeout-ms` | `10000` | Connection timeout in milliseconds |
| `management.langfuse.export-timeout-ms` | `30000` | Export timeout in milliseconds |

Note: The `/v1/traces` suffix is automatically appended to the endpoint.

## Langfuse Endpoints

| Environment | Endpoint |
|-------------|----------|
| Self-hosted | `http://localhost:3000/api/public/otel` |
| Langfuse Cloud (US) | `https://cloud.langfuse.com/api/public/otel` |
| Langfuse Cloud (EU) | `https://eu.cloud.langfuse.com/api/public/otel` |

## Authentication

Langfuse uses HTTP Basic Authentication:
- **Username**: Public key (`pk-lf-xxx`)
- **Password**: Secret key (`sk-lf-xxx`)

The exporter automatically handles the Base64 encoding of credentials.

## How It Works

This exporter creates a complete `OpenTelemetry` bean that:

1. **Configures the SDK**: Sets up `SdkTracerProvider` with service name
2. **Creates OTLP exporter**: Configures `OtlpHttpSpanExporter` for Langfuse
3. **Sets up batch processing**: Uses `BatchSpanProcessor` for efficient export
4. **Registers globally**: Calls `buildAndRegisterGlobal()` for context propagation

The core observability module (`embabel-agent-observability`) uses `SmartInitializingSingleton` to resolve this bean after all beans are created, ensuring no ordering issues.

## Multi-Backend Support

You can use Langfuse alongside other exporters. The core module will use whichever `OpenTelemetry` bean is available:

```yaml
management:
  langfuse:
    enabled: true
    endpoint: https://cloud.langfuse.com/api/public/otel
    public-key: pk-lf-xxx
    secret-key: sk-lf-xxx
    service-name: my-agent-app
```

For multiple backends simultaneously, you would need to configure a custom `OpenTelemetry` bean with multiple span processors.

## Trace Structure in Langfuse

With proper configuration, you'll see hierarchical traces in Langfuse:

```
MyAgent (trace root)
+-- planning:ready
+-- planning:formulated
+-- processData (action)
|   +-- gen_ai.client.operation (LLM call)
+-- analyzeResults (action)
|   +-- gen_ai.client.operation (LLM call)
|   +-- tool:searchDatabase
+-- goal:AnalyzedData
```

## Langfuse-Specific Attributes

The exporter adds Langfuse-compatible attributes:
- `langfuse.span.name`: Display name in Langfuse UI
- `langfuse.trace.name`: Trace name for grouping
- `langfuse.observation.type`: Type (agent, span, event, tool)
- `langfuse.level`: Log level (WARNING for replanning, stuck states)
- `input.value` / `output.value`: Input/output for Langfuse display

## Building

```bash
mvn clean package

mvn clean package -DskipTests
```

## Requirements

- Java 21+
- Spring Boot 3.5+
- embabel-agent-observability 0.3.2+

## License

Apache License 2.0