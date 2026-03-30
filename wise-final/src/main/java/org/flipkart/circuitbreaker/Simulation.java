package org.flipkart.circuitbreaker;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Replays a scripted sequence of events through a shared WebClient.
 * State accumulates across events — exactly as in production.
 * Time driven by TestClock — fully deterministic.
 */
public class Simulation {

    public static void main(String[] args) {

        header("Circuit Breaker Simulation — Wise London");

        TestClock    clock  = new TestClock(Instant.EPOCH);
        AtomicInteger sc    = new AtomicInteger(200);
        WebClient    client = new WebClient(
            req -> new Response(sc.get(), "body"), clock);

        // Per-service config
        client.registerConfig("SVC_B", CircuitBreakerConfig.builder()
            .failureThreshold(3).windowMinutes(10).cooldownMinutes(5).build());
        client.registerConfig("SVC_C", CircuitBreakerConfig.builder()
            .failureThreshold(10).windowMinutes(5).cooldownMinutes(2).build());

        // ── Scenario 1: Trip SVC_B ────────────────────────────────────────
        section("1. Three failures → OPEN");
        fire(client, "SVC_B", sc, 500, clock);   // fail 1
        fire(client, "SVC_B", sc, 500, clock);   // fail 2
        fire(client, "SVC_B", sc, 500, clock);   // fail 3 → OPEN

        // ── Scenario 2: Blocked ───────────────────────────────────────────
        section("2. Requests blocked while OPEN");
        fire(client, "SVC_B", sc, 200, clock);   // BLOCKED
        fire(client, "SVC_B", sc, 200, clock);   // BLOCKED

        // ── Scenario 3: Service isolation ────────────────────────────────
        section("3. SVC_C unaffected — per-service isolation");
        fire(client, "SVC_C", sc, 200, clock);   // ✅ own breaker

        // ── Scenario 4: Cooldown → HALF_OPEN → CLOSED ────────────────────
        section("4. Cooldown expires → HALF_OPEN → probe succeeds → CLOSED");
        clock.advance(5 * 60_000L + 1);
        fire(client, "SVC_B", sc, 200, clock);   // probe ✅ → CLOSED
        fire(client, "SVC_B", sc, 200, clock);   // normal ✅

        // ── Scenario 5: Failed probe → re-open ───────────────────────────
        section("5. Probe fails → re-OPEN with fresh cooldown");
        fire(client, "SVC_B", sc, 500, clock);   // fail 1
        fire(client, "SVC_B", sc, 500, clock);   // fail 2
        fire(client, "SVC_B", sc, 500, clock);   // fail 3 → OPEN
        clock.advance(5 * 60_000L + 1);
        fire(client, "SVC_B", sc, 500, clock);   // probe fails → OPEN again
        fire(client, "SVC_B", sc, 200, clock);   // BLOCKED
        clock.advance(5 * 60_000L + 1);
        fire(client, "SVC_B", sc, 200, clock);   // probe succeeds → CLOSED

        // ── Scenario 6: Stale failures evicted ───────────────────────────
        section("6. Stale failures evicted — does NOT trip");
        fire(client, "SVC_B", sc, 500, clock);   // fail 1 at current T
        fire(client, "SVC_B", sc, 500, clock);   // fail 2
        clock.advance(11 * 60_000L);              // both now stale
        fire(client, "SVC_B", sc, 500, clock);   // only 1 fresh → NOT tripped
        System.out.println("  SVC_B = " + client.stateFor("SVC_B")
            + "  (expected CLOSED ✅)");

        // ── Scenario 7: 4xx does not trip ────────────────────────────────
        section("7. 4xx responses do NOT trip the breaker");
        fire(client, "SVC_B", sc, 400, clock);
        fire(client, "SVC_B", sc, 404, clock);
        fire(client, "SVC_B", sc, 422, clock);
        System.out.println("  SVC_B = " + client.stateFor("SVC_B")
            + "  (expected CLOSED ✅)");

        // ── Scenario 8: Unknown service → default config ──────────────────
        section("8. Unknown service gets DEFAULT_CONFIG");
        fire(client, "SVC_X", sc, 200, clock);
        System.out.println("  SVC_X = " + client.stateFor("SVC_X")
            + "  (expected CLOSED ✅)");

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("  Simulation complete");
        System.out.println("═══════════════════════════════════════════════");
    }

    private static void fire(WebClient client, String svc,
                             AtomicInteger sc, int status, TestClock clock) {
        sc.set(status);
        try {
            Response r = client.execute(new Request(svc, "/api"));
            System.out.printf("  %-6s  %d  → %-9s | %s%n",
                svc, status,
                r.isSuccess() ? "✓ success" : "✗ failure",
                client.stateFor(svc));
        } catch (ServiceUnavailableException e) {
            System.out.printf("  %-6s  %d  → BLOCKED   | %s%n",
                svc, status, client.stateFor(svc));
        }
    }

    private static void header(String title) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.printf( "║  %-44s║%n", title);
        System.out.println("╚══════════════════════════════════════════════╝\n");
    }

    private static void section(String title) {
        System.out.printf("%n── %s ──%n", title);
    }
}
