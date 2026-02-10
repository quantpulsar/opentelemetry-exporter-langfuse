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
     * Default constructor.
     */
    public LangfuseExporterProperties() {
        // Default constructor for configuration properties binding
    }

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

    /**
     * When true, only export spans that have Embabel or GenAI attributes.
     * This filters out HTTP server spans (e.g., health checks, actuator endpoints).
     */
    private boolean embabelOnly = false;

    // ==================== Getters and Setters ====================

    /**
     * Returns whether the Langfuse exporter is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the Langfuse exporter is enabled.
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the Langfuse OTLP endpoint URL.
     *
     * @return the endpoint URL
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the Langfuse OTLP endpoint URL.
     *
     * @param endpoint the endpoint URL
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Returns the Langfuse public key.
     *
     * @return the public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Sets the Langfuse public key.
     *
     * @param publicKey the public key (pk-lf-xxx)
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Returns the Langfuse secret key.
     *
     * @return the secret key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Sets the Langfuse secret key.
     *
     * @param secretKey the secret key (sk-lf-xxx)
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Returns the service name for traces.
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the service name for traces.
     *
     * @param serviceName the service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Returns the connection timeout in milliseconds.
     *
     * @return the connection timeout
     */
    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    /**
     * Sets the connection timeout in milliseconds.
     *
     * @param connectTimeoutMs the connection timeout
     */
    public void setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    /**
     * Returns the export timeout in milliseconds.
     *
     * @return the export timeout
     */
    public long getExportTimeoutMs() {
        return exportTimeoutMs;
    }

    /**
     * Sets the export timeout in milliseconds.
     *
     * @param exportTimeoutMs the export timeout
     */
    public void setExportTimeoutMs(long exportTimeoutMs) {
        this.exportTimeoutMs = exportTimeoutMs;
    }

    /**
     * Returns whether only Embabel/GenAI spans should be exported.
     *
     * @return {@code true} if only Embabel spans are exported, {@code false} for all spans
     */
    public boolean isEmbabelOnly() {
        return embabelOnly;
    }

    /**
     * Sets whether only Embabel/GenAI spans should be exported.
     *
     * @param embabelOnly {@code true} to export only Embabel spans
     */
    public void setEmbabelOnly(boolean embabelOnly) {
        this.embabelOnly = embabelOnly;
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
