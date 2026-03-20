package br.com.payroll.orchestrator.adapter.in.rest;

import br.com.payroll.orchestrator.adapter.in.dto.OrchestrationResponseDto;
import br.com.payroll.orchestrator.adapter.in.dto.PayrollRequestDto;
import br.com.payroll.orchestrator.application.service.PayrollOrchestrationService;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/payroll-orchestrations")
@RequiredArgsConstructor
public class PayrollOrchestrationController {

    private final PayrollOrchestrationService payrollOrchestrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrchestrationResponseDto orchestrate(@Valid @RequestBody PayrollRequestDto request,
                                                @RequestHeader(name = "X-Idempotency-Key", required = false) String idempotencyKey) {
        String effectiveIdempotencyKey = idempotencyKey == null || idempotencyKey.isBlank()
                ? UUID.randomUUID().toString()
                : idempotencyKey;

        log.info("Recebida solicitação de orquestração. employeeId={}, idempotencyKey={}",
                request.employeeId(), effectiveIdempotencyKey);

        OrchestrationResult result = payrollOrchestrationService.orchestrate(PayrollRequest.builder()
                        .correlationId(request.correlationId())
                        .employeeId(request.employeeId())
                        .payrollPeriod(YearMonth.parse(request.payrollPeriod()))
                        .baseSalary(request.baseSalary())
                        .requestedBy(request.requestedBy())
                        .build(),
                effectiveIdempotencyKey);

        return OrchestrationResponseDto.builder()
                .orchestrationId(result.orchestrationId())
                .correlationId(result.correlationId())
                .messageId(result.messageId())
                .status(result.status())
                .processedAt(result.processedAt())
                .reusedResult(result.reusedResult())
                .build();
    }
}
