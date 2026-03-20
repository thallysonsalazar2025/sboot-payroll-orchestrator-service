package br.com.payroll.orchestrator.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record EmployeeProfile(
        String employeeId,
        String employeeName,
        String documentNumber,
        String department,
        String costCenter,
        String email) {
}
