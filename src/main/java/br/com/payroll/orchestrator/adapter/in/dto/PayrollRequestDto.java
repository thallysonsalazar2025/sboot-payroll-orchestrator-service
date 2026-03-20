package br.com.payroll.orchestrator.adapter.in.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PayrollRequestDto(
        String correlationId,
        @NotBlank(message = "employeeId é obrigatório") String employeeId,
        @NotBlank(message = "payrollPeriod é obrigatório no formato yyyy-MM") String payrollPeriod,
        @NotNull(message = "baseSalary é obrigatório") @DecimalMin(value = "0.01", message = "baseSalary deve ser maior que zero") BigDecimal baseSalary,
        @NotBlank(message = "requestedBy é obrigatório") String requestedBy) {
}
