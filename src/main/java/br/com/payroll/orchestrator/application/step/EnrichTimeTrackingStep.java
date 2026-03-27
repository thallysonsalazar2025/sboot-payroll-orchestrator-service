package br.com.payroll.orchestrator.application.step;

import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import br.com.payroll.orchestrator.domain.model.TimeTrackingSummary;
import br.com.payroll.orchestrator.domain.port.TimeTrackingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
@RequiredArgsConstructor
public class EnrichTimeTrackingStep implements FlowStep {

    private final TimeTrackingProvider timeTrackingProvider;

    @Override
    public ProcessingContext execute(ProcessingContext context) {
        TimeTrackingSummary summary = timeTrackingProvider.fetchByPayrollRequest(context.request());
        return context.toBuilder().timeTrackingSummary(summary).build();
    }

    @Override
    public String name() {
        return "enrich-time-tracking";
    }
}
