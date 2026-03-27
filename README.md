# sboot-payroll-orchestrator-service

Serviço Spring Boot 3 / Java 21 responsável por orquestrar a consolidação entre os componentes de ponto eletrônico, dados da empresa, dados do empregado e cálculo da folha, publicando o payload final no RabbitMQ para os consumidores downstream.

## Arquitetura

- **Hexagonal/Ports & Adapters**: separação entre domínio, aplicação e adapters.
- **Pipeline desacoplado por etapas**: `FlowStep` permite evoluir o fluxo sem acoplamento rígido.
- **Idempotência**: cache em memória para evitar reprocessamento do mesmo `X-Idempotency-Key`.
- **Resiliência**: Retry, Circuit Breaker e Bulkhead com Resilience4j nas integrações.
- **Observabilidade**: métricas customizadas com Micrometer + Prometheus e Actuator.
- **Mensageria**: publicação do payload consolidado no exchange `payroll.exchange` com routing key `payroll.orchestrated`.

## Fluxo orquestrado

1. Consulta apontamentos no `sboot-time-tracking-integration-service` (ponto eletrônico externo, ex.: Secullum).
2. Consulta dados da empresa no `sboot-data-company-service`.
3. Consulta dados do empregado no `sboot-data-employe-service`.
4. Solicita o cálculo tributário ao `sboot-payroll-calculation-service`, usando também os dados de ponto.
5. Consolida o payload final e publica o evento para consumo assíncrono.

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
