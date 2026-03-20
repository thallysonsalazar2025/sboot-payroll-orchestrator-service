package br.com.payroll.orchestrator.domain.port;

import br.com.payroll.orchestrator.domain.model.EmployeeProfile;

public interface EmployeeProfileProvider {
    EmployeeProfile fetchByEmployeeId(String employeeId);
}
