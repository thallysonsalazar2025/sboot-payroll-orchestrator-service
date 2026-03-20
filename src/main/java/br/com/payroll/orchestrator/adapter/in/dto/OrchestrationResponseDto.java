package br.com.payroll.orchestrator.adapter.in.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record OrchestrationResponseDto(
        String orchestrationId,
        String correlationId,
        String messageId,
        String status,
        Instant processedAt,
        boolean reusedResult) {
}
