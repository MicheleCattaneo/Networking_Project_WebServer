package server.message;

import java.util.HashMap;
import java.util.Iterator;
import java.time.LocalDateTime;

enum StatusCode {
    OK("200 OK"),
    CREATED("201 CREATED"),
    BAD_REQUEST("400 BAD REQUEST"), 
    FORBIDDEN("403 FORBIDDEN"),
    NOT_FOUND("404 NOT FOUND"), 
    METHOD_NOT_ALLOWED("405 METHOD NOT ALLOWED"), 
    NOT_IMPLEMENTED("501 NOT IMPLEMENTED"),
    HTTP_VERSION_NOT_SUPPORTED("505 HTTP VERSION NOT SUPPORTED");
    
    private String status;
    public String getStatus() {
        return this.status;
    }
    
    private StatusCode(final String status) {
        this.status = status;
    }
}

public class HTTPResponse {
    private String version;
    private StatusCode status;
    private HashMap<String, String> headers;
    private String body;

    public HTTPResponse() {
        headers = new HashMap<>();
        body = "";
    }
    
    /**
     * Handle the request depending on the given Method
     * @param request
     * @return the fully initialized response to be returned to the caller
     */
    public HTTPResponse handleRequest(final HTTPRequest request) {

        if (checkVersion(request.getVersion())){
            this.version = request.getVersion();
        } else {
            this.version = "HTTP/1.0";
            this.status = StatusCode.HTTP_VERSION_NOT_SUPPORTED;
            return this;
        }
        
        if (!hasPermissions(request.getUrl())) {
            this.status = StatusCode.FORBIDDEN;
            return this;
        }
        
        this.version = request.getVersion();
        switch(request.getMethod()){
            case "GET": 
                return handleGET(request);
            case "POST": 
                return handlePOST(request);
            case "PUT": 
                return handlePUT(request);
            case "DELETE": 
                return handleDELETE(request);
            case "NTW21INFO":
                return handleNTW21INFO(request);
            default:
                this.status = StatusCode.NOT_IMPLEMENTED;
                return this;
        }
    }
    
   
    private HTTPResponse handleGET(final HTTPRequest request ) {
        // TODO: build this response
        this.status = StatusCode.OK;
        headers.put("Date", LocalDateTime.now().toString());
        return this;
    }
    
    
    private HTTPResponse handlePOST(final HTTPRequest request ) {
        // TODO: build this response
        return this;
    }
    
    private HTTPResponse handlePUT(final HTTPRequest request ) {
        // TODO: build this response
        return this;
    }
    
    private HTTPResponse handleDELETE(final HTTPRequest request ) {
        // TODO: build this response
        return this;
    }

    private HTTPResponse handleNTW21INFO(final HTTPRequest request ) {
        // TODO: build this response
        return this;
    }

    private boolean hasPermissions(final String url) {
        return !url.contains("../");
    }
    
    private boolean checkVersion(String version) {
        return version.equals("HTTP/1.0") || version.equals("HTTP/1.1");
    }


    @Override
    public String toString() {
        String result = "";
        // Response line
        result += version + " " + status.getStatus() + "\r\n";
        // Head lines
        Iterator itr = headers.keySet().iterator();
        while(itr.hasNext()) {
            String key = (String)itr.next();
            result += key + " " + headers.get(key) + "\r\n";
        }
        result += "\r\n";
        result += body;

        return result;
    }
}
