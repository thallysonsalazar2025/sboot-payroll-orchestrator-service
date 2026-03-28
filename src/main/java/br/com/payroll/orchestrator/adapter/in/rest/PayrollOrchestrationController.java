package br.com.payroll.orchestrator.adapter.in.rest;

import br.com.payroll.orchestrator.adapter.in.dto.OrchestrationResponseDto;
import br.com.payroll.orchestrator.adapter.in.dto.PayrollRequestDto;
import br.com.payroll.orchestrator.adapter.in.rest.mapper.PayrollRestMapper;
import br.com.payroll.orchestrator.application.service.PayrollOrchestrationService;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import jakarta.validation.Valid;
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
    private final PayrollRestMapper payrollRestMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrchestrationResponseDto orchestrate(@Valid @RequestBody PayrollRequestDto request,
                                                @RequestHeader(name = "X-Idempotency-Key", required = false) String idempotencyKey) {
        String effectiveIdempotencyKey = payrollRestMapper.resolveIdempotencyKey(idempotencyKey, request.employeeId());
        var domainRequest = payrollRestMapper.toDomain(request);
        OrchestrationResult result = payrollOrchestrationService.orchestrate(domainRequest, effectiveIdempotencyKey);

        return payrollRestMapper.toResponse(result);
    }
}
