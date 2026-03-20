package br.com.payroll.orchestrator.application.service;

import br.com.payroll.orchestrator.application.metrics.FlowMetrics;
import br.com.payroll.orchestrator.application.step.FlowStep;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import br.com.payroll.orchestrator.domain.port.IdempotencyRepository;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollOrchestrationService {

    private final List<FlowStep> flowSteps;
    private final IdempotencyRepository idempotencyRepository;
    private final FlowMetrics flowMetrics;

    public OrchestrationResult orchestrate(PayrollRequest request, String idempotencyKey) {
        log.info("Iniciando orquestração de folha. correlationId={}, employeeId={}, payrollPeriod={}",
                request.correlationId(), request.employeeId(), request.payrollPeriod());

        Timer.Sample sample = flowMetrics.startFlow();
        boolean existing = idempotencyRepository.findByKey(idempotencyKey).isPresent();
        if (existing) {
            flowMetrics.incrementIdempotentHit();
        }

        try {
            OrchestrationResult result = idempotencyRepository.executeOnce(idempotencyKey,
                    () -> executeFlow(request, idempotencyKey));
            String status = result.reusedResult() ? "reused" : "success";
            flowMetrics.stopFlow(sample, status);
            return result;
        } catch (RuntimeException ex) {
            flowMetrics.stopFlow(sample, "error");
            throw ex;
        }
    }

    private OrchestrationResult executeFlow(PayrollRequest request, String idempotencyKey) {
        ProcessingContext context = ProcessingContext.builder()
                .idempotencyKey(idempotencyKey)
                .request(request.toBuilder()
                        .correlationId(request.correlationId() == null || request.correlationId().isBlank()
                                ? UUID.randomUUID().toString()
                                : request.correlationId())
                        .build())
                .build();

        for (FlowStep flowStep : flowSteps) {
            log.info("Executando etapa {} para correlationId={}", flowStep.name(), context.request().correlationId());
            flowMetrics.incrementStep(flowStep.name());
            context = flowStep.execute(context);
        }

        log.info("Fluxo concluído com sucesso. correlationId={}, messageId={}",
                context.request().correlationId(), context.orchestrationResult().messageId());

        return context.orchestrationResult().toBuilder()
                .orchestrationId(context.orchestrationResult().orchestrationId() == null
                        ? UUID.randomUUID().toString()
                        : context.orchestrationResult().orchestrationId())
                .processedAt(context.orchestrationResult().processedAt() == null ? Instant.now() : context.orchestrationResult().processedAt())
                .build();
    }
}
