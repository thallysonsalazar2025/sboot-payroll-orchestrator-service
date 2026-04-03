package br.com.payroll.orchestrator.adapter.in.rest;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.payroll.orchestrator.application.metrics.FlowMetrics;
import br.com.payroll.orchestrator.domain.exception.IntegrationException;
import br.com.payroll.orchestrator.domain.exception.NotFoundException;
import br.com.payroll.orchestrator.domain.model.CompanyProfile;
import br.com.payroll.orchestrator.domain.model.EmployeeProfile;
import br.com.payroll.orchestrator.domain.model.PayrollPayloadMessage;
import br.com.payroll.orchestrator.domain.model.TimeTrackingSummary;
import br.com.payroll.orchestrator.domain.port.CompanyProfileProvider;
import br.com.payroll.orchestrator.domain.port.EmployeeProfileProvider;
import br.com.payroll.orchestrator.domain.port.PayrollMessagePublisher;
import br.com.payroll.orchestrator.domain.port.TimeTrackingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureMockMvc
class PayrollOrchestrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollMessagePublisher payrollMessagePublisher;

    @MockBean
    private FlowMetrics flowMetrics;

    @MockBean
    private CompanyProfileProvider companyProfileProvider;

    @MockBean
    private EmployeeProfileProvider employeeProfileProvider;

    @MockBean
    private TimeTrackingProvider timeTrackingProvider;

    @BeforeEach
    void resetMocks() {
        reset(payrollMessagePublisher, flowMetrics, companyProfileProvider, employeeProfileProvider, timeTrackingProvider);
        when(flowMetrics.startFlow()).thenReturn(null);
        when(companyProfileProvider.fetchByEmployeeId(any())).thenReturn(defaultCompanyProfile());
        when(employeeProfileProvider.fetchByEmployeeId(any(), any())).thenReturn(defaultEmployeeProfile());
        when(timeTrackingProvider.fetchByPayrollRequest(any(), any())).thenReturn(defaultTimeTrackingSummary());
    }

    @Test
    void shouldOrchestrateAndPublishMessage() throws Exception {
        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                        .header("X-Idempotency-Key", "idem-success")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "emp-001",
                                  "payrollPeriod": "2026-03",
                                  "baseSalary": 8500.00,
                                  "requestedBy": "payroll-bff",
                                  "timeTracking": {
                                    "workedHours": 168,
                                    "overtimeHours": 10,
                                    "absenceHours": 2,
                                    "overtimeHourlyRate": 45.00,
                                    "absenceHourlyRate": 38.00
                                  }
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ORCHESTRATED"))
                .andExpect(jsonPath("$.reusedResult").value(false));

        verify(payrollMessagePublisher, times(1)).publish(any(PayrollPayloadMessage.class));
    }

    @Test
    void shouldReuseResultWhenIdempotencyKeyIsRepeated() throws Exception {
        String payload = """
                {
                  "employeeId": "emp-001",
                  "payrollPeriod": "2026-03",
                  "baseSalary": 8500.00,
                  "requestedBy": "payroll-bff",
                  "timeTracking": {
                    "workedHours": 168,
                    "overtimeHours": 10,
                    "absenceHours": 2,
                    "overtimeHourlyRate": 45.00,
                    "absenceHourlyRate": 38.00
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                        .header("X-Idempotency-Key", "idem-reuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.reusedResult").value(false));

        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                        .header("X-Idempotency-Key", "idem-reuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.reusedResult").value(true));

        verify(payrollMessagePublisher, times(1)).publish(any(PayrollPayloadMessage.class));
    }

    @Test
    void shouldUseAlternativeCompanyTaxRateAndStillPublish() throws Exception {
        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                        .header("X-Idempotency-Key", "idem-company-tax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "emp-003",
                                  "payrollPeriod": "2026-03",
                                  "baseSalary": 6200.00,
                                  "requestedBy": "payroll-bff",
                                  "timeTracking": {
                                    "workedHours": 162,
                                    "overtimeHours": 6,
                                    "absenceHours": 5,
                                    "overtimeHourlyRate": 42.00,
                                    "absenceHourlyRate": 35.00
                                  }
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ORCHESTRATED"))
                .andExpect(jsonPath("$.reusedResult").value(false));

        verify(payrollMessagePublisher, times(1)).publish(any(PayrollPayloadMessage.class));
    }

    @Test
    void shouldReturnNotFoundWhenEmployeeDoesNotExist() throws Exception {
        when(employeeProfileProvider.fetchByEmployeeId(any(), any()))
                .thenThrow(new NotFoundException("Colaborador não encontrado para o identificador informado"));

        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "unknown",
                                  "payrollPeriod": "2026-03",
                                  "baseSalary": 6200.00,
                                  "requestedBy": "payroll-bff",
                                  "timeTracking": {
                                    "workedHours": 160,
                                    "overtimeHours": 4,
                                    "absenceHours": 3,
                                    "overtimeHourlyRate": 40.00,
                                    "absenceHourlyRate": 35.00
                                  }
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details[0]", containsString("Colaborador não encontrado")));
    }

    @Test
    void shouldReturnServiceUnavailableWhenPublisherFails() throws Exception {
        doThrow(new IntegrationException("Falha ao publicar payload consolidado no broker RabbitMQ"))
                .when(payrollMessagePublisher).publish(any(PayrollPayloadMessage.class));

        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                        .header("X-Idempotency-Key", "idem-publisher-failure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "emp-002",
                                  "payrollPeriod": "2026-03",
                                  "baseSalary": 7000.00,
                                  "requestedBy": "payroll-bff",
                                  "timeTracking": {
                                    "workedHours": 160,
                                    "overtimeHours": 4,
                                    "absenceHours": 0,
                                    "overtimeHourlyRate": 40.00,
                                    "absenceHourlyRate": 35.00
                                  }
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.details[0]", containsString("Falha ao publicar payload consolidado no broker RabbitMQ")));

        verify(payrollMessagePublisher, times(1)).publish(any(PayrollPayloadMessage.class));
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "",
                                  "payrollPeriod": "2026-03",
                                  "baseSalary": 0,
                                  "requestedBy": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    private EmployeeProfile defaultEmployeeProfile() {
        return EmployeeProfile.builder()
                .employeeId("emp-001")
                .employeeName("Ana Souza")
                .documentNumber("12345678900")
                .department("Financeiro")
                .costCenter("FIN-001")
                .email("ana.souza@empresa.com")
                .build();
    }

    private CompanyProfile defaultCompanyProfile() {
        return CompanyProfile.builder()
                .companyId("COMP-001")
                .companyName("Acme Indicadores")
                .registrationNumber("12345678000199")
                .businessUnit("Unidade Central")
                .payrollCalendar("MENSAL")
                .defaultTaxRate(new BigDecimal("0.12"))
                .build();
    }

    private TimeTrackingSummary defaultTimeTrackingSummary() {
        return TimeTrackingSummary.builder()
                .workedHours(new BigDecimal("168"))
                .overtimeHours(new BigDecimal("10"))
                .absenceHours(new BigDecimal("2"))
                .overtimeHourlyRate(new BigDecimal("45.00"))
                .absenceHourlyRate(new BigDecimal("38.00"))
                .build();
    }
}
