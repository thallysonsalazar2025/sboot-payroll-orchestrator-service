package br.com.payroll.orchestrator.domain.port;

import br.com.payroll.orchestrator.domain.model.PdfGenerationMessage;

public interface PdfMessagePublisher {
    void publish(PdfGenerationMessage message);
}
