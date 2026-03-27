package org.flipkart.circuitbreaker;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Replays a scripted sequence of events through a shared WebClient,
 * so circuit-breaker state accumulates across events exactly as it
 * would in production.
 *
 * Time is driven by TestClock — fully deterministic, no Thread.sleep().
 */
public class Simulation {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     Circuit Breaker Simulation           ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        // ── per-service config ────────────────────────────────────────────
        Map<String, CircuitBreakerConfig> configs = Map.of(

            "SVC_B", CircuitBreakerConfig.builder()
                         .failureThreshold(3)
                         .windowMinutes(10)
                         .cooldownMinutes(5)
                         .build(),

            "SVC_C", CircuitBreakerConfig.builder()
                         .failureThreshold(10)
                         .windowMinutes(5)
                         .cooldownMinutes(2)
                         .build()
        );

        // ── shared clock + client ─────────────────────────────────────────
        TestClock    clock = new TestClock(Instant.EPOCH);
        AtomicInteger sc   = new AtomicInteger(200);
        WebClient client   = new WebClient(
            req -> new Response(sc.get(), "body"), configs, clock);

        // ── event script ──────────────────────────────────────────────────
        record Event(String service, int status, int offsetMinutes) {}

        List<Event> events = List.of(
            new Event("SVC_B", 200,  0),   // success — CLOSED
            new Event("SVC_B", 500,  2),   // failure 1
            new Event("SVC_B", 500,  4),   // failure 2
            new Event("SVC_B", 500,  6),   // failure 3 → OPEN
            new Event("SVC_B", 200,  8),   // BLOCKED (2 min elapsed, need 5)
            new Event("SVC_C", 500, 10),   // SVC_C independent — its own breaker
            new Event("SVC_B", 200, 11),   // BLOCKED (5 min not yet elapsed)
            new Event("SVC_B", 200, 12),   // CLOSED again ✅ (6 min elapsed)
            new Event("SVC_B", 200, 13)    // normal ✅
        );

        for (Event event : events) {
            clock.advanceTo((long) event.offsetMinutes() * 60_000L);
            sc.set(event.status());

            String prefix = String.format("T+%-3d min  %-6s  status=%-3d",
                event.offsetMinutes(), event.service(), event.status());

            try {
                Response r = client.execute(new Request(event.service(), "/api"));
                System.out.printf("%s  → %-8s | circuit=%s%n",
                    prefix,
                    r.isSuccess() ? "✓ success" : "✗ failure",
                    client.stateFor(event.service()));
            } catch (CircuitOpenException e) {
                System.out.printf("%s  → BLOCKED  | circuit=%s%n",
                    prefix, client.stateFor(event.service()));
            }
        }

        // ── stale failure scenario ────────────────────────────────────────
        System.out.println("\n── Stale failure eviction scenario ──");
        TestClock    c2 = new TestClock(Instant.EPOCH);
        AtomicInteger s2 = new AtomicInteger(500);
        WebClient    w2 = new WebClient(
            req -> new Response(s2.get(), "body"), configs, c2);

        executeQuiet(w2, "SVC_B", s2, 500);   // fail 1 at T+0
        executeQuiet(w2, "SVC_B", s2, 500);   // fail 2 at T+0
        c2.advance(11 * 60_000L);              // jump to T+11m — both stale
        executeQuiet(w2, "SVC_B", s2, 500);   // fail 1 (fresh) — NOT tripped
        System.out.printf("  SVC_B after stale eviction = %s  (expected CLOSED)%n",
            w2.stateFor("SVC_B"));

        System.out.println("\n═══ End of simulation ═══");
    }

    private static void executeQuiet(WebClient w, String svc,
                                     AtomicInteger sc, int status) {
        sc.set(status);
        try { w.execute(new Request(svc, "/api")); }
        catch (CircuitOpenException ignored) { }
    }
}
