package br.com.payroll.orchestrator.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.payroll.orchestrator.application.metrics.FlowMetrics;
import br.com.payroll.orchestrator.application.step.FlowStep;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import br.com.payroll.orchestrator.domain.port.IdempotencyRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PayrollOrchestrationServiceTest {

    private final FlowStep step = Mockito.mock(FlowStep.class);
    private final IdempotencyRepository idempotencyRepository = Mockito.mock(IdempotencyRepository.class);
    private PayrollOrchestrationService service;

    @BeforeEach
    void setUp() {
        service = new PayrollOrchestrationService(List.of(step), idempotencyRepository, new FlowMetrics(new SimpleMeterRegistry()));
    }

    @Test
    void shouldExecuteFlowAndReturnOrchestrationResult() {
        PayrollRequest request = PayrollRequest.builder()
                .employeeId("emp-001")
                .payrollPeriod(YearMonth.of(2026, 3))
                .baseSalary(new BigDecimal("5000.00"))
                .requestedBy("bff")
                .build();

        when(step.name()).thenReturn("mock-step");
        when(step.execute(Mockito.any())).thenAnswer(invocation -> {
            ProcessingContext context = invocation.getArgument(0);
            return context.toBuilder()
                    .orchestrationResult(OrchestrationResult.builder()
                            .orchestrationId("orch-1")
                            .correlationId(context.request().correlationId())
                            .messageId("msg-1")
                            .status("ORCHESTRATED")
                            .processedAt(Instant.now())
                            .reusedResult(false)
                            .build())
                    .build();
        });
        when(idempotencyRepository.findByKey("idem-1")).thenReturn(java.util.Optional.empty());
        when(idempotencyRepository.executeOnce(Mockito.eq("idem-1"), Mockito.any())).thenAnswer(invocation -> invocation.<java.util.function.Supplier<OrchestrationResult>>getArgument(1).get());

        OrchestrationResult result = service.orchestrate(request, "idem-1");

        assertThat(result.status()).isEqualTo("ORCHESTRATED");
        assertThat(result.messageId()).isEqualTo("msg-1");
        verify(step, times(1)).execute(Mockito.any());
    }

    @Test
    void shouldReuseStoredResultWhenIdempotencyKeyAlreadyExists() {
        PayrollRequest request = PayrollRequest.builder()
                .employeeId("emp-001")
                .payrollPeriod(YearMonth.of(2026, 3))
                .baseSalary(new BigDecimal("5000.00"))
                .requestedBy("bff")
                .build();

        OrchestrationResult stored = OrchestrationResult.builder()
                .orchestrationId("orch-1")
                .correlationId("corr-1")
                .messageId("msg-1")
                .status("ORCHESTRATED")
                .processedAt(Instant.now())
                .reusedResult(true)
                .build();

        when(idempotencyRepository.findByKey("idem-1")).thenReturn(java.util.Optional.of(stored));
        when(idempotencyRepository.executeOnce(Mockito.eq("idem-1"), Mockito.any())).thenReturn(stored);

        OrchestrationResult result = service.orchestrate(request, "idem-1");

        assertThat(result.reusedResult()).isTrue();
        verify(step, times(0)).execute(Mockito.any());
    }
}
