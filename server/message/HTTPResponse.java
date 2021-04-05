package server.message;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.time.LocalDateTime;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import server.FileHandler;
import server.Server;
import server.Server.DomainInformations;

public class HTTPResponse {
    private String version;
    private StatusCode status;
    private HashMap<String, String> headers;
    private byte[] body;
    private final Server server;

    private boolean lastResponse;

    public HTTPResponse(Server server) {
        headers = new HashMap<>();
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

            // set the host if it's not specified ( for HTTP/1.0 )
            String requestHost = request.setHostIfNull();

            // if the URL is the root, get the EntryPoint File for this host
            if (request.getUrl().equals("/")) {
                String entryPoint = server.getEntryPoint(requestHost);
                if (entryPoint == null) {
                    this.status = StatusCode.INTERNAL_SERVER_ERROR;
                    return null;
                }
                request.setUrl("/" + entryPoint);
            }

            this.version = request.getVersion();
            switch (request.getMethod()) {
            case "GET":
                return handleGET(request);
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

        } catch (Exception e) {
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
            headers.put("Content-Length", String.valueOf(body.length));
        } else {
            this.status = StatusCode.NOT_FOUND;
            this.headers.put("Date", LocalDateTime.now().toString());
            lastResponse = true;
            return this;
        }
        return this;
    }

    private HTTPResponse handlePUT(final HTTPRequest request) {
        if (!FileHandler.hasPermissions(request.getHeaderValue("Host").get(), request.getUrl())) { // if it has
            // permissions
            this.status = StatusCode.FORBIDDEN;
            this.headers.put("Date", LocalDateTime.now().toString());
            lastResponse = true;
            return this;
        }

        if (request.getHeaderValue("Content-Length").isEmpty() || request.getHeaderValue("Content-Type").isEmpty()
                || Integer.parseInt(request.getHeaderValue("Content-Length").get()) != request.getBody().length()) {
            System.out.println("error");
            this.status = StatusCode.BAD_REQUEST;
            lastResponse = true;
            return this;
        }

        try {
            boolean isCreated = FileHandler.createFile(request.getHeaderValue("Host").get(), request.getUrl(),
                    request.getBody());
            this.status = isCreated ? StatusCode.CREATED : StatusCode.OK;

            headers.put("Date", LocalDateTime.now().toString());
        } catch (IOException e) {
            this.status = StatusCode.INTERNAL_SERVER_ERROR;
            headers.put("Date", LocalDateTime.now().toString());
            lastResponse = true;
            return this;
        }
        return this;
    }

    private HTTPResponse handleDELETE(final HTTPRequest request) {
        if (!FileHandler.hasPermissions(request.getHeaderValue("Host").get(), request.getUrl())) { // if it has
            // permissions
            this.status = StatusCode.FORBIDDEN;
            this.headers.put("Date", LocalDateTime.now().toString());
            lastResponse = true;
            return this;
        }
        try {
            FileHandler.deleteFile(request.getHeaderValue("Host").get(), request.getUrl());
            this.status = StatusCode.OK;
            headers.put("Date", LocalDateTime.now().toString());
        } catch (FileNotFoundException e) {
            this.status = StatusCode.NOT_FOUND;
            lastResponse = true;
        }
        return this;
    }

    private HTTPResponse handleNTW21INFO(final HTTPRequest request) throws NullPointerException {

        Optional<String> host = request.getHeaderValue("Host");
        if (host.isPresent()) {
            this.status = StatusCode.OK;
            headers.put("Date", LocalDateTime.now().toString());

            LinkedHashMap<String, DomainInformations> map = (LinkedHashMap) server.getDomainsInformations();
            DomainInformations info = map.get(host.get());

            String body = "The administrator of " + host.get() + " is " + info.memberFullname + ".\n"
                    + "You can contact him at " + info.memberEmail + ".\n";
            this.body = body.getBytes();
            headers.put("Content-Length", String.valueOf(body.length()));
            headers.put("Content-Type", "text/plain");
        } else {
            // ideally we never are here as we set the default host
            this.status = StatusCode.BAD_REQUEST;
            lastResponse = true;
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

    public byte[] toStringMod() {
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
        if (body != null && body.length > 0) {
            byte[] responseHead = result.getBytes();
            byte[] finalResult = new byte[responseHead.length + body.length];
            System.arraycopy(responseHead, 0, finalResult, 0, responseHead.length);
            System.arraycopy(body, 0, finalResult, responseHead.length, body.length);
            return finalResult;
        }
        return result.getBytes();
    }

    public StatusCode getStatus() {
        return this.status;
    }
}
