package br.com.payroll.orchestrator.adapter.in.rest.mapper;

import br.com.payroll.orchestrator.adapter.in.dto.OrchestrationResponseDto;
import br.com.payroll.orchestrator.adapter.in.dto.PayrollRequestDto;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import java.time.YearMonth;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayrollRestMapper {

    public PayrollRequest toDomain(PayrollRequestDto request) {
        return PayrollRequest.builder()
                .correlationId(request.correlationId())
                .employeeId(request.employeeId())
                .payrollPeriod(YearMonth.parse(request.payrollPeriod()))
                .baseSalary(request.baseSalary())
                .requestedBy(request.requestedBy())
                .build();
    }

    public OrchestrationResponseDto toResponse(OrchestrationResult result) {
        return OrchestrationResponseDto.builder()
                .orchestrationId(result.orchestrationId())
                .correlationId(result.correlationId())
                .messageId(result.messageId())
                .status(result.status())
                .processedAt(result.processedAt())
                .reusedResult(result.reusedResult())
                .build();
    }

    public String resolveIdempotencyKey(String headerKey, String employeeId) {
        String effectiveKey = (headerKey == null || headerKey.isBlank())
                ? UUID.randomUUID().toString()
                : headerKey;

        log.info("Recebida solicitação de orquestração. employeeId={}, idempotencyKey={}",
                employeeId, effectiveKey);

        return effectiveKey;
    }
}