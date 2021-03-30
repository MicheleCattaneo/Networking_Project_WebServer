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
            System.out.println(parsedLine[0]+ "Unknown Input");
        }
        return true;
    }
    /*
PUT /cocacoca.html HTTP/1.1
Host: matias.com 
Connection: keep-alive
Content-Length: 10
Content-Type: applicatio/jonson

Ciao ti
aw
     */

    private void setHeader(String header, String value) {
        headers.put(header, value);
    }

    public String getMethod() {
        return method;
    }

    public Optional<String> getHeaderValue(final String header) {
        return headers.get(header) == null ? Optional.empty() : Optional.of(headers.get(header));
    }

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
