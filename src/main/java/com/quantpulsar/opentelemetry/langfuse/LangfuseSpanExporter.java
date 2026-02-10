package com.quantpulsar.opentelemetry.langfuse;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.DelegatingSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * Wrapper SpanExporter that enriches spans with Langfuse observation types at export time.
 * When {@code embabelOnly} is enabled, only spans with Embabel or GenAI attributes are exported.
 *
 * Langfuse types: agent, generation, tool, chain, retriever, embedding, evaluator, guardrail, event, span
 */
public class LangfuseSpanExporter implements SpanExporter {

    private static final Logger log = LoggerFactory.getLogger(LangfuseSpanExporter.class);

    private final SpanExporter delegate;
    private final boolean embabelOnly;

    // Langfuse
    private static final AttributeKey<String> LANGFUSE_TYPE = AttributeKey.stringKey("langfuse.observation.type");

    // Embabel event type
    private static final AttributeKey<String> EMBABEL_EVENT_TYPE = AttributeKey.stringKey("embabel.event.type");

    // Embabel individual attributes
    private static final AttributeKey<String> EMBABEL_AGENT_NAME = AttributeKey.stringKey("embabel.agent.name");
    private static final AttributeKey<String> EMBABEL_TOOL_NAME = AttributeKey.stringKey("embabel.tool.name");
    private static final AttributeKey<String> EMBABEL_ACTION_SHORT_NAME = AttributeKey.stringKey("embabel.action.short_name");
    private static final AttributeKey<String> EMBABEL_GOAL_SHORT_NAME = AttributeKey.stringKey("embabel.goal.short_name");
    private static final AttributeKey<String> EMBABEL_STATE_TO = AttributeKey.stringKey("embabel.state.to");
    private static final AttributeKey<String> EMBABEL_LIFECYCLE_STATE = AttributeKey.stringKey("embabel.lifecycle.state");
    private static final AttributeKey<String> EMBABEL_EMBEDDING_NAME = AttributeKey.stringKey("embabel.embedding.name");
    private static final AttributeKey<String> EMBABEL_RETRIEVER_NAME = AttributeKey.stringKey("embabel.retriever.name");
    private static final AttributeKey<String> EMBABEL_EVALUATOR_NAME = AttributeKey.stringKey("embabel.evaluator.name");
    private static final AttributeKey<String> EMBABEL_GUARDRAIL_NAME = AttributeKey.stringKey("embabel.guardrail.name");

    // GenAI / Spring AI
    private static final AttributeKey<String> GEN_AI_OPERATION = AttributeKey.stringKey("gen_ai.operation.name");
    private static final AttributeKey<String> GEN_AI_SYSTEM = AttributeKey.stringKey("gen_ai.system");

    /**
     * Creates a new LangfuseSpanExporter wrapping the given delegate exporter.
     *
     * @param delegate the underlying SpanExporter to delegate to after enrichment
     * @param embabelOnly when true, only export spans with Embabel or GenAI attributes
     */
    public LangfuseSpanExporter(SpanExporter delegate, boolean embabelOnly) {
        this.delegate = delegate;
        this.embabelOnly = embabelOnly;
        if (embabelOnly) {
            log.info("Langfuse: embabel-only mode enabled, non-Embabel spans will be filtered out");
        }
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        List<SpanData> toExport;
        if (embabelOnly) {
            toExport = spans.stream()
                    .filter(this::isEmbabelOrGenAiSpan)
                    .map(this::enrich)
                    .toList();
            if (toExport.isEmpty()) {
                return CompletableResultCode.ofSuccess();
            }
        } else {
            toExport = spans.stream().map(this::enrich).toList();
        }
        return delegate.export(toExport);
    }

