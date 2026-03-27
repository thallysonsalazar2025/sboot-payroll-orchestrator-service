package br.com.payroll.orchestrator.domain.model;

import java.math.BigDecimal;
import lombok.Builder;

@Builder(toBuilder = true)
public record TimeTrackingSummary(
        BigDecimal workedHours,
        BigDecimal overtimeHours,
        BigDecimal absenceHours,
        BigDecimal overtimeHourlyRate,
        BigDecimal absenceHourlyRate) {
}
