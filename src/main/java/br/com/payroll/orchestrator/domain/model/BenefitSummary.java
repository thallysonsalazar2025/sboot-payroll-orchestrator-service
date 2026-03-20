package br.com.payroll.orchestrator.domain.model;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record BenefitSummary(
        List<String> activeBenefits,
        BigDecimal monthlyDiscount) {
}
