package br.com.payroll.orchestrator.domain.exception;

public class IntegrationException extends RuntimeException {
    public IntegrationException(String message) {
        super(message);
    }
}
