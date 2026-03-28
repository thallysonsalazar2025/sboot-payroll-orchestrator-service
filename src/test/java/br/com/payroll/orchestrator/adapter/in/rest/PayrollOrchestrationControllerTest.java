package br.com.payroll.orchestrator.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.payroll.orchestrator.adapter.in.rest.mapper.PayrollRestMapper;
import br.com.payroll.orchestrator.application.service.PayrollOrchestrationService;
import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PayrollOrchestrationController.class)
class PayrollOrchestrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollOrchestrationService service;

    @MockBean
    private PayrollRestMapper mapper;

    @Test
    void shouldReturnAcceptedWhenRequestIsValid() throws Exception {
        String json = """
                {
                    "employeeId": "emp-001",
                    "payrollPeriod": "2023-12",
                    "baseSalary": 5000,
                    "requestedBy": "user-test"
                }
                """;

        when(mapper.resolveIdempotencyKey(any(), any())).thenReturn("uuid");
        when(service.orchestrate(any(), any())).thenReturn(OrchestrationResult.builder().status("OK").build());

        mockMvc.perform(post("/api/v1/payroll-orchestrations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isAccepted());
    }
}