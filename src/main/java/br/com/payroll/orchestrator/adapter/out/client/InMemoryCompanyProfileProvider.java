package br.com.payroll.orchestrator.adapter.out.client;

import br.com.payroll.orchestrator.domain.model.CompanyProfile;
import br.com.payroll.orchestrator.domain.port.CompanyProfileProvider;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryCompanyProfileProvider implements CompanyProfileProvider {

    private static final Map<String, CompanyProfile> COMPANIES_BY_EMPLOYEE = Map.of(
            "emp-001", CompanyProfile.builder()
                    .companyId("comp-001")
                    .companyName("Acme Payments Brasil")
                    .registrationNumber("12345678000199")
                    .businessUnit("Fintech")
                    .payrollCalendar("MENSAL")
                    .defaultTaxRate(new BigDecimal("0.12"))
                    .build(),
            "emp-002", CompanyProfile.builder()
                    .companyId("comp-001")
                    .companyName("Acme Payments Brasil")
                    .registrationNumber("12345678000199")
                    .businessUnit("Fintech")
                    .payrollCalendar("MENSAL")
                    .defaultTaxRate(new BigDecimal("0.12"))
                    .build(),
            "emp-003", CompanyProfile.builder()
                    .companyId("comp-002")
                    .companyName("Acme Shared Services")
                    .registrationNumber("99887766000155")
                    .businessUnit("SharedServices")
                    .payrollCalendar("MENSAL")
                    .defaultTaxRate(new BigDecimal("0.10"))
                    .build());

    @Override
    @Retry(name = "companyProfileProvider")
    @CircuitBreaker(name = "companyProfileProvider")
    @Bulkhead(name = "companyProfileProvider")
    public CompanyProfile fetchByEmployeeId(String employeeId) {
        log.info("Buscando dados da empresa para o colaborador {}", employeeId);
        return COMPANIES_BY_EMPLOYEE.getOrDefault(employeeId, CompanyProfile.builder()
                .companyId("comp-default")
                .companyName("Empresa Padrão")
                .registrationNumber("00000000000000")
                .businessUnit("Corporate")
                .payrollCalendar("MENSAL")
                .defaultTaxRate(new BigDecimal("0.12"))
                .build());
    }
}
