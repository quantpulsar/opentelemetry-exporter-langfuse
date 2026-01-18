package com.embabel.agent.observability.langfuse;

import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LangfuseExporterAutoConfiguration}.
 *
 * <p>This configuration creates a {@link SpanExporter} bean that will be collected
 * by the central OpenTelemetry configuration in embabel-agent-observability.
 */
class LangfuseExporterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LangfuseExporterAutoConfiguration.class));

    @Test
    void autoConfiguration_returnsNullSpanExporter_whenNotConfigured() {
        contextRunner
                .run(context -> {
                    // When not configured (no keys), the bean factory method returns null
                    // Spring registers the null value, but we can't retrieve it via getBean()
                    // The central OpenTelemetry config will filter out null exporters
                    assertThat(context).hasBean("langfuseSpanExporter");
                    // Verify properties bean exists but is not fully configured
                    LangfuseExporterProperties props = context.getBean(LangfuseExporterProperties.class);
                    assertThat(props.isConfigured()).isFalse();
                });
    }

    @Test
    void autoConfiguration_createsSpanExporter_whenFullyConfigured() {
        contextRunner
                .withPropertyValues(
                        "management.langfuse.public-key=pk-lf-test",
                        "management.langfuse.secret-key=sk-lf-test"
                )
                .run(context -> {
                    assertThat(context).hasBean("langfuseSpanExporter");
                    SpanExporter exporter = context.getBean("langfuseSpanExporter", SpanExporter.class);
                    assertThat(exporter).isNotNull();
                    assertThat(exporter).isInstanceOf(LangfuseSpanExporter.class);
                });
    }

    @Test
    void autoConfiguration_disabled_whenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("management.langfuse.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("langfuseSpanExporter");
                });
    }

    @Test
    void autoConfiguration_usesCustomEndpoint() {
        contextRunner
                .withPropertyValues(
                        "management.langfuse.public-key=pk-lf-test",
                        "management.langfuse.secret-key=sk-lf-test",
                        "management.langfuse.endpoint=https://custom.langfuse.com/api/public/otel"
                )
                .run(context -> {
                    assertThat(context).hasBean("langfuseSpanExporter");
                    assertThat(context).hasSingleBean(LangfuseExporterProperties.class);
                    LangfuseExporterProperties props = context.getBean(LangfuseExporterProperties.class);
                    assertThat(props.getEndpoint()).isEqualTo("https://custom.langfuse.com/api/public/otel");
                });
    }

    @Test
    void autoConfiguration_usesCustomServiceName() {
        contextRunner
                .withPropertyValues(
                        "management.langfuse.public-key=pk-lf-test",
                        "management.langfuse.secret-key=sk-lf-test",
                        "management.langfuse.service-name=my-custom-service"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(LangfuseExporterProperties.class);
                    LangfuseExporterProperties props = context.getBean(LangfuseExporterProperties.class);
                    assertThat(props.getServiceName()).isEqualTo("my-custom-service");
                });
    }

    @Test
    void autoConfiguration_doesNotOverrideExistingSpanExporterBean() {
        SpanExporter customExporter = SpanExporter.composite();

        contextRunner
                .withBean("langfuseSpanExporter", SpanExporter.class, () -> customExporter)
                .withPropertyValues(
                        "management.langfuse.public-key=pk-lf-test",
                        "management.langfuse.secret-key=sk-lf-test"
                )
                .run(context -> {
                    assertThat(context).hasBean("langfuseSpanExporter");
                    assertThat(context.getBean("langfuseSpanExporter", SpanExporter.class)).isSameAs(customExporter);
                });
    }
}
