package za.co.siza.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Logs messages to stdout and keeps an in-memory record for tests.
 * Use in dev and test. Never in production.
 */
public final class MockSmsSender implements SmsSender {

    private final List<SmsMessage> sent = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void send(SmsMessage message) {
        sent.add(message);
        System.out.println("[SMS] to=" + message.toPhoneNumber()
            + " body=\"" + message.body() + "\"");
    }

    public List<SmsMessage> sentMessages() {
        return List.copyOf(sent);
    }

    public void clear() {
        sent.clear();
    }
}
