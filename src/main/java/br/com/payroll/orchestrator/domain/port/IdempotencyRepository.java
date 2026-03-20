package br.com.payroll.orchestrator.domain.port;

import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import java.util.Optional;
import java.util.function.Supplier;

public interface IdempotencyRepository {
    OrchestrationResult executeOnce(String key, Supplier<OrchestrationResult> supplier);

    Optional<OrchestrationResult> findByKey(String key);
}
