package br.com.payroll.orchestrator.application.executor;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import br.com.payroll.orchestrator.application.engine.PayrollFlowEngine;
import br.com.payroll.orchestrator.application.metrics.FlowMetrics;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import br.com.payroll.orchestrator.domain.port.IdempotencyRepository;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.function.Supplier;

class IdempotentFlowExecutorTest {

    @Test
    void shouldIncrementHitMetricWhenKeyExists() {
        IdempotencyRepository repo = mock(IdempotencyRepository.class);
        FlowMetrics metrics = mock(FlowMetrics.class);
        PayrollFlowEngine engine = mock(PayrollFlowEngine.class);
        Timer.Sample sample = mock(Timer.Sample.class);

        when(metrics.startFlow()).thenReturn(sample);
        when(repo.findByKey("key-1")).thenReturn(Optional.of(OrchestrationResult.builder().build()));
        when(repo.executeOnce(anyString(), any())).thenReturn(OrchestrationResult.builder().reusedResult(true).build());

        IdempotentFlowExecutor executor = new IdempotentFlowExecutor(repo, metrics, engine);
        executor.execute(PayrollRequest.builder().build(), "key-1");

        verify(metrics).incrementIdempotentHit();
        verify(metrics).stopFlow(eq(sample), eq("reused"));
    }

    @Test
    void shouldHandleExceptionAndStopFlowWithErrorMessage() {
        IdempotencyRepository repo = mock(IdempotencyRepository.class);
        FlowMetrics metrics = mock(FlowMetrics.class);
        PayrollFlowEngine engine = mock(PayrollFlowEngine.class);
        Timer.Sample sample = mock(Timer.Sample.class);

        when(metrics.startFlow()).thenReturn(sample);
        when(repo.executeOnce(anyString(), any())).thenThrow(new RuntimeException("Fail"));

        IdempotentFlowExecutor executor = new IdempotentFlowExecutor(repo, metrics, engine);

        assertThrows(RuntimeException.class, () -> executor.execute(PayrollRequest.builder().build(), "key-err"));
        verify(metrics).stopFlow(sample, "error");
    }
}