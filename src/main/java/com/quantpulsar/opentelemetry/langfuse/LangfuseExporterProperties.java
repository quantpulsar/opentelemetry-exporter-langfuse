package com.quantpulsar.opentelemetry.langfuse;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Langfuse exporter.
 *
 * <p>Example configuration in application.yml:
 * <pre>
 * management:
 *   langfuse:
 *     enabled: true
 *     endpoint: https://cloud.langfuse.com/api/public/otel
 *     public-key: pk-lf-xxx
 *     secret-key: sk-lf-xxx
 * </pre>
 *
 * <p>For self-hosted Langfuse:
 * <pre>
 * management:
 *   langfuse:
 *     endpoint: https://your-langfuse.example.com/api/public/otel
 * </pre>
 */
@ConfigurationProperties(prefix = "management.langfuse")
public class LangfuseExporterProperties {

    /**
     * Enable or disable the Langfuse exporter.
     */
    private boolean enabled = true;

    /**
     * Langfuse OTLP endpoint URL (base URL, /v1/traces is added automatically).
     * Default is Langfuse Cloud EU.
     */
    private String endpoint = "https://cloud.langfuse.com/api/public/otel";

    /**
     * Langfuse public key (pk-lf-xxx).
     */
    private String publicKey;

    /**
     * Langfuse secret key (sk-lf-xxx).
     */
    private String secretKey;

    /**
     * Service name for traces.
     */
    private String serviceName = "embabel-agent";

    /**
     * Connection timeout in milliseconds.
     */
    private long connectTimeoutMs = 10000;

    /**
     * Export timeout in milliseconds.
     */
    private long exportTimeoutMs = 30000;

    // ==================== Getters and Setters ====================

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public long getExportTimeoutMs() {
        return exportTimeoutMs;
    }

    public void setExportTimeoutMs(long exportTimeoutMs) {
        this.exportTimeoutMs = exportTimeoutMs;
    }

    /**
     * Validates that required properties are set.
     *
     * @return true if all required properties are configured
     */
    public boolean isConfigured() {
        return publicKey != null && !publicKey.isBlank()
                && secretKey != null && !secretKey.isBlank()
                && endpoint != null && !endpoint.isBlank();
    }
}
