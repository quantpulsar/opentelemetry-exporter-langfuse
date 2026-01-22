package com.quantpulsar.opentelemetry.langfuse;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LangfuseExporterProperties}.
 */
class LangfuseExporterPropertiesTest {

    @Test
    void defaultValues() {
        LangfuseExporterProperties properties = new LangfuseExporterProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getEndpoint()).isEqualTo("https://cloud.langfuse.com/api/public/otel");
        assertThat(properties.getServiceName()).isEqualTo("embabel-agent");
        assertThat(properties.getConnectTimeoutMs()).isEqualTo(10000);
        assertThat(properties.getExportTimeoutMs()).isEqualTo(30000);
        assertThat(properties.getPublicKey()).isNull();
        assertThat(properties.getSecretKey()).isNull();
    }

    @Test
    void isConfigured_returnsFalse_whenPublicKeyMissing() {
        LangfuseExporterProperties properties = new LangfuseExporterProperties();
        properties.setSecretKey("sk-lf-test");

        assertThat(properties.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsFalse_whenSecretKeyMissing() {
        LangfuseExporterProperties properties = new LangfuseExporterProperties();
        properties.setPublicKey("pk-lf-test");

        assertThat(properties.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsFalse_whenKeysAreBlank() {
        LangfuseExporterProperties properties = new LangfuseExporterProperties();
        properties.setPublicKey("   ");
        properties.setSecretKey("   ");

        assertThat(properties.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsTrue_whenAllKeysSet() {
        LangfuseExporterProperties properties = new LangfuseExporterProperties();
        properties.setPublicKey("pk-lf-test");
        properties.setSecretKey("sk-lf-test");

        assertThat(properties.isConfigured()).isTrue();
    }

    @Test
    void settersAndGetters() {
        LangfuseExporterProperties properties = new LangfuseExporterProperties();

        properties.setEnabled(false);
        properties.setEndpoint("https://custom.langfuse.com");
        properties.setPublicKey("pk-lf-custom");
        properties.setSecretKey("sk-lf-custom");
        properties.setServiceName("my-service");
        properties.setConnectTimeoutMs(5000);
        properties.setExportTimeoutMs(15000);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getEndpoint()).isEqualTo("https://custom.langfuse.com");
        assertThat(properties.getPublicKey()).isEqualTo("pk-lf-custom");
        assertThat(properties.getSecretKey()).isEqualTo("sk-lf-custom");
        assertThat(properties.getServiceName()).isEqualTo("my-service");
        assertThat(properties.getConnectTimeoutMs()).isEqualTo(5000);
        assertThat(properties.getExportTimeoutMs()).isEqualTo(15000);
    }
}
