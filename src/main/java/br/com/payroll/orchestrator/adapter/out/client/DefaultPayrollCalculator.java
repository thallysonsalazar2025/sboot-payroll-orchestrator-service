package br.com.payroll.orchestrator.adapter.out.client;

import br.com.payroll.orchestrator.domain.exception.BusinessException;
import br.com.payroll.orchestrator.domain.model.PayrollCalculationResult;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import br.com.payroll.orchestrator.domain.port.PayrollCalculator;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultPayrollCalculator implements PayrollCalculator {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.12");

    @Override
    @Retry(name = "payrollCalculator")
    @CircuitBreaker(name = "payrollCalculator")
    @Bulkhead(name = "payrollCalculator")
    public PayrollCalculationResult calculate(ProcessingContext context) {
        BigDecimal baseSalary = context.request().baseSalary();
        if (baseSalary == null || baseSalary.signum() <= 0) {
            throw new BusinessException("O salário base deve ser maior que zero");
        }

        BigDecimal benefitDiscount = context.benefitSummary().monthlyDiscount();
        BigDecimal taxAmount = baseSalary.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = baseSalary.subtract(benefitDiscount).subtract(taxAmount).setScale(2, RoundingMode.HALF_UP);

        log.info("Cálculo concluído. employeeId={}, gross={}, discounts={}, taxes={}, net={}",
                context.request().employeeId(), baseSalary, benefitDiscount, taxAmount, netAmount);

        return PayrollCalculationResult.builder()
                .grossAmount(baseSalary.setScale(2, RoundingMode.HALF_UP))
                .benefitDiscount(benefitDiscount.setScale(2, RoundingMode.HALF_UP))
                .taxAmount(taxAmount)
                .netAmount(netAmount)
                .build();
    }
}
