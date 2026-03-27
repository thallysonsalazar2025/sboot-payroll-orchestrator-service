package br.com.payroll.orchestrator.domain.port;

import br.com.payroll.orchestrator.domain.model.PayrollRequest;
import br.com.payroll.orchestrator.domain.model.TimeTrackingSummary;

public interface TimeTrackingProvider {
    TimeTrackingSummary fetchByPayrollRequest(PayrollRequest request);
}
