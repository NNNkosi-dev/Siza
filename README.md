# Siza: USSD Emergency Alert Service

Siza is a small Java project that simulates how an emergency alert service could work using USSD and SMS.
The idea is simple: someone dials a USSD code, chooses an option from a menu, and the service creates an alert and sends it through an SMS service.
I built this mainly to learn how USSD and SMS gateways fit together. I also wanted to get more comfortable using Maven, JUnit, and Gson in the same project.

## What it does

* Runs a small HTTP server
* Accepts USSD-style requests
* Keeps track of a user's session
* Shows a menu with different options
* Lets the user choose things like:

  * Help now
  * Share location
  * Check in
* Creates an alert when the user finishes a flow
* Sends the alert through an `SmsSender` interface
* Uses a mock SMS sender for now

## How it works

A USSD gateway would normally send a request to the server when someone interacts with the USSD menu.
For this project, I can use `curl` instead.

The request contains:

* A session ID
* The user's phone number
* The text they entered

The server uses the session ID to find the user's current session. It then passes the input to the menu flow, which decides what should happen next.

The server sends the next menu back as plain text.
When the user reaches the end of the flow, an alert is created and passed to the SMS sender.

The endpoint accepts two types of requests:

* `application/x-www-form-urlencoded` - similar to what a USSD provider would send
* `application/json` - useful for testing locally

The menu text is loaded from:

```text
src/main/resources/menus.json
```

There are also hardcoded fallback menus if the file can't be loaded.

## Why USSD?

USSD instead of a smartphone app. The person using this might only have a basic phone with no data. USSD works on almost any phone with a signal.

Two ways to send a request. Real USSD providers send form-encoded requests, but JSON is easier to test with locally. The server accepts both so it doesn't matter which one shows up.

Menu text lives in a JSON file. The menu isn't baked into the Java code. It's loaded from a file when the app starts, so it can be changed without recompiling.

SMS is behind an interface. Right now the SMS sender just prints to the console, but it's written as an interface so a real SMS service can plug in later without changing the rest of the code.

The alert classes don't know about USSD or SMS. Alert, Severity, and Trigger only know about alerts. They don't import anything from the USSD or SMS side. That means if I ever swap USSD for something else, the alert code stays the same

## Project structure

```text
src/main/java/za/co/siza/

domain/
    Alert.java
    AlertType.java
    Severity.java
    Trigger.java

ussd/
    MenuFlow.java
    MenuRenderer.java
    MenuState.java
    UssdServer.java
    UssdSession.java
    SessionStore.java

alert/
    AlertService.java
    AlertFactory.java

notify/
    SmsSender.java
    MockSmsSender.java

```

The `domain` package contains the alert-related classes and doesn't know anything about HTTP or USSD.

The `SmsSender` is an interface, so the mock SMS sender can eventually be replaced with a real SMS provider without changing the rest of the alert code.

## How to run

You need:

* Java 17+
* Maven

Run the tests:

```bash
mvn -q test
```

Build the project:

```bash
mvn -q package
```

Run the JAR:

```bash
java -jar target/siza-0.1.0.jar
```

The server starts on:

```text
http://localhost:8080
```

## Try it with curl

### Form-encoded request

Start a new session:

```bash
curl -X POST http://localhost:8080/ussd \
  -d "sessionId=demo1&phoneNumber=%2B27821234567&text="
```

Then select an option:

```bash
curl -X POST http://localhost:8080/ussd \
  -d "sessionId=demo1&phoneNumber=%2B27821234567&text=1"
```

Keep using the same `sessionId` for the rest of the flow.

### JSON request

The repo includes a sample request file (`sample-request.json`). Send it with:

```bash
curl -X POST http://localhost:8080/ussd \
  -H "Content-Type: application/json" \
  -d @sample-request.json
```

## What this doesn't do yet

### No real SMS gateway

`MockSmsSender` just logs the alert to the console. A real implementation would be a new class that implements `SmsSender` needing no changes needed anywhere else.

### No database

Sessions are only stored in memory.
They expire after three minutes, and everything is lost when the application is restarted.

### No authentication

The `/ussd` endpoint doesn't have authentication yet.
Anyone who can reach the endpoint could send a request.

### Not production-ready

This is a learning project and hasn't been built with production deployment in mind.

## What I'd add next

* Add a real `SmsSender` using a service like Africa's Talking or Clickatell
* Add a health-check endpoint
* Replace `System.out.println` with proper logging
* Add more tests for the HTTP endpoints
* Add authentication
* Add some form of persistent session storage

## Tech

* Java 17
* Maven
* JUnit 5
* Gson
* `com.sun.net.httpserver.HttpServer`

## Why I built it

The main goal was to learn by building something small rather than just reading about USSD and SMS APIs.

There are still quite a few things missing, but the basic flow works locally. Here's the shape of it:

```text
USSD request
    ↓
Session
    ↓
Menu
    ↓
User selection
    ↓
Alert
    ↓
SmsSender
    ↓
Mock SMS
```
