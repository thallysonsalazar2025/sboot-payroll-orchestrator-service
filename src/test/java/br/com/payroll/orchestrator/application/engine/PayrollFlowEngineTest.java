package br.com.payroll.orchestrator.application.engine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import br.com.payroll.orchestrator.application.metrics.FlowMetrics;
import br.com.payroll.orchestrator.application.step.FlowStep;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import org.junit.jupiter.api.Test;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

class PayrollFlowEngineTest {

    @Test
    void shouldExecuteAllStepsInOrder() {
        FlowStep step1 = mock(FlowStep.class);
        FlowStep step2 = mock(FlowStep.class);
        FlowMetrics metrics = mock(FlowMetrics.class);
        
        when(step1.name()).thenReturn("Step1");
        when(step2.name()).thenReturn("Step2");
        
        when(step1.execute(any())).thenAnswer(i -> i.getArgument(0));
        when(step2.execute(any())).thenAnswer(i -> {
            ProcessingContext ctx = i.getArgument(0);
            return ctx.toBuilder()
                    .orchestrationResult(OrchestrationResult.builder()
                            .status("SUCCESS")
                            .messageId(UUID.randomUUID().toString())
                            .build())
                    .build();
        });

        PayrollFlowEngine engine = new PayrollFlowEngine(List.of(step1, step2), metrics);
        
        PayrollRequest request = PayrollRequest.builder()
                .employeeId("emp-123")
                .payrollPeriod(YearMonth.now())
                .build();
        
        OrchestrationResult result = engine.execute(request, "idem-1");

        assertNotNull(result);
        assertEquals("SUCCESS", result.status());
        assertNotNull(result.orchestrationId());
        assertNotNull(result.processedAt());
        
        verify(step1, times(1)).execute(any());
        verify(step2, times(1)).execute(any());
        verify(metrics, times(2)).incrementStep(anyString());
    }
}