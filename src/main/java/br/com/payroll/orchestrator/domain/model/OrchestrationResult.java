package br.com.payroll.orchestrator.domain.model;

import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record OrchestrationResult(
        String orchestrationId,
        String correlationId,
        String messageId,
        String status,
        Instant processedAt,
        boolean reusedResult) {
}
