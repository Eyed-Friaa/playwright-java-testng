package com.framework.hooks;

import lombok.extern.slf4j.Slf4j;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Splits test methods across parallel CI runners deterministically.
 *
 * Activated by system properties:
 *   -Dshard.index=<0-based index>  -Dshard.total=<number of shards>
 *
 * When {@code shard.total} is unset or ≤ 1 the interceptor is a no-op, so
 * local runs and un-sharded CI jobs keep working unchanged.
 *
 * Assignment is by stable hash of the fully-qualified method name — same
 * method always lands on the same shard, so re-running a specific shard
 * exercises the same tests across runs (useful for reproducing flakes).
 */
@Slf4j
public class ShardInterceptor implements IMethodInterceptor {

    private static final String INDEX_PROP = "shard.index";
    private static final String TOTAL_PROP = "shard.total";

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        int total = Integer.parseInt(System.getProperty(TOTAL_PROP, "1"));
        if (total <= 1) return methods;

        int index = Integer.parseInt(System.getProperty(INDEX_PROP, "0"));
        if (index < 0 || index >= total) {
            throw new IllegalArgumentException(
                    "shard.index=" + index + " out of range for shard.total=" + total);
        }

        List<IMethodInstance> kept = IntStream.range(0, methods.size())
                .filter(i -> shardOf(methods.get(i), total) == index)
                .mapToObj(methods::get)
                .toList();

        log.info("Shard {}/{}: {} of {} methods selected", index, total, kept.size(), methods.size());
        return kept;
    }

    private static int shardOf(IMethodInstance m, int total) {
        String key = m.getMethod().getQualifiedName();
        // Math.floorMod handles negative hashCodes cleanly.
        return Math.floorMod(key.hashCode(), total);
    }
}
