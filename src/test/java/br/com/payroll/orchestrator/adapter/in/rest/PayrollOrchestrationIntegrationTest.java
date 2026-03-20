package br.com.payroll.orchestrator.adapter.in.rest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import br.com.payroll.orchestrator.domain.model.PdfGenerationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PayrollOrchestrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void resetMocks() {
        reset(rabbitTemplate);
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
                                  "requestedBy": "payroll-bff"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.reusedResult").value(false));

        verify(rabbitTemplate, times(1)).convertAndSend(eq("payroll.exchange"), eq("payroll.pdf.generate"), any(PdfGenerationMessage.class));
    }

    @Test
    void shouldReuseResultWhenIdempotencyKeyIsRepeated() throws Exception {
        String payload = """
                {
                  "employeeId": "emp-001",
                  "payrollPeriod": "2026-03",
                  "baseSalary": 8500.00,
                  "requestedBy": "payroll-bff"
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

        verify(rabbitTemplate, times(1)).convertAndSend(eq("payroll.exchange"), eq("payroll.pdf.generate"), any(PdfGenerationMessage.class));
    }

    @Test
    void shouldApplyBenefitFallbackAndStillPublish() throws Exception {
        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                        .header("X-Idempotency-Key", "idem-fallback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "benefits-down",
                                  "payrollPeriod": "2026-03",
                                  "baseSalary": 6200.00,
                                  "requestedBy": "payroll-bff"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.reusedResult").value(false));
    }

    @Test
    void shouldReturnNotFoundWhenEmployeeDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "unknown",
                                  "payrollPeriod": "2026-03",
                                  "baseSalary": 6200.00,
                                  "requestedBy": "payroll-bff"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details[0]", containsString("Colaborador não encontrado")));
    }

    @Test
    void shouldReturnServiceUnavailableWhenPublisherFails() throws Exception {
        doThrow(new RuntimeException("broker unavailable"))
                .when(rabbitTemplate).convertAndSend(eq("payroll.exchange"), eq("payroll.pdf.generate"), any(PdfGenerationMessage.class));

        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                        .header("X-Idempotency-Key", "idem-publisher-failure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeId": "emp-002",
                                  "payrollPeriod": "2026-03",
                                  "baseSalary": 7000.00,
                                  "requestedBy": "payroll-bff"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.details[0]", containsString("Falha ao publicar payload no broker RabbitMQ")));
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
}
