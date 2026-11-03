package metiers;

import entities.Module;
import exceptions.ApiException;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Stockage en mémoire des modules : chaque module appartient à une UE existante. */
public class ModuleBusiness {

    private static final ModuleBusiness INSTANCE = new ModuleBusiness();

    private final Map<String, Module> modules = new LinkedHashMap<>();

    private ModuleBusiness() {}

    public static ModuleBusiness getInstance() {
        return INSTANCE;
    }

    public void reset() {
        synchronized (UniteEnseignementBusiness.LOCK) {
            modules.clear();
        }
    }

    public List<Module> search(Integer codeUE) {
        synchronized (UniteEnseignementBusiness.LOCK) {
            List<Module> resultat = new ArrayList<>();
            for (Module module : modules.values()) {
                if (codeUE == null || module.getCodeUE() == codeUE) {
                    resultat.add(module);
                }
            }
            return resultat;
        }
    }

    public Module getByMatricule(String matricule) {
        synchronized (UniteEnseignementBusiness.LOCK) {
            Module module = modules.get(matricule);
            if (module == null) {
                throw new ApiException(Status.NOT_FOUND, "Module " + matricule + " introuvable");
            }
            return module;
        }
    }

    public boolean existsForUE(int codeUE) {
        return !search(codeUE).isEmpty();
    }

    public void add(Module module) {
        validate(module);
        synchronized (UniteEnseignementBusiness.LOCK) {
            checkUE(module);
            if (modules.containsKey(module.getMatricule())) {
                throw new ApiException(Status.CONFLICT,
                        "Le matricule " + module.getMatricule() + " existe déjà");
            }
            modules.put(module.getMatricule(), module);
        }
    }

    public void update(String matricule, Module module) {
        if (module != null) {
            module.setMatricule(matricule); // le matricule de l'URL fait foi
        }
        validate(module);
        synchronized (UniteEnseignementBusiness.LOCK) {
            getByMatricule(matricule);
            checkUE(module);
            modules.put(matricule, module);
        }
    }

    public void delete(String matricule) {
        synchronized (UniteEnseignementBusiness.LOCK) {
            getByMatricule(matricule);
            modules.remove(matricule);
        }
    }

    private void checkUE(Module module) {
        if (!UniteEnseignementBusiness.getInstance().exists(module.getCodeUE())) {
            throw new ApiException(Status.BAD_REQUEST, "L'UE " + module.getCodeUE() + " n'existe pas");
        }
    }

    private void validate(Module module) {
        if (module == null) {
            throw new ApiException(Status.BAD_REQUEST, "Corps de requête manquant");
        }
        if (isBlank(module.getMatricule()) || isBlank(module.getNom())) {
            throw new ApiException(Status.BAD_REQUEST, "Le matricule et le nom sont obligatoires");
        }
        if (module.getCoefficient() <= 0 || module.getVolumeHoraire() <= 0) {
            throw new ApiException(Status.BAD_REQUEST,
                    "Le coefficient et le volume horaire doivent être positifs");
        }
        if (module.getType() == null) {
            throw new ApiException(Status.BAD_REQUEST, "Le type est obligatoire (COURS, TD ou TP)");
        }
    }

    private boolean isBlank(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
