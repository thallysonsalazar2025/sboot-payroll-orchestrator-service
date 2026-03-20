package br.com.payroll.orchestrator.application.step;

import br.com.payroll.orchestrator.domain.model.BenefitSummary;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import br.com.payroll.orchestrator.domain.port.BenefitProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
@RequiredArgsConstructor
public class EnrichBenefitsStep implements FlowStep {

    private final BenefitProvider benefitProvider;

    @Override
    public ProcessingContext execute(ProcessingContext context) {
        BenefitSummary benefits = benefitProvider.fetchByEmployeeId(context.request().employeeId());
        return context.toBuilder().benefitSummary(benefits).build();
    }

    @Override
    public String name() {
        return "enrich-benefits";
    }
}
