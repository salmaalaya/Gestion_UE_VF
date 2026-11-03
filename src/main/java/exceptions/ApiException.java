package exceptions;

import javax.ws.rs.core.Response;

/** Erreur métier traduite en réponse HTTP par {@link ApiExceptionMapper}. */
public class ApiException extends RuntimeException {

    private final Response.Status status;

    public ApiException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}
