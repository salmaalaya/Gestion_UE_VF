package ressources;

import entities.Module;
import filtres.Secured;
import metiers.ModuleBusiness;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("modules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModuleRessources {

    private final ModuleBusiness metier = ModuleBusiness.getInstance();

    @GET
    public List<Module> list(@QueryParam("codeUE") Integer codeUE) {
        return metier.search(codeUE);
    }

    @GET
    @Path("/{matricule}")
    public Module get(@PathParam("matricule") String matricule) {
        return metier.getByMatricule(matricule);
    }

    @POST
    @Secured
    public Response add(Module module) {
        metier.add(module);
        return Response.status(Response.Status.CREATED).entity(module).build();
    }

    @PUT
    @Secured
    @Path("/{matricule}")
    public Module update(@PathParam("matricule") String matricule, Module module) {
        metier.update(matricule, module);
        return module;
    }

    @DELETE
    @Secured
    @Path("/{matricule}")
    public Response delete(@PathParam("matricule") String matricule) {
        metier.delete(matricule);
        return Response.noContent().build();
    }
}
