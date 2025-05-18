package vn.shop.snack.filter;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter implements WebFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "RequestId";
    private static final String TRACE_ID_MDC_KEY = "TraceId";
    private static final String SPAN_ID_MDC_KEY = "SpanId";

    @Override
    @WithSpan("RequestIdFilter")
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }

        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);

        Span currentSpan = Span.current();
        String traceId = currentSpan != null ? currentSpan.getSpanContext().getTraceId() : "";
        String spanId = currentSpan != null ? currentSpan.getSpanContext().getSpanId() : "";

        final String finalRequestId = requestId;
        final String finalTraceId = traceId;
        final String finalSpanId = spanId;

        return chain.filter(exchange)
                .contextWrite(context -> Context.of(
                        REQUEST_ID_MDC_KEY, finalRequestId,
                        TRACE_ID_MDC_KEY, finalTraceId,
                        SPAN_ID_MDC_KEY, finalSpanId))
                .doOnEach(signal -> {
                    if (signal.isOnSubscribe()) {
                        MDC.put(REQUEST_ID_MDC_KEY, finalRequestId);
                        MDC.put(TRACE_ID_MDC_KEY, finalTraceId);
                        MDC.put(SPAN_ID_MDC_KEY, finalSpanId);
                    } else {
                        MDC.remove(REQUEST_ID_MDC_KEY);
                        MDC.remove(TRACE_ID_MDC_KEY);
                        MDC.remove(SPAN_ID_MDC_KEY);
                    }
                });
    }
}