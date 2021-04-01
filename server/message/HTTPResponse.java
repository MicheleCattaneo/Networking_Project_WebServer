package server.message;

import java.util.HashMap;
import java.util.Iterator;
import java.time.LocalDateTime;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

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
    private final String serverPath;

    public HTTPResponse() {
        headers = new HashMap<>();
        body = "";
        serverPath = Path.of("").toAbsolutePath().toString();
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

        // TODO Get the actual host, if present, or default one if not present
        // TODO HTTP/1.1 host header is not optional, check that if not there -> 400
        // TODO if it's a POST, it is normal that the URL is not gonna point to a file, it's not there yet
        urlExists("michelecattaneo.ch", request.getUrl());

        //TODO  test
        if (!hasPermissions("michelecattaneo.ch", "../michelecattaneo.ch/home.html")) {
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

    /**
     * Check whether the Path $SERVER_PATH/host/url is an existing object
     * @param host
     * @param url
     * @return
     */
    private boolean urlExists(final String host,final String url) {
        try {
            Path p = Paths.get(Path.of("").toAbsolutePath().toString() + "/" + host + "/" + url);
            System.out.println(p + " ciao");
            return true;
        }
        catch (InvalidPathException e) {
            return false;
        }
    }

    /**
     * Given the host and the url, get the CANONICAL path (unique, removed '../' ), and ensure that it starts with the same sub-path
     * Care must be given, if the given parameters to not form a valid url, the method returns false, but not because 
     * of a permission problem but because the object does not exist. Use this method AFTER having checked the validity of the host + url
     * @param host
     * @param url
     * @return
     */
    private boolean hasPermissions(String host, String url) {
        try {
            String canonicalObjectPath = new File(serverPath, "/" + host + "/" + url).getCanonicalPath();
            System.out.println(canonicalObjectPath);
            String canonicalHostPath = new File(serverPath, "/" + host).getCanonicalPath();
            
            if(canonicalObjectPath.startsWith(canonicalHostPath)) {
                return true;
            }
            return false;

        } catch (IOException e) {
            
            e.printStackTrace();
            return false;
        }
        
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
