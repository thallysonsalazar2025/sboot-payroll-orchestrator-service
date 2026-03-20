package br.com.payroll.orchestrator.domain.model;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record PdfGenerationMessage(
        String messageId,
        String correlationId,
        Instant createdAt,
        String employeeId,
        String employeeName,
        String documentNumber,
        String department,
        String costCenter,
        String email,
        YearMonth payrollPeriod,
        java.math.BigDecimal grossAmount,
        java.math.BigDecimal benefitDiscount,
        java.math.BigDecimal taxAmount,
        java.math.BigDecimal netAmount,
        List<String> activeBenefits,
        String requestedBy) {
}
