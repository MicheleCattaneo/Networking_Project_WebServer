package server.message;

import java.util.HashMap;
import java.util.Optional;

/**
 * HTTPRequest
 */
public class HTTPRequest {

    private String method;
    private String url;
    private String version;
    private HashMap<String, String> headers;
    private String body;

    public HTTPRequest() {
        headers = new HashMap<>();
        this.body = "";
    }

    /**
     * Parse a given HTTP request line or header line and update the relative fields.
     * @param line the HTTP request line or header line
     * @return true on success, false otherwise.
     */
    public boolean parseAndComputeLine(final String line) {
        String[] parsedLine = line.split(" ");

        if (parsedLine.length < 2) {
            System.err.println("error");
            return false;
        }
        
        // Handle request lines
        if (parsedLine[0].equals("GET") 
            || parsedLine[0].equals("POST")
            || parsedLine[0].equals("PUT")
            || parsedLine[0].equals("DELETE")
            || parsedLine[0].equals("NTW21INFO")) {
            if (parsedLine.length != 3) {
                System.err.println("Request line should have 3 fields");
                return false;
            }
            method = parsedLine[0];
            url = parsedLine[1];
            version = parsedLine[2];
        } // Handle Headers
        else if (parsedLine[0].equals("Host:")
                || parsedLine[0].equals("Connection:")
                || parsedLine[0].equals("Content-Length:")
                || parsedLine[0].equals("Content-Type:")) {
            setHeader(parsedLine[0], parsedLine[1]);
        }
        else {
            System.out.println(parsedLine[0]+ " Unknown Input");
        }
        return true;
    }
    /*
GET /home.html HTTP/1.1
Host: michelecattaneo.ch 
Connection: keep-alive
Content-Length: 10
Content-Type: applicatio/jonson


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
     * @param header the given header String.
     * @return An Optional<String> with the value if the header is present, an empty Optional otherwise.
     */
    public Optional<String> getHeaderValue(final String header) {
        return headers.get(header) == null ? Optional.empty() : Optional.of(headers.get(header));
    }

    /**
     * Append the given String to the body of this request.
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
        System.out.println("Host: " + headers.get("Host:"));
        System.out.println("Connection: " + headers.get("Connection:"));
        System.out.println("Content-Length: " + headers.get("Content-Length:"));
        System.out.println("Content-Type: " + headers.get("Content-Type:"));
        System.out.println("");
        System.out.println(body);
    }
}
