package br.com.payroll.orchestrator.application.step;

import br.com.payroll.orchestrator.domain.model.EmployeeProfile;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import br.com.payroll.orchestrator.domain.port.EmployeeProfileProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
@RequiredArgsConstructor
public class EnrichEmployeeProfileStep implements FlowStep {

    private final EmployeeProfileProvider employeeProfileProvider;

    @Override
    public ProcessingContext execute(ProcessingContext context) {
        EmployeeProfile profile = employeeProfileProvider.fetchByEmployeeId(context.request().employeeId());
        return context.toBuilder().employeeProfile(profile).build();
    }

    @Override
    public String name() {
        return "enrich-employee-profile";
    }
}
