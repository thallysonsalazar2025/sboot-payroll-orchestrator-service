package br.com.payroll.orchestrator.application.engine;

import br.com.payroll.orchestrator.application.metrics.FlowMetrics;
import br.com.payroll.orchestrator.application.step.FlowStep;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayrollFlowEngine {

    private final List<FlowStep> flowSteps;
    private final FlowMetrics flowMetrics;

    public OrchestrationResult execute(PayrollRequest request, String idempotencyKey) {
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