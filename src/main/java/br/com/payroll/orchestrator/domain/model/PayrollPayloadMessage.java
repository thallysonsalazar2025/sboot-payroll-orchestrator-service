package br.com.payroll.orchestrator.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import lombok.Builder;

@Builder(toBuilder = true)
public record PayrollPayloadMessage(
        String messageId,
        String correlationId,
        Instant createdAt,
        String companyId,
        String companyName,
        String registrationNumber,
        String businessUnit,
        String payrollCalendar,
        String employeeId,
        String employeeName,
        String documentNumber,
        String department,
        String costCenter,
        String email,
        YearMonth payrollPeriod,
        BigDecimal grossAmount,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal netAmount,
        String requestedBy) {
}
