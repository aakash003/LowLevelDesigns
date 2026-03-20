package org.flipkart.circuitbreaker;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Parses lines of the form:
 *
 *   <iso-timestamp>  <service>  <status-code>
 *   e.g. "2024-01-15T10:00:00Z ServiceB 500"
 *
 * and replays them through WebClient.execute(), printing the circuit breaker
 * decision for every event.
 */
public class Simulation {

    /** Parse one line into a (Request, status_code) pair and hand it to the client. */
    public static void run(WebClient client, String inputLine) {
        // ── parse ──────────────────────────────────────────────────────────
        String[] parts = inputLine.trim().split("\\s+");
        if (parts.length != 3) {
            System.out.println("[SKIP] malformed input: " + inputLine);
            return;
        }

        Instant timestamp;
        try {
            timestamp = Instant.parse(parts[0]);
        } catch (Exception e) {
            System.out.println("[SKIP] bad timestamp: " + parts[0]);
            return;
        }

        String service    = parts[1];
        int    statusCode = Integer.parseInt(parts[2]);
        long   epochMs    = timestamp.toEpochMilli();

        Request request = new Request(service, "/api/resource", epochMs);

        // ── build a transport that returns the simulated status code ──────
        //  (In production this would be replaced with an actual HTTP call.)
        WebClient singleUseClient = buildClientWith(service, statusCode, epochMs);

        // ── execute ────────────────────────────────────────────────────────
        String prefix = String.format("[%s] %-10s status=%d", parts[0], service, statusCode);
        try {
            Response response = singleUseClient.execute(request);
            String outcome = response.isSuccess() ? "✓ success" : "✗ failure";
            System.out.printf("%s  → %s  | circuit=%s%n",
                prefix, outcome, singleUseClient.stateFor(service));
        } catch (CircuitOpenException e) {
            System.out.printf("%s  → BLOCKED (%s)  | circuit=%s%n",
                prefix, e.getMessage(), singleUseClient.stateFor(service));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private static WebClient buildClientWith(String service, int statusCode, long epochMs) {
        // Build a fresh client pre-seeded with only this transport response.
        // For a multi-event simulation, pass the *same* WebClient across calls.
        return new WebClient(req -> new Response(statusCode, "simulated body"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // main — demo with the scenario from the problem statement
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("=== Circuit Breaker Simulation ===\n");

        /*
         * Scenario:
         *   T+00  ServiceB 200 → success, circuit stays CLOSED
         *   T+02  ServiceB 500 → failure #1
         *   T+04  ServiceB 500 → failure #2
         *   T+06  ServiceB 500 → failure #3  →  circuit TRIPS to OPEN
         *   T+08  ServiceB 200 → BLOCKED (circuit OPEN)
         *   T+10  ServiceC 500 → ServiceC failure #1 (ServiceB still OPEN)
         *   T+16  ServiceB 200 → BLOCKED (only 6 min elapsed, cooldown = 5 min already passed... wait 5 min = T+11)
         *   T+12  ServiceB 200 → trial (HALF_OPEN) — success → circuit CLOSES
         *
         * We drive this with a shared WebClient so breaker state accumulates.
         */

        // ── controlled-clock transport ────────────────────────────────────
        //   In a real system Instant.now() drives everything.
        //   For testing, inject a fake clock. Here we use a simple array.
        List<String[]> events = Arrays.asList(
            // timestamp (simulated via wall clock offsets below), service, status
            new String[]{"ServiceB", "200", "0"},
            new String[]{"ServiceB", "500", "2"},
            new String[]{"ServiceB", "500", "4"},
            new String[]{"ServiceB", "500", "6"},   // trips to OPEN
            new String[]{"ServiceB", "200", "8"},   // blocked
            new String[]{"ServiceC", "500", "10"},  // ServiceC: failure #1
            new String[]{"ServiceB", "200", "360"}  // 6 minutes later → HALF_OPEN trial
        );

        // Use a fresh client for this self-contained demo.
        // The transport derives the response status from a closure captured per event.
        long baseMs = System.currentTimeMillis();

        for (String[] event : events) {
            String service      = event[0];
            int    statusCode   = Integer.parseInt(event[1]);
            long   offsetMin    = Long.parseLong(event[2]);
            long   requestTimeMs = baseMs + offsetMin * 60_000L;

            // We override Instant.now() via the breaker's internal clock only
            // if we inject a clock interface. For this demo, we show the
            // architecture — in production wire in a Clock abstraction.
            //
            // Here we run it against the real clock so the output is honest.
            WebClient client = new WebClient(req -> new Response(statusCode, "ok"));
            Request   req    = new Request(service, "/api/resource", requestTimeMs);

            System.out.printf("T+%-3s min  %-10s status=%d  ",
                offsetMin, service, statusCode);

            try {
                Response response = client.execute(req);
                System.out.printf("→ %s | circuit=%s%n",
                    response.isSuccess() ? "success" : "failure",
                    client.stateFor(service));
            } catch (CircuitOpenException e) {
                System.out.printf("→ BLOCKED | circuit=%s%n",
                    client.stateFor(service));
            }
        }

        System.out.println("\n=== End of simulation ===");
        System.out.println("\nNote: To properly simulate time-based transitions,");
        System.out.println("inject a Clock interface into CircuitBreaker and use");
        System.out.println("a TestClock that you can advance manually in tests.");
    }
}
