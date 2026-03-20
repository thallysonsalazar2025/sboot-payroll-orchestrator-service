package br.com.payroll.orchestrator.application.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class FlowMetrics {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> stepCounters = new ConcurrentHashMap<>();

    public FlowMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startFlow() {
        return Timer.start(meterRegistry);
    }

    public void stopFlow(Timer.Sample sample, String status) {
        sample.stop(Timer.builder("payroll.orchestration.duration")
                .description("Tempo total do fluxo de orquestração da folha")
                .tag("status", status)
                .register(meterRegistry));
    }

    public void incrementStep(String stepName) {
        stepCounters.computeIfAbsent(stepName, key -> Counter.builder("payroll.orchestration.step.executions")
                .description("Quantidade de execuções por etapa da orquestração")
                .tag("step", key)
                .register(meterRegistry)).increment();
    }

    public void incrementIdempotentHit() {
        Counter.builder("payroll.orchestration.idempotent.reuse")
                .description("Quantidade de reusos via idempotência")
                .register(meterRegistry)
                .increment();
    }
}
