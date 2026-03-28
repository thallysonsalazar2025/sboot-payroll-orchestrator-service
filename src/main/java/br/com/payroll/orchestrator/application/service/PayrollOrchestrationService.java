package br.com.payroll.orchestrator.application.service;

import br.com.payroll.orchestrator.application.executor.IdempotentFlowExecutor;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollOrchestrationService {

    private final IdempotentFlowExecutor idempotentFlowExecutor;

    public OrchestrationResult orchestrate(PayrollRequest request, String idempotencyKey) {
        log.info("Iniciando orquestração de folha. correlationId={}, employeeId={}, payrollPeriod={}",
                request.correlationId(), request.employeeId(), request.payrollPeriod());

        return idempotentFlowExecutor.execute(request, idempotencyKey);
    }
}
