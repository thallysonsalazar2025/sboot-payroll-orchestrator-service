package br.com.payroll.orchestrator.application.step;

import br.com.payroll.orchestrator.domain.model.PayrollCalculationResult;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import br.com.payroll.orchestrator.domain.port.PayrollCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
@RequiredArgsConstructor
public class CalculatePayrollStep implements FlowStep {

    private final PayrollCalculator payrollCalculator;

    @Override
    public ProcessingContext execute(ProcessingContext context) {
        PayrollCalculationResult result = payrollCalculator.calculate(context);
        return context.toBuilder().calculationResult(result).build();
    }

    @Override
    public String name() {
        return "calculate-payroll";
    }
}
