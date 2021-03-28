package server.message;

import java.util.HashMap;

enum StatusCode {

}

public class HTTPResponse {
    private String version;
    private StatusCode response;
    private HashMap<String, String> headers;
    private String body;
}
