package org.flipkart.circuitbreaker;

import org.flipkart.circuitbreaker.client.WebClient;
import org.flipkart.circuitbreaker.exceptions.CircuitOpenException;
import org.flipkart.circuitbreaker.model.Request;
import org.flipkart.circuitbreaker.model.Response;
import org.flipkart.circuitbreaker.test.WebClientCircuitBreakerTest;

import java.time.Instant;
import java.util.List;

/**
 * Replays a sequence of (service, statusCode, offsetMinutes) events through a
 * SHARED WebClient so circuit-breaker state accumulates across events.
 *
 * Time is driven by a TestClock advanced per event — no wall-clock dependency,
 * no Thread.sleep(), fully deterministic.
 *
 * Input format (per event):  service  statusCode  offsetMinutes
 * Example:                   ServiceB 500         2
 */
public class Simulation {

    public static void main(String[] args) {

        System.out.println("=== Circuit Breaker Simulation ===\n");

        /*
         * Scenario (matches the problem statement):
         *
         *   T+ 0 min  ServiceB 200  → success,  CLOSED
         *   T+ 2 min  ServiceB 500  → failure 1, CLOSED
         *   T+ 4 min  ServiceB 500  → failure 2, CLOSED
         *   T+ 6 min  ServiceB 500  → failure 3 → OPEN
         *   T+ 8 min  ServiceB 200  → BLOCKED   (only 2 min elapsed, cooldown = 5 min)
         *   T+10 min  ServiceC 500  → ServiceC failure #1 (ServiceB still OPEN)
         *   T+11 min  ServiceB 200  → BLOCKED   (only 5 min elapsed, need > 5 min)
         *   T+12 min  ServiceB 200  → HALF_OPEN trial → success → CLOSED
         *   T+13 min  ServiceB 200  → success,  CLOSED (normal again)
         */
        List<Event> events = List.of(
            new Event("ServiceB", 200,  0),
            new Event("ServiceB", 500,  2),
            new Event("ServiceB", 500,  4),
            new Event("ServiceB", 500,  6),   // trips → OPEN
            new Event("ServiceB", 200,  8),   // blocked (2 min in cooldown)
            new Event("ServiceC", 500, 10),   // ServiceC: independent breaker
            new Event("ServiceB", 200, 11),   // still blocked (5 min not elapsed)
            new Event("ServiceB", 200, 12),   // HALF_OPEN trial → success → CLOSED
            new Event("ServiceB", 200, 13)    // back to normal
        );

        // ── shared clock and client ───────────────────────────────────────
        // ONE client for the whole simulation so breaker state accumulates.
        WebClientCircuitBreakerTest.TestClock clock = new WebClientCircuitBreakerTest.TestClock(Instant.EPOCH);

        // Transport returns whatever status the current event specifies.
        // We swap the response via a one-element array (effectively a mutable cell).
        int[] currentStatus = { 200 };
        WebClient client = new WebClient(
            req -> new Response(currentStatus[0], "simulated"),
            clock
        );

        // ── replay ────────────────────────────────────────────────────────
        for (Event event : events) {
            clock.advanceTo(event.offsetMinutes * 60_000L);
            currentStatus[0] = event.statusCode;

            String prefix = String.format("T+%-3d min  %-10s status=%d",
                event.offsetMinutes, event.service, event.statusCode);

            try {
                Response response = client.execute(
                    new Request(event.service, "/api/resource", clock.millis()));

                String outcome = response.isSuccess() ? "✓ success" : "✗ failure";
                System.out.printf("%s  → %-10s | %-10s circuit=%s%n",
                    prefix, outcome, "", client.stateFor(event.service));

            } catch (CircuitOpenException e) {
                System.out.printf("%s  → BLOCKED             | circuit=%s%n",
                    prefix, client.stateFor(event.service));
            }
        }

        System.out.println("\n=== End of simulation ===");
    }

    // ── event record ─────────────────────────────────────────────────────────

    record Event(String service, int statusCode, int offsetMinutes) {}
}
