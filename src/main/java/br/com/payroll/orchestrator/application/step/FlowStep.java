package br.com.payroll.orchestrator.application.step;

import br.com.payroll.orchestrator.domain.model.ProcessingContext;

public interface FlowStep {
    ProcessingContext execute(ProcessingContext context);

    String name();
}
