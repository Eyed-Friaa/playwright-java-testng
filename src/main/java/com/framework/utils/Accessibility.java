package com.framework.utils;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Accessibility scanning via axe-core, injected into the page at runtime.
 *
 * Usage from a page object or test:
 *   Accessibility.assertNoSeriousViolations(page);
 *
 * The default impact floor is "serious" — a common industry baseline that
 * blocks WCAG A/AA blockers without failing on cosmetic contrast nits.
 * Override with {@link #assertNoViolations(Page, String)} for stricter runs.
 *
 * <b>Network requirement.</b> axe-core is fetched from unpkg on first call
 * per page. In air-gapped CI, bundle a copy in test resources and switch
 * to page.addScriptTag(Path) instead.
 */
@Slf4j
public final class Accessibility {

    private static final String AXE_CDN =
            "https://unpkg.com/axe-core@4.10.0/axe.min.js";

    /** Impact levels axe-core reports, in ascending severity. */
    public enum Impact {
        MINOR, MODERATE, SERIOUS, CRITICAL;

        boolean atLeast(Impact floor) { return this.ordinal() >= floor.ordinal(); }

        static Impact of(String s) {
            return s == null ? MINOR : Impact.valueOf(s.toUpperCase());
        }
    }

    private Accessibility() {}

    public static void assertNoCriticalViolations(Page page) {
        assertNoViolationsAtOrAbove(page, Impact.CRITICAL);
    }

    public static void assertNoSeriousViolations(Page page) {
        assertNoViolationsAtOrAbove(page, Impact.SERIOUS);
    }

    /** Fails on any violation regardless of impact. Strictest mode. */
    public static void assertNoViolations(Page page, String scanName) {
        Object result = scan(page);
        attachRawResult(scanName, result);
        List<Map<String, Object>> violations = extractViolations(result);
        if (!violations.isEmpty()) {
            Assertions.fail("axe-core violations (%d) on '%s': %s"
                    .formatted(violations.size(), scanName, summarize(violations)));
        }
    }

    // ─── internals ────────────────────────────────────────────────────────────

    private static void assertNoViolationsAtOrAbove(Page page, Impact floor) {
        Object result = scan(page);
        attachRawResult("a11y-scan-" + page.url(), result);

        List<Map<String, Object>> filtered = extractViolations(result).stream()
                .filter(v -> Impact.of((String) v.get("impact")).atLeast(floor))
                .toList();

        if (!filtered.isEmpty()) {
            Assertions.fail("axe-core violations at or above %s (%d): %s"
                    .formatted(floor, filtered.size(), summarize(filtered)));
        }
    }

    private static Object scan(Page page) {
        page.addScriptTag(new Page.AddScriptTagOptions().setUrl(AXE_CDN));
        // axe.run resolves with { violations, passes, incomplete, inapplicable }.
        return page.evaluate("async () => await axe.run()");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> extractViolations(Object result) {
        if (!(result instanceof Map<?, ?> map)) return List.of();
        Object v = map.get("violations");
        return v instanceof List ? (List<Map<String, Object>>) v : List.of();
    }

    private static String summarize(List<Map<String, Object>> violations) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> v : violations) {
            sb.append("\n  [%s] %s — %s".formatted(
                    v.get("impact"), v.get("id"), v.get("description")));
        }
        return sb.toString();
    }

    private static void attachRawResult(String name, Object result) {
        Allure.addAttachment(name, "application/json",
                new ByteArrayInputStream(String.valueOf(result).getBytes(StandardCharsets.UTF_8)),
                "json");
    }
}
