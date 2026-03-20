package br.com.payroll.orchestrator.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record ProcessingContext(
        String idempotencyKey,
        PayrollRequest request,
        CompanyProfile companyProfile,
        EmployeeProfile employeeProfile,
        PayrollCalculationResult calculationResult,
        PayrollPayloadMessage payrollPayloadMessage,
        OrchestrationResult orchestrationResult) {
}
