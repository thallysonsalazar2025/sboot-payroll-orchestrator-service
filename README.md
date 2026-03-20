# sboot-payroll-orchestrator-service

Serviço Spring Boot 3 / Java 21 responsável por orquestrar a consolidação entre os componentes de dados da empresa, dados do empregado e cálculo da folha, publicando o payload final no RabbitMQ para os consumidores downstream.

## Arquitetura

- **Hexagonal/Ports & Adapters**: separação entre domínio, aplicação e adapters.
- **Pipeline desacoplado por etapas**: `FlowStep` permite evoluir o fluxo sem acoplamento rígido.
- **Idempotência**: cache em memória para evitar reprocessamento do mesmo `X-Idempotency-Key`.
- **Resiliência**: Retry, Circuit Breaker e Bulkhead com Resilience4j nas integrações.
- **Observabilidade**: métricas customizadas com Micrometer + Prometheus e Actuator.
- **Mensageria**: publicação do payload consolidado no exchange `payroll.exchange` com routing key `payroll.orchestrated`.

## Fluxo orquestrado

1. Consulta dados da empresa no componente `sboot-data-company-service`.
2. Consulta dados do empregado no componente `sboot-data-employe-service`.
3. Solicita o cálculo tributário ao componente `sboot-payroll-calculation-service`.
4. Consolida o payload final e publica o evento para consumo assíncrono.

## Principais endpoints

- `POST /api/v1/payroll-orchestrations`
- `GET /actuator/health`
- `GET /actuator/prometheus`
- `GET /swagger-ui.html`

## Contrato

O contrato OpenAPI 3.0 está em [`openapi.yaml`](openapi.yaml).

## Executando localmente

```bash
mvn spring-boot:run
```

## Testes

```bash
mvn test
```
