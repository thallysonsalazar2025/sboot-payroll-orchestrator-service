package br.com.payroll.orchestrator.domain.port;

import br.com.payroll.orchestrator.domain.model.CompanyProfile;

public interface CompanyProfileProvider {
    CompanyProfile fetchByEmployeeId(String employeeId);
}
