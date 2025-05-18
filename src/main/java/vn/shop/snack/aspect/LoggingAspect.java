package vn.shop.snack.aspect;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* vn.shop.snack..*.*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Logger logger = LoggerFactory.getLogger(signature.getDeclaringType());
        String methodName = signature.getName();
        String className = signature.getDeclaringType().getSimpleName();

        Span currentSpan = Span.current();
        SpanContext spanContext = currentSpan.getSpanContext();
        String traceId = spanContext.getTraceId();
        String spanId = spanContext.getSpanId();

        String requestId = traceId.substring(0, 8);

        logger.info("START class={} method={} args={} - TraceId={} SpanId={} RequestId={}", className, methodName, Arrays.toString(joinPoint.getArgs()),
                traceId, spanId, requestId);

        long startTime = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();

            if (result instanceof Mono) {
                Mono<?> monoResult = (Mono<?>) result;
                return monoResult
                        .doOnEach(signal -> {
                            long executionTime = System.currentTimeMillis() - startTime;

                            if (signal.isOnError()) {
                                Throwable error = signal.getThrowable();
                                logger.error(
                                        "END, class={}, method={}, executionTime={}ms, status=ERROR, exception={} - TraceId={}, SpanId={}, RequestId={}", className, methodName, executionTime, error.getMessage(), error,
                                        traceId, spanId, requestId);
                            } else if (signal.isOnComplete() || signal.isOnNext()) {
                                logger.info(
                                        "END class={} method={} executionTime={}ms status=SUCCESS - TraceId={} SpanId={} RequestId={}", className, methodName, executionTime,
                                        traceId, spanId, requestId);
                            }
                        });
            } else if (result instanceof Flux) {
                Flux<?> fluxResult = (Flux<?>) result;
                return fluxResult
                        .doOnEach(signal -> {
                            long executionTime = System.currentTimeMillis() - startTime;

                            if (signal.isOnError()) {
                                Throwable error = signal.getThrowable();
                                logger.error(
                                        "END class={} method={} executionTime={}ms status=ERROR exception={} - TraceId={} SpanId={} RequestId={}", className, methodName, executionTime, error.getMessage(), error,
                                        traceId, spanId, requestId);
                            } else if (signal.isOnComplete()) {
                                logger.info(
                                        "END class={} method={} executionTime={}ms status=SUCCESS - TraceId={} SpanId={} RequestId={}", className, methodName, executionTime,
                                        traceId, spanId, requestId);
                            }
                        });
            }

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error(
                    "END class={} method={} executionTime={}ms status=ERROR exception={} - TraceId={} SpanId={} RequestId={}", className, methodName, executionTime, e.getMessage(), e,
                    traceId, spanId, requestId);
            throw e;
        }
    }
}