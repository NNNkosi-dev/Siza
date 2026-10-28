package za.co.siza.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fake SMS sender that just prints to the console.
 * 
 * We use this for testing so we don't accidentally send real text messages 
 * while developing. It also keeps a list of everything it "sent" so the 
 * unit tests can look at it and check that the right messages were created.
 */
public final class MockSmsSender implements SmsSender {

    // We store the sent messages in a list so tests can check them later.
    // "synchronizedList" just means it's safe if multiple parts of the app 
    // try to add a message at the exact same time.
    private final List<SmsMessage> sent = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void send(SmsMessage message) {
        // Save it to our list...
        sent.add(message);
        
        // ...and print it to the screen so we can see it in the terminal.
        System.out.println("[SMS] to=" + message.toPhoneNumber()
            + " body=\"" + message.body() + "\"");
    }

    // This lets the tests ask: "What messages were sent?"
    public List<SmsMessage> sentMessages() {
        return List.copyOf(sent);
    }

    // This lets the tests clean up before starting a new test.
    public void clear() {
        sent.clear();
    }
}
