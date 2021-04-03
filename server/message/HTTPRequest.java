package server.message;

import java.util.HashMap;
import java.util.Optional;

/**
 * HTTPRequest
 */
public class HTTPRequest {
    private final String[] knownHeaders = { "Host", "Connection", "Content-Length", "Content-Type" };

    private String method;
    private String url;
    private String version;
    private HashMap<String, String> headers;
    private String body;
    private boolean malformed;

    public HTTPRequest() {
        headers = new HashMap<>();
        this.body = "";
    }

    /**
     * Parse a given HTTP request line and update the relative fields.
     * 
     * @param line the HTTP request line or header line
     * @return true on success, false otherwise.
     */
    public boolean parseRequestLine(final String line) {
        String[] parsedLine = line.split(" ");
        if (parsedLine.length != 3) {
            malformed = true;
            return false;
        }
        method = parsedLine[0];
        url = parsedLine[1];
        version = parsedLine[2];
        return true;
    }

    public boolean isMalformed() {
        return malformed;
    }

    /**
     * Parse a given HTTP or header line and update the relative fields.
     * 
     * @param line the HTTP request line or header line
     * @return true on success, false otherwise.
     */
    public boolean parseAndComputeLine(final String line) {
        String[] parsedLine = line.split(": ");

        if (parsedLine.length < 2) {
            malformed = true;
            return false;
        }

        if (isKnownHeader(parsedLine[0])) {
            setHeader(parsedLine[0], parsedLine[1]);
        } else {
            // System.out.println(parsedLine[0] + " Unknown Input");
        }
        return true;
    }

    /**
     * Check wheather the header is a known one to this implementation
     * 
     * @param header the header String
     * @return true if known, false otherwise
     */
    private boolean isKnownHeader(final String header) {
        for (String h : knownHeaders) {
            if (header.equals(h)) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldHaveBody() {
        return method.equals("POST") || method.equals("PUT");
    }
    /*
     * GET /home.html HTTP/1.0 Host: michelecattaneo.ch Connection: Keep-Alive
     * Content-Length: 10 Content-Type: applicatio/jonson
     * 
     * GET /home.html HTTP/1.1 Host: michelecattaneo.ch Connection: close
     * Content-Length: 10 Content-Type: applicatio/jonson
     * 
     * 
     */

    private void setHeader(String header, String value) {
        headers.put(header, value);
    }

    public String getMethod() {
        return method;
    }

    public String getVersion() {
        return this.version;
    }

    public String getUrl() {
        return this.url;
    }

    /**
     * Returns an optional String containing the value of the given header.
     * 
     * @param header the given header String.
     * @return An Optional<String> with the value if the header is present, an empty
     *         Optional otherwise.
     */
    public Optional<String> getHeaderValue(final String header) {
        return headers.get(header) == null ? Optional.empty() : Optional.of(headers.get(header));
    }

    /**
     * Append the given String to the body of this request.
     * 
     * @param line the line to append.
     */
    public void appendBody(String line) {
        if (body.length() > 0) {
            body += '\n';
        }
        body += line;
    }

    public void toStringMio() {
        System.out.println(method + " " + url + " " + version);

        System.out.println("Host: " + headers.get("Host"));
        System.out.println("Connection: " + headers.get("Connection"));
        System.out.println("Content-Length: " + headers.get("Content-Length"));
        System.out.println("Content-Type: " + headers.get("Content-Type"));
        System.out.println("");
        System.out.println(body);
    }

    /**
     * Check various contraints AFTER the request is fully parsed
     */
    public void checkValidity() {
        // check host is necessary and/or present
        if (version.equals("HTTP/1.1") && !getHeaderValue("Host").isPresent()) {
            malformed = true;
            return;
        }
        // check if version 1.0 and there is not a Content-Length header
        if ((method.equals("PUT") || method.equals("POST")) && version.equals("HTTP/1.0")
                && !getHeaderValue("Content-Length").isPresent()) {
            malformed = true;
            return;
        }
    }

    /**
     * Set the default host if we have a HTTP1.0, where the host is optional
     */
    public void setHostIfNull() {
        if (version.equals("HTTP/1.0") && getHeaderValue("Host").isEmpty()) {
            headers.put("Host", "michelecattaneo.ch"); // TODO: not hardcoded
        }

    }
}
