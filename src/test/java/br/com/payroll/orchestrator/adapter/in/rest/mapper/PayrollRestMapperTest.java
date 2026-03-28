package br.com.payroll.orchestrator.adapter.in.rest.mapper;

import static org.junit.jupiter.api.Assertions.*;
import br.com.payroll.orchestrator.adapter.in.dto.PayrollRequestDto;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

class PayrollRestMapperTest {

    private final PayrollRestMapper mapper = new PayrollRestMapper();

    @Test
    void shouldMapDtoToDomainCorrectly() {
        PayrollRequestDto dto = new PayrollRequestDto(
                "corr-123", "emp-001", "2023-10", new BigDecimal("5000"), "admin"
        );

        PayrollRequest domain = mapper.toDomain(dto);

        assertEquals(dto.correlationId(), domain.correlationId());
        assertEquals(dto.employeeId(), domain.employeeId());
        assertEquals(2023, domain.payrollPeriod().getYear());
        assertEquals(10, domain.payrollPeriod().getMonthValue());
        assertEquals(dto.baseSalary(), domain.baseSalary());
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