    /**
     * Checks if a span has Embabel or GenAI attributes.
     */
    private boolean isEmbabelOrGenAiSpan(SpanData span) {
        Attributes attrs = span.getAttributes();

        // Already tagged for Langfuse
        if (attrs.get(LANGFUSE_TYPE) != null) {
            return true;
        }

        // Embabel attributes
        if (attrs.get(EMBABEL_EVENT_TYPE) != null
                || attrs.get(EMBABEL_AGENT_NAME) != null
                || attrs.get(EMBABEL_TOOL_NAME) != null
                || attrs.get(EMBABEL_ACTION_SHORT_NAME) != null
                || attrs.get(EMBABEL_GOAL_SHORT_NAME) != null
                || attrs.get(EMBABEL_STATE_TO) != null
                || attrs.get(EMBABEL_LIFECYCLE_STATE) != null
                || attrs.get(EMBABEL_EMBEDDING_NAME) != null
                || attrs.get(EMBABEL_RETRIEVER_NAME) != null
                || attrs.get(EMBABEL_EVALUATOR_NAME) != null
                || attrs.get(EMBABEL_GUARDRAIL_NAME) != null) {
            return true;
        }

        // GenAI spans (LLM calls)
        if (attrs.get(GEN_AI_OPERATION) != null || attrs.get(GEN_AI_SYSTEM) != null) {
            return true;
        }

        return false;
    }

    private SpanData enrich(SpanData span) {
        Attributes attrs = span.getAttributes();

        if (attrs.get(LANGFUSE_TYPE) != null) {
            return span;
        }

        String type = detectType(attrs);
        Attributes newAttrs = attrs.toBuilder().put(LANGFUSE_TYPE, type).build();

        return new DelegatingSpanData(span) {
            @Override
            public Attributes getAttributes() {
                return newAttrs;
            }
        };
    }

    private String detectType(Attributes attrs) {
        // 1. Check embabel.event.type first (most specific)
        String eventType = attrs.get(EMBABEL_EVENT_TYPE);
        if (eventType != null) {
            String mapped = mapEventType(eventType);
            if (mapped != null) {
                return mapped;
            }
        }

        // 2. Check individual Embabel attributes
        if (attrs.get(EMBABEL_AGENT_NAME) != null) return "agent";
        if (attrs.get(EMBABEL_TOOL_NAME) != null) return "tool";
        if (attrs.get(EMBABEL_ACTION_SHORT_NAME) != null) return "chain";
        if (attrs.get(EMBABEL_EMBEDDING_NAME) != null) return "embedding";
        if (attrs.get(EMBABEL_RETRIEVER_NAME) != null) return "retriever";
        if (attrs.get(EMBABEL_EVALUATOR_NAME) != null) return "evaluator";
        if (attrs.get(EMBABEL_GUARDRAIL_NAME) != null) return "guardrail";
        if (attrs.get(EMBABEL_GOAL_SHORT_NAME) != null) return "event";
        if (attrs.get(EMBABEL_STATE_TO) != null) return "event";
        if (attrs.get(EMBABEL_LIFECYCLE_STATE) != null) return "event";

        // 3. GenAI (LLM calls) - only for actual chat operations
        if ("chat".equals(attrs.get(GEN_AI_OPERATION))) {
            return "generation";
        }

        // 4. Default fallback
        return "span";
    }

    private String mapEventType(String eventType) {
        return switch (eventType) {
            case "agent_process" -> "agent";
            case "action" -> "chain";
            case "tool_call" -> "tool";
            case "embedding" -> "embedding";
            case "retriever" -> "retriever";
            case "evaluator" -> "evaluator";
            case "guardrail" -> "guardrail";
            case "planning", "goal_achieved", "planning_ready", "plan_formulated", "replanning",
                 "state_transition", "lifecycle_waiting", "lifecycle_paused", "lifecycle_stuck",
                 "object_added", "object_bound" -> "event";
            default -> null;
        };
    }

    @Override
    public CompletableResultCode flush() {
        return delegate.flush();
    }

    @Override
    public CompletableResultCode shutdown() {
        return delegate.shutdown();
    }
}
