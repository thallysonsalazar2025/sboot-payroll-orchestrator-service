package br.com.payroll.orchestrator.domain.port;

import br.com.payroll.orchestrator.domain.model.PayrollCalculationResult;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;

public interface PayrollCalculator {
    PayrollCalculationResult calculate(ProcessingContext context);
}
