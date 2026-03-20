package br.com.payroll.orchestrator.adapter.in.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record ErrorResponseDto(
        Instant timestamp,
        int status,
        String error,
        List<String> details) {
}
