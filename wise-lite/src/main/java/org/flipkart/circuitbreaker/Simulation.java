package org.flipkart.circuitbreaker;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * End-to-end demo — shared WebClient, TestClock, 7 scenarios.
 * Run: mvn exec:java
 */
public class Simulation {

    public static void main(String[] args) {

        header("Circuit Breaker — OPEN/CLOSED — Wise Lite");

        TestClock     clock  = new TestClock(Instant.EPOCH);
        AtomicInteger sc     = new AtomicInteger(200);
        WebClient     client = new WebClient(
            req -> new Response(sc.get(), "body"), clock);

        client.registerConfig("SVC_B", new CircuitBreakerConfig(
            3, 10 * 60_000L, 5 * 60_000L));   // strict
        client.registerConfig("SVC_C", new CircuitBreakerConfig(
            10, 5 * 60_000L, 2 * 60_000L));   // lenient

        // ── 1. Trip SVC_B ─────────────────────────────────────
        section("1. Three failures → OPEN");
        fire(client, "SVC_B", sc, 500, clock);
        fire(client, "SVC_B", sc, 500, clock);
        fire(client, "SVC_B", sc, 500, clock);   // OPEN

        // ── 2. Blocked ────────────────────────────────────────
        section("2. Requests blocked while OPEN");
        fire(client, "SVC_B", sc, 200, clock);   // BLOCKED
        fire(client, "SVC_B", sc, 200, clock);   // BLOCKED

        // ── 3. Service isolation ──────────────────────────────
        section("3. SVC_C unaffected — service isolation");
        fire(client, "SVC_C", sc, 200, clock);   // ✅ own breaker

        // ── 4. Cooldown → CLOSED ──────────────────────────────
        section("4. Advance 5 min → cooldown done → CLOSED");
        clock.advance(5 * 60_000L + 1);
        fire(client, "SVC_B", sc, 200, clock);   // ✅ CLOSED
        fire(client, "SVC_B", sc, 200, clock);   // ✅ normal

        // ── 5. Stale failures evicted ─────────────────────────
        section("5. Stale failures evicted — does NOT trip");
        fire(client, "SVC_B", sc, 500, clock);   // fail 1
        fire(client, "SVC_B", sc, 500, clock);   // fail 2
        clock.advance(11 * 60_000L);              // both stale
        fire(client, "SVC_B", sc, 500, clock);   // 1 fresh only
        System.out.println("  SVC_B = " + client.stateFor("SVC_B")
            + "  (expected CLOSED ✅)");

        // ── 6. 4xx does not trip ──────────────────────────────
        section("6. 4xx does NOT trip — client error not server fault");
        fire(client, "SVC_B", sc, 400, clock);
        fire(client, "SVC_B", sc, 404, clock);
        fire(client, "SVC_B", sc, 422, clock);
        System.out.println("  SVC_B = " + client.stateFor("SVC_B")
            + "  (expected CLOSED ✅)");

        // ── 7. Unknown service → default config ───────────────
        section("7. Unknown service → DEFAULT_CONFIG auto-registered");
        fire(client, "SVC_X", sc, 200, clock);
        System.out.println("  SVC_X = " + client.stateFor("SVC_X")
            + "  (expected CLOSED ✅)");

        System.out.println("\n═══ End of simulation ═══");
    }

    static void fire(WebClient client, String svc,
                     AtomicInteger sc, int status, TestClock clock) {
        sc.set(status);
        try {
            Response r = client.execute(new Request(svc, "/api"));
            System.out.printf("  %-6s  %d  → %-9s | %s%n",
                svc, status,
                r.isSuccess() ? "✓ success" : "✗ failure",
                client.stateFor(svc));
        } catch (CircuitOpenException e) {
            System.out.printf("  %-6s  %d  → BLOCKED   | %s%n",
                svc, status, client.stateFor(svc));
        }
    }

    static void header(String t) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.printf( "║  %-40s║%n", t);
        System.out.println("╚══════════════════════════════════════════╝\n");
    }

    static void section(String t) {
        System.out.printf("%n── %s ──%n", t);
    }
}
