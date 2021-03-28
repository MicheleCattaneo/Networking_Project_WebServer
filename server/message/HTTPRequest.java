package server.message;

import java.util.HashMap;
import java.util.Scanner;

/**
 * HTTPRequest
 */
public class HTTPRequest {

    enum Method {
        GET, POST, DELETE, NTW21INFO
    }

    private String method;
    private String url;
    private String version;
    private HashMap<String, String> headers;
    private String body;

    public void parseAndComputeLine(final String line) {
        Scanner sc = new Scanner(line).useDelimiter(" |\\n");
        while (sc.hasNext()) {

        }
        sc.close();
    }
}