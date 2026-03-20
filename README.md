# sboot-payroll-orchestrator-service

Serviço Spring Boot 3 / Java 21 responsável por orquestrar o fluxo de enriquecimento de dados da folha, executar o cálculo final e publicar o payload consolidado no RabbitMQ para o componente gerador de PDF.

## Arquitetura

- **Hexagonal/Ports & Adapters**: separação entre domínio, aplicação e adapters.
- **Pipeline desacoplado por etapas**: `FlowStep` permite evoluir o fluxo sem acoplamento rígido.
- **Idempotência**: cache em memória para evitar reprocessamento do mesmo `X-Idempotency-Key`.
- **Resiliência**: Retry, Circuit Breaker e Bulkhead com Resilience4j nas integrações.
- **Observabilidade**: métricas customizadas com Micrometer + Prometheus e Actuator.
- **Mensageria**: publicação do payload consolidado no exchange `payroll.exchange` com routing key `payroll.pdf.generate`.

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
