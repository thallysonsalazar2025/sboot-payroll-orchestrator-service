package br.com.payroll.orchestrator.domain.model;

import java.math.BigDecimal;
import lombok.Builder;

@Builder(toBuilder = true)
public record PayrollCalculationResult(
        BigDecimal grossAmount,
        BigDecimal benefitDiscount,
        BigDecimal taxAmount,
        BigDecimal netAmount) {
}
