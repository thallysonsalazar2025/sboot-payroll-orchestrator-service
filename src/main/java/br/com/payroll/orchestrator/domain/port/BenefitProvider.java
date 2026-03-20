package br.com.payroll.orchestrator.domain.port;

import br.com.payroll.orchestrator.domain.model.BenefitSummary;

public interface BenefitProvider {
    BenefitSummary fetchByEmployeeId(String employeeId);
}
