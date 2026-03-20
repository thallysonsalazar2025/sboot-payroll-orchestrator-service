package br.com.payroll.orchestrator.adapter.out.client;

import br.com.payroll.orchestrator.domain.exception.NotFoundException;
import br.com.payroll.orchestrator.domain.model.EmployeeProfile;
import br.com.payroll.orchestrator.domain.port.EmployeeProfileProvider;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryEmployeeProfileProvider implements EmployeeProfileProvider {

    private static final Map<String, EmployeeProfile> EMPLOYEES = Map.of(
            "emp-001", EmployeeProfile.builder()
                    .employeeId("emp-001")
                    .employeeName("Ana Souza")
                    .documentNumber("12345678900")
                    .department("Financeiro")
                    .costCenter("FIN-001")
                    .email("ana.souza@empresa.com")
                    .build(),
            "emp-002", EmployeeProfile.builder()
                    .employeeId("emp-002")
                    .employeeName("Carlos Lima")
                    .documentNumber("98765432100")
                    .department("Tecnologia")
                    .costCenter("TEC-010")
                    .email("carlos.lima@empresa.com")
                    .build(),
            "benefits-down", EmployeeProfile.builder()
                    .employeeId("benefits-down")
                    .employeeName("Maria Fallback")
                    .documentNumber("11122233344")
                    .department("Operações")
                    .costCenter("OPS-008")
                    .email("maria.fallback@empresa.com")
                    .build());

    @Override
    @Retry(name = "employeeProfileProvider")
    @CircuitBreaker(name = "employeeProfileProvider")
    @Bulkhead(name = "employeeProfileProvider")
    public EmployeeProfile fetchByEmployeeId(String employeeId) {
        log.info("Buscando dados cadastrais do colaborador {}", employeeId);
        EmployeeProfile profile = EMPLOYEES.get(employeeId);
        if (profile == null) {
            throw new NotFoundException("Colaborador não encontrado para o identificador informado");
        }
        return profile;
    }
}
