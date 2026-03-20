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

        synchronized (cache) {
            existing = cache.get(key);
            if (existing != null) {
                return existing.toBuilder().reusedResult(true).build();
            }

            OrchestrationResult result = supplier.get();
            cache.put(key, result.toBuilder().reusedResult(false).build());
            return result;
        }
    }

    @Override
    public Optional<OrchestrationResult> findByKey(String key) {
        return Optional.ofNullable(cache.get(key));
    }
}
