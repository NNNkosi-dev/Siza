package za.co.siza.ussd;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import za.co.siza.alert.AlertFactory;
import za.co.siza.alert.AlertService;
import za.co.siza.domain.Alert;
import za.co.siza.notify.MockSmsSender;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Exposes the USSD state machine over HTTP.
 *
 * Accepts POSTs on /ussd with either:
 *   - Content-Type: application/x-www-form-urlencoded
 *     body: sessionId=...&phoneNumber=...&text=...
 *   - Content-Type: application/json
 *     body: {"sessionId":"...","phoneNumber":"...","text":"..."}
 *
 * Responds with text/plain: the next USSD screen. This is the format
 * aggregators (Africa's Talking, Infobip) expect for the callback.
 */
public final class UssdServer {

    private static final int PORT = 8080;
    private static final Gson GSON = new Gson();

    private final SessionStore sessions = new SessionStore();
    private final MenuFlow flow = new MenuFlow();
    private final MenuRenderer renderer = new MenuRenderer();
    private final AlertService alertService = new AlertService(new MockSmsSender());

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/ussd", this::handleUssd);
        server.setExecutor(null);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Siza USSD server...");
            server.stop(0);
        }));

        System.out.println("Siza USSD server listening on http://localhost:" + PORT);
        System.out.println("POST /ussd with form-encoded or JSON body.");
    }

    private void handleUssd(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "Method not allowed");
            return;
        }

        String body = readBody(exchange);
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        UssdRequest request = parseRequest(body, contentType);

        if (request.sessionId().isBlank() || request.phoneNumber().isBlank()) {
            respond(exchange, 400, "Missing sessionId or phoneNumber");
            return;
        }

        UssdSession session = sessions.getOrCreate(request.sessionId(), request.phoneNumber());
        MenuState current = session.state();

        MenuState next;
        if (flow.expectsFreeText(current)) {
            session.recordLocation(request.text());
            next = flow.next(current, request.text());
        } else {
            next = flow.next(current, request.text());
        }

        session.advanceTo(next);

        if (flow.isTerminal(next)) {
            onSessionEnd(session, next);
            sessions.remove(request.sessionId());
        }

        respond(exchange, 200, renderer.render(next));
    }

    /**
     * Parses the request body as either JSON or form-encoded, depending on
     * the Content-Type header. If Content-Type is missing, tries form first.
     */
    private UssdRequest parseRequest(String body, String contentType) {
        if (contentType != null
            && contentType.toLowerCase().contains("application/json")) {
            try {
                UssdRequest parsed = GSON.fromJson(body, UssdRequest.class);
                return parsed != null ? parsed : new UssdRequest("", "", "");
            } catch (Exception e) {
                System.err.println("Bad JSON in /ussd request: " + e.getMessage());
                return new UssdRequest("", "", "");
            }
        }
        Map<String, String> params = parseForm(body);
        return new UssdRequest(
            params.getOrDefault("sessionId", ""),
            params.getOrDefault("phoneNumber", ""),
            params.getOrDefault("text", "")
        );
    }

    private void onSessionEnd(UssdSession session, MenuState terminalState) {
        System.out.println("[SESSION END] phone=" + session.phoneNumber()
            + " location=" + session.locationText()
            + " terminalState=" + terminalState);
        Alert alert = AlertFactory.fromUssdSession(session, terminalState);
        alertService.dispatch(alert);
    }

    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private Map<String, String> parseForm(String body) {
        Map<String, String> out = new HashMap<>();
        if (body == null || body.isEmpty()) return out;
        for (String pair : body.split("&")) {
            int i = pair.indexOf('=');
            if (i > 0) {
                out.put(decode(pair.substring(0, i)), decode(pair.substring(i + 1)));
            }
        }
        return out;
    }

    private String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void main(String[] args) throws IOException {
        new UssdServer().start();
    }
}
