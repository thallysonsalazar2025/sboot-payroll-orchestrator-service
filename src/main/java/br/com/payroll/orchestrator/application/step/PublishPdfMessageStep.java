package br.com.payroll.orchestrator.application.step;

import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import br.com.payroll.orchestrator.domain.port.PdfMessagePublisher;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(50)
@RequiredArgsConstructor
public class PublishPdfMessageStep implements FlowStep {

    private final PdfMessagePublisher pdfMessagePublisher;

    @Override
    public ProcessingContext execute(ProcessingContext context) {
        pdfMessagePublisher.publish(context.pdfGenerationMessage());

        OrchestrationResult result = OrchestrationResult.builder()
                .orchestrationId(UUID.randomUUID().toString())
                .correlationId(context.request().correlationId())
                .messageId(context.pdfGenerationMessage().messageId())
                .status("PUBLISHED")
                .processedAt(Instant.now())
                .reusedResult(false)
                .build();

        return context.toBuilder().orchestrationResult(result).build();
    }

    @Override
    public String name() {
        return "publish-pdf-message";
    }
}
