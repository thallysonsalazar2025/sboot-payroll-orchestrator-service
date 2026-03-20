package br.com.payroll.orchestrator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI payrollOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payroll Orchestrator Service")
                        .description("Serviço responsável por orquestrar enriquecimento, cálculo e publicação para geração de PDF de folha.")
                        .version("v1")
                        .contact(new Contact().name("Payroll Platform Team").email("payroll-platform@empresa.com"))
                        .license(new License().name("Apache 2.0")));
    }
}
