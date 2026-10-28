package za.co.siza.domain;

/**
 * How serious the alert is. This helps decide how the message 
 * should be worded and how fast someone should react to it.
 */
public enum Severity {
    
    IMMEDIATE, // Life-threatening. Needs a response right away.
    URGENT,    // Very concerning. Needs a response soon.
    CONCERN    // Just a log or a heads-up. No immediate emergency.
}
