package ressources;

import exceptions.ApiException;
import utilities.SecurityConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("authentification")
public class AuthenticationEndPoint {

    @Context
    private UriInfo uriInfo;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticateUser(@FormParam("username") String username,
                                     @FormParam("password") String password) {
        if (!SecurityConfig.checkCredentials(username, password)) {
            throw new ApiException(Response.Status.UNAUTHORIZED, "Identifiants incorrects");
        }
        return Response.ok(SecurityConfig.issueToken(username, uriInfo.getAbsolutePath().toString())).build();
    }
}
