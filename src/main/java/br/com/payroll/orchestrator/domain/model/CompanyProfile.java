package br.com.payroll.orchestrator.domain.model;

import java.math.BigDecimal;
import lombok.Builder;

@Builder(toBuilder = true)
public record CompanyProfile(
        String companyId,
        String companyName,
        String registrationNumber,
        String businessUnit,
        String payrollCalendar,
        BigDecimal defaultTaxRate) {
}
