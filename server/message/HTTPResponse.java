package server.message;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.time.LocalDateTime;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;


import server.FileHandler;
import server.Server;
import server.Server.DomainInformations;

enum StatusCode {
    OK("200 OK"), CREATED("201 CREATED"), BAD_REQUEST("400 BAD REQUEST"), FORBIDDEN("403 FORBIDDEN"),
    NOT_FOUND("404 NOT FOUND"), METHOD_NOT_ALLOWED("405 METHOD NOT ALLOWED"), NOT_IMPLEMENTED("501 NOT IMPLEMENTED"),
    HTTP_VERSION_NOT_SUPPORTED("505 HTTP VERSION NOT SUPPORTED"), INTERNAL_SERVER_ERROR("500 Internal Server Error");

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
    private final Server server;

    private boolean lastResponse;

    public HTTPResponse(Server server) {
        headers = new HashMap<>();
        body = "";
        lastResponse = false;
        this.server = server;
    }

    /**
     * Handle the request depending on the given Method
     * 
     * @param request
     * @return the fully initialized response to be returned to the caller
     * @throws IOException
     */
    public HTTPResponse handleRequest(final HTTPRequest request) throws IOException {

        try {
            request.checkValidity();
            if (request.isMalformed()) {
                this.version = "HTTP/1.0";
                this.status = StatusCode.BAD_REQUEST;
                this.lastResponse = true;
                return this;
            }
    
            // check if this response is the last one to be sent
            if (request.getVersion().equals("HTTP/1.0") || shouldCloseConnection(request)) {
                lastResponse = true;
            }
    
            if (checkVersion(request.getVersion())) {
                this.version = request.getVersion();
            } else {
                this.version = "HTTP/1.0";
                this.status = StatusCode.HTTP_VERSION_NOT_SUPPORTED;
                lastResponse = true;
                return this;
            }
    
            // TODO HTTP/1.1 host header is not optional, check that if not there -> 400
            // TODO if it's a POST, it is normal that the URL is not gonna point to a file,
            // it's not there yet
            // urlExists("michelecattaneo.ch", request.getUrl());
    
            request.setHostIfNull();
            this.version = request.getVersion();
            switch (request.getMethod()) {
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
        catch(Exception e) {
            if (e.getClass() == NullPointerException.class) {
                System.out.println("NullPointer Exception catched");
                this.status = StatusCode.INTERNAL_SERVER_ERROR;
                return this;
            }

            if (e.getClass() == IOException.class) {
                System.out.println("IO Exception catched");
                this.status = StatusCode.NOT_FOUND;
                return this;
            }

            // default error for unknown exceptions
            System.out.println("Other Exception catched");
            this.status = StatusCode.INTERNAL_SERVER_ERROR;
            return this;
        }

    }

    private HTTPResponse handleGET(final HTTPRequest request) throws IOException, NullPointerException {
        // TODO: check if we request a folder instead of file

        if (FileHandler.isValidFile(request.getHeaderValue("Host").get(), request.getUrl())) { // if file exists
            if (!FileHandler.hasPermissions(request.getHeaderValue("Host").get(), request.getUrl())) { // if it has
                                                                                                       // permissions
                this.status = StatusCode.FORBIDDEN;
                this.headers.put("Date", LocalDateTime.now().toString());
                return this;
            }
            headers.put("Date", LocalDateTime.now().toString());
            String ext = FileHandler.getFileType(request.getUrl());
            String contentType = FileHandler.mapExtensionsToContentType(ext);
            headers.put("Content-Type", contentType);
            body = FileHandler.getBody(request.getHeaderValue("Host").get(), request.getUrl());
            this.status = StatusCode.OK;
            System.out.println("response");
        } else {
            this.status = StatusCode.NOT_FOUND;
            this.headers.put("Date", LocalDateTime.now().toString());
            return this;
        }
        return this;
    }

    private HTTPResponse handlePOST(final HTTPRequest request) {
        // TODO: build this response
        return this;
    }

    private HTTPResponse handlePUT(final HTTPRequest request) {
        // TODO: build this response
        return this;
    }

    private HTTPResponse handleDELETE(final HTTPRequest request) {
        // TODO: build this response
        return this;
    }

    private HTTPResponse handleNTW21INFO(final HTTPRequest request) throws NullPointerException{
       
        Optional<String> host = request.getHeaderValue("Host");
        if(host.isPresent()) {
            // System.out.println(host.get());
            this.status = StatusCode.OK;
            headers.put("Date", LocalDateTime.now().toString());
            LinkedHashMap<String, DomainInformations> map =  (LinkedHashMap)server.getDomainsInformations();
            DomainInformations info = map.get(host.get());
            
            String body =   "The administrator of " +
                            host.get() +
                            " is " + info.memberFullname + ".\n" +
                            "You can contact him at " + info.memberEmail + ".\n";
            this.body = body;
        } else {
            // ideally we never are here as we set the default host
            this.status = StatusCode.BAD_REQUEST;
        }
        return this;
    }

    /**
     * 
     * @param request
     * @return true if the connection should close else false
     */
    private boolean shouldCloseConnection(HTTPRequest request) {
        Optional<String> value = request.getHeaderValue("Connection");
        return value.isPresent() && value.get().equalsIgnoreCase("close");
    }

    private boolean checkVersion(String version) {
        return version.equals("HTTP/1.0") || version.equals("HTTP/1.1");
    }

    /**
     * Check wheter this is the last response that should be sent, after which the
     * connection will be closed.
     * 
     * @return true if it's the last one, false otherwise
     */
    public boolean isLastOne() {
        return lastResponse;
    }

    @Override
    public String toString() {
        String result = "";
        // Response line
        result += version + " " + status.getStatus() + "\r\n";
        // Head lines
        Iterator itr = headers.keySet().iterator();
        while (itr.hasNext()) {
            String key = (String) itr.next();
            result += key + ": " + headers.get(key) + "\r\n";
        }
        result += "\r\n";
        result += body;

        return result;
    }
}
