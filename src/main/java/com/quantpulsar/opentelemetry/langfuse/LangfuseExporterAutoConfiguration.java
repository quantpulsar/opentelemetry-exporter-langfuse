package com.quantpulsar.opentelemetry.langfuse;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Auto-configuration for Langfuse OpenTelemetry exporter.
 *
 * <p>This configuration creates a {@link SpanExporter} bean configured for Langfuse,
 * with proper authentication headers and endpoint configuration.
 *
 * <p>The SpanExporter will be collected by the central OpenTelemetry configuration
 * in embabel-agent-observability, allowing multiple exporters to work together
 * (e.g., Langfuse + Zipkin).
 *
 * <p>Langfuse uses HTTP Basic Authentication with:
 * <ul>
 *   <li>Username: public key (pk-lf-xxx)</li>
 *   <li>Password: secret key (sk-lf-xxx)</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * management:
 *   langfuse:
 *     enabled: true
 *     endpoint: https://cloud.langfuse.com/api/public/otel
 *     public-key: pk-lf-xxx
 *     secret-key: sk-lf-xxx
 *     service-name: my-agent-app
 * </pre>
 *
 * @see LangfuseExporterProperties
 */
@AutoConfiguration
@EnableConfigurationProperties(LangfuseExporterProperties.class)
@ConditionalOnClass(OtlpHttpSpanExporter.class)
@ConditionalOnProperty(prefix = "management.langfuse", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LangfuseExporterAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LangfuseExporterAutoConfiguration.class);

    /**
     * Creates and configures the Langfuse SpanExporter.
     *
     * <p>The exporter is configured with:
     * <ul>
     *   <li>OTLP HTTP endpoint with Langfuse authentication</li>
     *   <li>Configurable timeouts</li>
     * </ul>
     *
     * <p>This bean will be collected by the central OpenTelemetry configuration
     * along with other SpanExporter beans (e.g., Zipkin).
     *
     * @param properties Langfuse configuration properties
     * @return configured SpanExporter for Langfuse, or null if not configured
     */
    @Bean("langfuseSpanExporter")
    @ConditionalOnMissingBean(name = "langfuseSpanExporter")
    public SpanExporter langfuseSpanExporter(LangfuseExporterProperties properties) {
        if (!properties.isConfigured()) {
            log.warn("Langfuse exporter is enabled but not fully configured. " +
                    "Please set management.langfuse.public-key and management.langfuse.secret-key. " +
                    "Langfuse exporter will not be created.");
            return null;
        }

        // Configure OTLP HTTP exporter for Langfuse
        String authHeader = createBasicAuthHeader(properties.getPublicKey(), properties.getSecretKey());

        OtlpHttpSpanExporter otlpExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(properties.getEndpoint() + "/v1/traces")
                .addHeader("Authorization", authHeader)
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .setTimeout(Duration.ofMillis(properties.getExportTimeoutMs()))
                .build();

        log.info("Langfuse: SpanExporter configured to send traces to {} (service: {})",
                properties.getEndpoint(), properties.getServiceName());

        return new LangfuseSpanExporter(otlpExporter);
    }

    /**
     * Creates HTTP Basic Authentication header value.
     *
     * @param publicKey Langfuse public key
     * @param secretKey Langfuse secret key
     * @return Base64 encoded Basic auth header value
     */
    private String createBasicAuthHeader(String publicKey, String secretKey) {
        String credentials = publicKey + ":" + secretKey;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
