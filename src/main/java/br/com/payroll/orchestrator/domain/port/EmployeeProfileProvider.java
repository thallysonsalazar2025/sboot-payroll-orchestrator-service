package br.com.payroll.orchestrator.domain.port;

import br.com.payroll.orchestrator.domain.model.CompanyProfile;
import br.com.payroll.orchestrator.domain.model.EmployeeProfile;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;

public interface EmployeeProfileProvider {
    EmployeeProfile fetchByEmployeeId(PayrollRequest request, CompanyProfile companyProfile);
}
