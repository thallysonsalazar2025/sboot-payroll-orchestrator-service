package br.com.payroll.orchestrator.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.payroll.orchestrator.application.executor.IdempotentFlowExecutor;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PayrollOrchestrationServiceTest {

    private final IdempotentFlowExecutor executor = Mockito.mock(IdempotentFlowExecutor.class);
    private PayrollOrchestrationService service;

    @BeforeEach
    void setUp() {
        service = new PayrollOrchestrationService(executor);
    }

    @Test
    void shouldDelegateExecutionToIdempotentFlowExecutor() {
        PayrollRequest request = PayrollRequest.builder()
                .correlationId("corr-001")
                .employeeId("emp-001")
                .payrollPeriod(YearMonth.of(2026, 3))
                .baseSalary(new BigDecimal("5000.00"))
                .requestedBy("bff")
                .build();

        OrchestrationResult expected = OrchestrationResult.builder()
                .orchestrationId("orch-001")
                .correlationId("corr-001")
                .messageId("msg-001")
                .status("ORCHESTRATED")
                .processedAt(Instant.now())
                .reusedResult(false)
                .build();

        when(executor.execute(request, "idem-001")).thenReturn(expected);

        OrchestrationResult actual = service.orchestrate(request, "idem-001");

        assertThat(actual).isSameAs(expected);
        verify(executor).execute(request, "idem-001");
    }
}
