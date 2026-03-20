package br.com.payroll.orchestrator.application.step;

import br.com.payroll.orchestrator.domain.model.PdfGenerationMessage;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import java.time.Instant;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(40)
public class AssemblePdfPayloadStep implements FlowStep {

    @Override
    public ProcessingContext execute(ProcessingContext context) {
        PdfGenerationMessage message = PdfGenerationMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .correlationId(context.request().correlationId())
                .createdAt(Instant.now())
                .employeeId(context.employeeProfile().employeeId())
                .employeeName(context.employeeProfile().employeeName())
                .documentNumber(context.employeeProfile().documentNumber())
                .department(context.employeeProfile().department())
                .costCenter(context.employeeProfile().costCenter())
                .email(context.employeeProfile().email())
                .payrollPeriod(context.request().payrollPeriod())
                .grossAmount(context.calculationResult().grossAmount())
                .benefitDiscount(context.calculationResult().benefitDiscount())
                .taxAmount(context.calculationResult().taxAmount())
                .netAmount(context.calculationResult().netAmount())
                .activeBenefits(context.benefitSummary().activeBenefits())
                .requestedBy(context.request().requestedBy())
                .build();

        return context.toBuilder().pdfGenerationMessage(message).build();
    }

    @Override
    public String name() {
        return "assemble-pdf-payload";
    }
}
