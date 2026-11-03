package filtres;

import utilities.SecurityConfig;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Map;

/** Exige un JWT valide sur les méthodes annotées @Secured. */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.toLowerCase().startsWith(SCHEME.toLowerCase() + " ")) {
            abort(requestContext, "Authentification requise");
            return;
        }

        try {
            SecurityConfig.validateToken(header.substring(SCHEME.length()).trim());
        } catch (Exception e) {
            abort(requestContext, "Token invalide ou expiré");
        }
    }

    private void abort(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, SCHEME)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", message))
                .build());
    }
}
