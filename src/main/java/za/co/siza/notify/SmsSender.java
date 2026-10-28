package za.co.siza.notify;

/**
 * A rule (interface) for sending SMS messages.
 * 
 * Right now, we only have MockSmsSender (which prints to the console). 
 * When we are ready to use a real service like Africa's Talking or Clickatell, 
 * we just create a new class that follows this rule, and the rest of the app 
 * won't even know the difference.
 */
public interface SmsSender {
    
    // Any class that implements this interface MUST have this method.
    void send(SmsMessage message);
}
