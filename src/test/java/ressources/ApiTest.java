package ressources;

import metiers.ModuleBusiness;
import metiers.UniteEnseignementBusiness;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Teste l'API complète (ressources, filtre, erreurs) sans serveur HTTP. */
class ApiTest {

    private static final String UE = "{\"code\":10,\"domaine\":\"Réseaux\",\"responsable\":\"Mme Test\",\"credits\":3,\"semestre\":1}";
    private static final String MODULE = "{\"matricule\":\"M1\",\"nom\":\"Java\",\"coefficient\":2,\"volumeHoraire\":30,\"type\":\"TP\",\"codeUE\":1}";

    private static final ApplicationHandler APP = new ApplicationHandler(
            new ResourceConfig().packages("ressources", "filtres", "exceptions").register(JacksonFeature.class));

    @BeforeEach
    void resetData() {
        UniteEnseignementBusiness.getInstance().reset();
        ModuleBusiness.getInstance().reset();
    }

    private int call(String method, String path, String json, String token) throws Exception {
        URI base = URI.create("http://localhost/");
        ContainerRequest request = new ContainerRequest(base, base.resolve(path), method, null,
                new MapPropertiesDelegate(), null);
        if (json != null) {
            request.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            request.setEntityStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        }
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return APP.apply(request).get().getStatus();
    }

    private ContainerResponse login(String password) throws Exception {
        URI base = URI.create("http://localhost/");
        ContainerRequest request = new ContainerRequest(base, base.resolve("authentification"), "POST", null,
                new MapPropertiesDelegate(), null);
        request.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        request.setEntityStream(new ByteArrayInputStream(
                ("username=admin&password=" + password).getBytes(StandardCharsets.UTF_8)));
        return APP.apply(request).get();
    }

    private String token() throws Exception {
        return (String) login("admin").getEntity();
    }

    @Test
    void lectureSansToken() throws Exception {
        assertEquals(200, call("GET", "UE", null, null));
        assertEquals(200, call("GET", "modules", null, null));
    }

    @Test
    void mauvaisMotDePasseRefuse() throws Exception {
        assertEquals(401, login("mauvais").getStatus());
    }

    @Test
    void ecritureSansTokenRefusee() throws Exception {
        assertEquals(401, call("POST", "UE", UE, null));
        assertEquals(401, call("DELETE", "UE/1", null, null));
        assertEquals(401, call("POST", "modules", MODULE, null));
    }

    @Test
    void tokenFalsifieRefuse() throws Exception {
        assertEquals(401, call("DELETE", "UE/1", null, "abc.def.ghi"));
    }

    @Test
    void crudUE() throws Exception {
        String token = token();
        assertEquals(201, call("POST", "UE", UE, token));
        assertEquals(200, call("GET", "UE/10", null, null));
        assertEquals(200, call("PUT", "UE/10", UE.replace("Réseaux", "Sécurité"), token));
        assertEquals("Sécurité", UniteEnseignementBusiness.getInstance().getByCode(10).getDomaine());
        assertEquals(204, call("DELETE", "UE/10", null, token));
        assertEquals(404, call("GET", "UE/10", null, null));
    }

    @Test
    void codeDupliqueRefuse() throws Exception {
        String token = token();
        call("POST", "UE", UE, token);
        assertEquals(409, call("POST", "UE", UE, token));
    }

    @Test
    void miseAJourNeChangePasLeCode() throws Exception {
        assertEquals(200, call("PUT", "UE/1", UE, token()));
        assertEquals(200, call("GET", "UE/1", null, null));
        assertEquals(404, call("GET", "UE/10", null, null));
    }

    @Test
    void ueInvalideRefusee() throws Exception {
        String token = token();
        assertEquals(400, call("POST", "UE", UE.replace("\"credits\":3", "\"credits\":-1"), token));
        assertEquals(400, call("POST", "UE", UE.replace("Réseaux", " "), token));
        assertEquals(400, call("POST", "UE", null, token));
    }

    @Test
    void filtreParSemestre() {
        assertEquals(2, UniteEnseignementBusiness.getInstance().search(null, 2).size());
    }

    @Test
    void crudModule() throws Exception {
        String token = token();
        assertEquals(201, call("POST", "modules", MODULE, token));
        assertEquals(409, call("POST", "modules", MODULE, token));
        assertEquals(200, call("PUT", "modules/M1", MODULE.replace("Java", "Spring"), token));
        assertEquals("Spring", ModuleBusiness.getInstance().getByMatricule("M1").getNom());
        assertEquals(204, call("DELETE", "modules/M1", null, token));
    }

    @Test
    void moduleSurUEInexistanteRefuse() throws Exception {
        assertEquals(400, call("POST", "modules", MODULE.replace("\"codeUE\":1", "\"codeUE\":99"), token()));
    }

    @Test
    void typeInconnuRefuse() throws Exception {
        assertEquals(400, call("POST", "modules", MODULE.replace("TP", "XX"), token()));
    }

    @Test
    void ueAvecModulesNonSupprimable() throws Exception {
        String token = token();
        call("POST", "modules", MODULE, token);
        assertEquals(409, call("DELETE", "UE/1", null, token));
        call("DELETE", "modules/M1", null, token);
        assertEquals(204, call("DELETE", "UE/1", null, token));
    }
}
