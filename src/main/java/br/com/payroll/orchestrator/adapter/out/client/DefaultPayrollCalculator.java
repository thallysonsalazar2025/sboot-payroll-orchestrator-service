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

    @Override
    @Retry(name = "payrollCalculator")
    @CircuitBreaker(name = "payrollCalculator")
    @Bulkhead(name = "payrollCalculator")
    public PayrollCalculationResult calculate(ProcessingContext context) {
        BigDecimal baseSalary = context.request().baseSalary();
        if (baseSalary == null || baseSalary.signum() <= 0) {
            throw new BusinessException("O salário base deve ser maior que zero");
        }

        BigDecimal taxRate = context.companyProfile().defaultTaxRate();
        if (taxRate == null || taxRate.signum() < 0) {
            throw new BusinessException("A alíquota tributária da empresa deve ser válida");
        }

        BigDecimal grossAmount = baseSalary.setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = grossAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = grossAmount.subtract(taxAmount).setScale(2, RoundingMode.HALF_UP);

        log.info("Cálculo tributário concluído. employeeId={}, companyId={}, gross={}, taxRate={}, taxes={}, net={}",
                context.request().employeeId(), context.companyProfile().companyId(), grossAmount, taxRate, taxAmount, netAmount);

        return PayrollCalculationResult.builder()
                .grossAmount(grossAmount)
                .taxRate(taxRate)
                .taxAmount(taxAmount)
                .netAmount(netAmount)
                .build();
    }
}
