package br.com.payroll.orchestrator.adapter.in.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record TimeTrackingDto(
        @NotNull(message = "workedHours é obrigatório") @DecimalMin(value = "0.00", message = "workedHours deve ser maior ou igual a zero") BigDecimal workedHours,
        @NotNull(message = "overtimeHours é obrigatório") @DecimalMin(value = "0.00", message = "overtimeHours deve ser maior ou igual a zero") BigDecimal overtimeHours,
        @NotNull(message = "absenceHours é obrigatório") @DecimalMin(value = "0.00", message = "absenceHours deve ser maior ou igual a zero") BigDecimal absenceHours,
        @NotNull(message = "overtimeHourlyRate é obrigatório") @DecimalMin(value = "0.00", message = "overtimeHourlyRate deve ser maior ou igual a zero") BigDecimal overtimeHourlyRate,
        @NotNull(message = "absenceHourlyRate é obrigatório") @DecimalMin(value = "0.00", message = "absenceHourlyRate deve ser maior ou igual a zero") BigDecimal absenceHourlyRate) {
}
