package br.com.payroll.orchestrator.adapter.out.client;

import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import br.com.payroll.orchestrator.domain.model.TimeTrackingSummary;
import br.com.payroll.orchestrator.domain.port.TimeTrackingProvider;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryTimeTrackingProvider implements TimeTrackingProvider {

    private static final Map<String, TimeTrackingSummary> TIME_TRACKING_BY_EMPLOYEE = Map.of(
            "emp-001", TimeTrackingSummary.builder()
                    .workedHours(new BigDecimal("168"))
                    .overtimeHours(new BigDecimal("10"))
                    .absenceHours(new BigDecimal("2"))
                    .overtimeHourlyRate(new BigDecimal("45.00"))
                    .absenceHourlyRate(new BigDecimal("38.00"))
                    .build(),
            "emp-002", TimeTrackingSummary.builder()
                    .workedHours(new BigDecimal("160"))
                    .overtimeHours(new BigDecimal("4"))
                    .absenceHours(new BigDecimal("0"))
                    .overtimeHourlyRate(new BigDecimal("40.00"))
                    .absenceHourlyRate(new BigDecimal("35.00"))
                    .build(),
            "emp-003", TimeTrackingSummary.builder()
                    .workedHours(new BigDecimal("162"))
                    .overtimeHours(new BigDecimal("6"))
                    .absenceHours(new BigDecimal("5"))
                    .overtimeHourlyRate(new BigDecimal("42.00"))
                    .absenceHourlyRate(new BigDecimal("35.00"))
                    .build());

    @Override
    @Retry(name = "timeTrackingProvider")
    @CircuitBreaker(name = "timeTrackingProvider")
    @Bulkhead(name = "timeTrackingProvider")
    public TimeTrackingSummary fetchByPayrollRequest(PayrollRequest request) {
        log.info("Consultando ponto eletrônico do colaborador {} para período {}", request.employeeId(), request.payrollPeriod());
        return TIME_TRACKING_BY_EMPLOYEE.getOrDefault(request.employeeId(), TimeTrackingSummary.builder()
                .workedHours(new BigDecimal("160"))
                .overtimeHours(BigDecimal.ZERO)
                .absenceHours(BigDecimal.ZERO)
                .overtimeHourlyRate(new BigDecimal("35.00"))
                .absenceHourlyRate(new BigDecimal("35.00"))
                .build());
    }
}
