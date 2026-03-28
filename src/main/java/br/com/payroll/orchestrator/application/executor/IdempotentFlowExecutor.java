package br.com.payroll.orchestrator.application.executor;

import br.com.payroll.orchestrator.application.engine.PayrollFlowEngine;
import br.com.payroll.orchestrator.application.metrics.FlowMetrics;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import br.com.payroll.orchestrator.domain.port.IdempotencyRepository;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdempotentFlowExecutor {

    private final IdempotencyRepository idempotencyRepository;
    private final FlowMetrics flowMetrics;
    private final PayrollFlowEngine payrollFlowEngine;

    public OrchestrationResult execute(PayrollRequest request, String idempotencyKey) {
        Timer.Sample sample = flowMetrics.startFlow();

        // Verificação para fins de métrica
        if (idempotencyRepository.findByKey(idempotencyKey).isPresent()) {
            flowMetrics.incrementIdempotentHit();
        }

        try {
            // Execução atômica controlada pelo repositório
            OrchestrationResult result = idempotencyRepository.executeOnce(idempotencyKey,
                    () -> payrollFlowEngine.execute(request, idempotencyKey));

            String status = result.reusedResult() ? "reused" : "success";
            flowMetrics.stopFlow(sample, status);
            return result;
        } catch (RuntimeException ex) {
            flowMetrics.stopFlow(sample, "error");
            throw ex;
        }
    }
}