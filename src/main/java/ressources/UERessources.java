package ressources;

import entities.UniteEnseignement;
import filtres.Secured;
import metiers.UniteEnseignementBusiness;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("UE")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UERessources {

    private final UniteEnseignementBusiness metier = UniteEnseignementBusiness.getInstance();

    @GET
    public List<UniteEnseignement> list(@QueryParam("domaine") String domaine,
                                        @QueryParam("semestre") Integer semestre) {
        return metier.search(domaine, semestre);
    }

    @GET
    @Path("/{code}")
    public UniteEnseignement get(@PathParam("code") int code) {
        return metier.getByCode(code);
    }

    @POST
    @Secured
    public Response add(UniteEnseignement ue) {
        metier.add(ue);
        return Response.status(Response.Status.CREATED).entity(ue).build();
    }

    @PUT
    @Secured
    @Path("/{code}")
    public UniteEnseignement update(@PathParam("code") int code, UniteEnseignement ue) {
        metier.update(code, ue);
        return ue;
    }

    @DELETE
    @Secured
    @Path("/{code}")
    public Response delete(@PathParam("code") int code) {
        metier.delete(code);
        return Response.noContent().build();
    }
}
