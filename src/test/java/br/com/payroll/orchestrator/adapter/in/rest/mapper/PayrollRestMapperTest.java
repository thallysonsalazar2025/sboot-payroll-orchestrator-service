package br.com.payroll.orchestrator.adapter.in.rest.mapper;

import static org.junit.jupiter.api.Assertions.*;

import br.com.payroll.orchestrator.adapter.in.dto.PayrollRequestDto;
import br.com.payroll.orchestrator.adapter.in.dto.TimeTrackingDto;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import br.com.payroll.orchestrator.domain.model.TimeTrackingSummary;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PayrollRestMapperTest {

    private final PayrollRestMapper mapper = new PayrollRestMapper();

    @Test
    void shouldMapDtoToDomainCorrectly() {
        TimeTrackingDto timeTracking = TimeTrackingDto.builder()
                .workedHours(new BigDecimal("168"))
                .overtimeHours(new BigDecimal("10"))
                .absenceHours(new BigDecimal("2"))
                .overtimeHourlyRate(new BigDecimal("45.00"))
                .absenceHourlyRate(new BigDecimal("38.00"))
                .build();

        PayrollRequestDto dto = new PayrollRequestDto(
                "corr-123", "emp-001", "2023-10", new BigDecimal("5000"), "admin", timeTracking
        );

        PayrollRequest domain = mapper.toDomain(dto);
        TimeTrackingSummary summary = domain.timeTrackingSummary();

        assertEquals(dto.correlationId(), domain.correlationId());
        assertEquals(dto.employeeId(), domain.employeeId());
        assertEquals(2023, domain.payrollPeriod().getYear());
        assertEquals(10, domain.payrollPeriod().getMonthValue());
        assertEquals(dto.baseSalary(), domain.baseSalary());
        assertEquals(dto.timeTracking().workedHours(), summary.workedHours());
        assertEquals(dto.timeTracking().overtimeHours(), summary.overtimeHours());
        assertEquals(dto.timeTracking().absenceHours(), summary.absenceHours());
    }

    @Test
    void shouldResolveProvidedIdempotencyKey() {
        String headerKey = "manual-key";
        String resolved = mapper.resolveIdempotencyKey(headerKey, "emp-1");
        assertEquals(headerKey, resolved);
    }

    @Test
    void shouldGenerateRandomIdempotencyKeyWhenMissing() {
        String resolvedNull = mapper.resolveIdempotencyKey(null, "emp-1");
        String resolvedBlank = mapper.resolveIdempotencyKey("", "emp-1");

        assertNotNull(resolvedNull);
        assertFalse(resolvedNull.isBlank());
        assertNotNull(resolvedBlank);
        assertFalse(resolvedBlank.isBlank());
        assertNotEquals(resolvedNull, resolvedBlank);
    }
}
