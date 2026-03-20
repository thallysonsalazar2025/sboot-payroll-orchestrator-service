package br.com.payroll.orchestrator.adapter.out.client;

import br.com.payroll.orchestrator.domain.model.BenefitSummary;
import br.com.payroll.orchestrator.domain.port.BenefitProvider;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryBenefitProvider implements BenefitProvider {

    private static final Map<String, BenefitSummary> BENEFITS = Map.of(
            "emp-001", BenefitSummary.builder()
                    .activeBenefits(List.of("VA", "VT", "PlanoSaude"))
                    .monthlyDiscount(new BigDecimal("650.00"))
                    .build(),
            "emp-002", BenefitSummary.builder()
                    .activeBenefits(List.of("VA", "PlanoOdontologico"))
                    .monthlyDiscount(new BigDecimal("280.00"))
                    .build());

    @Override
    @Retry(name = "benefitProvider", fallbackMethod = "fallback")
    @CircuitBreaker(name = "benefitProvider", fallbackMethod = "fallback")
    @Bulkhead(name = "benefitProvider")
    public BenefitSummary fetchByEmployeeId(String employeeId) {
        log.info("Buscando benefícios ativos do colaborador {}", employeeId);
        if ("benefits-down".equals(employeeId)) {
            throw new IllegalStateException("Sistema de benefícios indisponível");
        }
        return BENEFITS.getOrDefault(employeeId, BenefitSummary.builder()
                .activeBenefits(List.of())
                .monthlyDiscount(BigDecimal.ZERO)
                .build());
    }

    @SuppressWarnings("unused")
    private BenefitSummary fallback(String employeeId, Throwable throwable) {
        log.warn("Fallback aplicado para benefícios do colaborador {}. motivo={}", employeeId, throwable.getMessage());
        return BenefitSummary.builder()
                .activeBenefits(List.of())
                .monthlyDiscount(BigDecimal.ZERO)
                .build();
    }
}
