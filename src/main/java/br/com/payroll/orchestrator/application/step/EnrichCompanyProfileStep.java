package br.com.payroll.orchestrator.application.step;

import br.com.payroll.orchestrator.domain.model.CompanyProfile;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import br.com.payroll.orchestrator.domain.port.CompanyProfileProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
@RequiredArgsConstructor
public class EnrichCompanyProfileStep implements FlowStep {

    private final CompanyProfileProvider companyProfileProvider;

    @Override
    public ProcessingContext execute(ProcessingContext context) {
        CompanyProfile companyProfile = companyProfileProvider.fetchByEmployeeId(context.request().employeeId());
        return context.toBuilder().companyProfile(companyProfile).build();
    }

    @Override
    public String name() {
        return "enrich-company-profile";
    }
}
