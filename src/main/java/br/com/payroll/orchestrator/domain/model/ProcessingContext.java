package br.com.payroll.orchestrator.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record ProcessingContext(
        String idempotencyKey,
        PayrollRequest request,
        EmployeeProfile employeeProfile,
        BenefitSummary benefitSummary,
        PayrollCalculationResult calculationResult,
        PdfGenerationMessage pdfGenerationMessage,
        OrchestrationResult orchestrationResult) {
}
