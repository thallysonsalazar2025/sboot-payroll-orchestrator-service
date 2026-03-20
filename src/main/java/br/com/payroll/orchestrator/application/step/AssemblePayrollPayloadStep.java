package br.com.payroll.orchestrator.application.step;

import br.com.payroll.orchestrator.domain.model.PayrollPayloadMessage;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import java.time.Instant;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(40)
public class AssemblePayrollPayloadStep implements FlowStep {

    @Override
    public ProcessingContext execute(ProcessingContext context) {
        PayrollPayloadMessage message = PayrollPayloadMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .correlationId(context.request().correlationId())
                .createdAt(Instant.now())
                .companyId(context.companyProfile().companyId())
                .companyName(context.companyProfile().companyName())
                .registrationNumber(context.companyProfile().registrationNumber())
                .businessUnit(context.companyProfile().businessUnit())
                .payrollCalendar(context.companyProfile().payrollCalendar())
                .employeeId(context.employeeProfile().employeeId())
                .employeeName(context.employeeProfile().employeeName())
                .documentNumber(context.employeeProfile().documentNumber())
                .department(context.employeeProfile().department())
                .costCenter(context.employeeProfile().costCenter())
                .email(context.employeeProfile().email())
                .payrollPeriod(context.request().payrollPeriod())
                .grossAmount(context.calculationResult().grossAmount())
                .taxRate(context.calculationResult().taxRate())
                .taxAmount(context.calculationResult().taxAmount())
                .netAmount(context.calculationResult().netAmount())
                .requestedBy(context.request().requestedBy())
                .build();

        return context.toBuilder().payrollPayloadMessage(message).build();
    }

    @Override
    public String name() {
        return "assemble-payroll-payload";
    }
}
