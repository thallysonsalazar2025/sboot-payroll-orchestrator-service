package br.com.payroll.orchestrator.adapter.out.persistence;

import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.port.IdempotencyRepository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class InMemoryIdempotencyRepository implements IdempotencyRepository {

    private final ConcurrentMap<String, OrchestrationResult> cache = new ConcurrentHashMap<>();

    @Override
    public OrchestrationResult executeOnce(String key, Supplier<OrchestrationResult> supplier) {
        OrchestrationResult existing = cache.get(key);
        if (existing != null) {
            return existing.toBuilder().reusedResult(true).build();
        }

        // computeIfAbsent garante execução atômica por chave sem travar o mapa inteiro

        // Nota: O computeIfAbsent retorna o valor atual. Se ele acabou de ser criado,
        // reusedResult será false (conforme o supplier). Se já existia, será false (do cache).
        // Para este mock, se quisermos ser precisos, teríamos que verificar se o objeto existia antes.
        return cache.computeIfAbsent(key, k -> {
            OrchestrationResult executionResult = supplier.get();
            return executionResult.toBuilder().reusedResult(false).build();
        });
    }

    @Override
    public Optional<OrchestrationResult> findByKey(String key) {
        return Optional.ofNullable(cache.get(key));
    }
}
