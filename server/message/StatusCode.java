package server.message;

public enum StatusCode {
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