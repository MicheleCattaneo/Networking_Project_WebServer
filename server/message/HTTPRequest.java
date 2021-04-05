package server.message;

import java.util.HashMap;
import java.util.Optional;

import server.Server;

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
    private final Server server;

    public HTTPRequest(Server server) {
        this.server = server;
        headers = new HashMap<>();
        this.body = "";
    }

    /**
     * Parse a given HTTP request line and update the relative fields.
     * 
     * @param line the HTTP request line
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

    /**
     * Check whether this instance of request is malformed. Malformed can be set
     * during the parsing or later using setMalformed if the malformity is found
     * while handling the response.
     * 
     * @return true if this is malformed, false otherwise.
     */
    public boolean isMalformed() {
        return malformed;
    }

    /**
     * Set the malformed flag of this request in case this is found to not follow
     * the http standards.
     */
    public void setMalformed() {
        this.malformed = true;
    }

    /**
     * Parse a given HTTP header line and update the relative fields.
     * 
     * @param line the HTTP header line
     * @return true on success, false otherwise.
     */
    public boolean parseAndComputeLine(final String line) {
        String[] parsedLine = line.split(": ");

        if (parsedLine.length < 2) {
            malformed = true;
            return false;
        }

        if (isKnownHeader(parsedLine[0])) {
            if (headers.get(parsedLine[0]) == null) {
                setHeader(parsedLine[0], parsedLine[1]);
            } else {
                // if there is a duplicate header in the request being parsed
                malformed = true;
                return false;
            }
        }
        return true;
    }

    /**
     * Check wheather the given header is a known one to this implementation.
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

    /**
     * Check whether this request had a method which requires to have a body.
     * 
     * @return true if it should, false otherwise.
     */
    public boolean shouldHaveBody() {
        return method.equals("POST") || method.equals("PUT");
    }

    /**
     * Set a header value for the given header.
     * 
     * @param header the String corresponding to the header.
     * @param value  the value of the header.
     */
    private void setHeader(String header, String value) {
        headers.put(header, value);
    }

    /**
     * Get the method of this request.
     * 
     * @return the String corresponding to the method. Could be null.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Get the http version of this request.
     * 
     * @return the String corresponding to the http version. Could be null.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Get the url of this request.
     * 
     * @return the String corresponding to the url. Could be null.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Get the body of this request.
     * 
     * @return the String corresponding to the body. Could be null.
     */
    public String getBody() {
        return this.body;
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

    /**
     * Print this request
     */
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
        if (malformed || (version.equals("HTTP/1.1") && getHeaderValue("Host").isEmpty())) {
            malformed = true;
            return;
        }
        // check if version 1.0 and there is not a Content-Length header
        if ((method.equals("PUT") || method.equals("POST")) && version.equals("HTTP/1.0")
                && getHeaderValue("Content-Length").isEmpty()) {
            malformed = true;
            return;
        }
        // Check if Host header is present and if the specified Host actually exists in
        // the server
        if (getHeaderValue("Host").isPresent() && !server.hasDomain(getHeaderValue("Host").get())) {
            malformed = true;
            return;
        }
    }

    /**
     * Set the default host if we have a HTTP1.0, where the host is optional In any
     * case return the host of the request.
     */
    public String setHostIfNull() {
        if (version.equals("HTTP/1.0") && getHeaderValue("Host").isEmpty()) {
            headers.put("Host", server.getDefaultHost());
        }
        return headers.get("Host");
    }

    /**
     * Set the url of this request. Used for the root entry point when "/" is
     * requested.
     * 
     * @param url the ulr string
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Return the Content-Length header value, if there is one.
     * 
     * @return the value of the Content-Length, -1 otherwise
     */
    public int getContentLength() {
        return headers.get("Content-Length") == null ? -1 : Integer.parseInt(headers.get("Content-Length"));
    }

}
