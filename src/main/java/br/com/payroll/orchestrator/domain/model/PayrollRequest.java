package br.com.payroll.orchestrator.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import lombok.Builder;

@Builder(toBuilder = true)
public record PayrollRequest(
        String correlationId,
        String employeeId,
        YearMonth payrollPeriod,
        BigDecimal baseSalary,
        String requestedBy) {
}
