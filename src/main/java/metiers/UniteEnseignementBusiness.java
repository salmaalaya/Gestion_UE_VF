package metiers;

import entities.UniteEnseignement;
import exceptions.ApiException;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Stockage en mémoire des UE : les données sont perdues à chaque redémarrage. */
public class UniteEnseignementBusiness {

    /** Verrou partagé avec ModuleBusiness pour garder les deux listes cohérentes. */
    static final Object LOCK = new Object();

    private static final UniteEnseignementBusiness INSTANCE = new UniteEnseignementBusiness();

    private final Map<Integer, UniteEnseignement> unites = new LinkedHashMap<>();

    private UniteEnseignementBusiness() {
        reset();
    }

    public static UniteEnseignementBusiness getInstance() {
        return INSTANCE;
    }

    /** Restaure les données d'exemple. */
    public void reset() {
        synchronized (LOCK) {
            unites.clear();
            save(new UniteEnseignement(1, "Informatique", "Mme Maroua Douiri", 6, 1));
            save(new UniteEnseignement(2, "Mathématiques", "Mme Ines ElMejid", 5, 1));
            save(new UniteEnseignement(3, "Physique", "Mme Sarra Abidi", 4, 2));
            save(new UniteEnseignement(4, "Infographie", "Mme Oumeima Ibnelfkih", 3, 1));
            save(new UniteEnseignement(5, "Chimie", "M. Mohamed Amine Chebbi", 4, 2));
        }
    }

    public List<UniteEnseignement> search(String domaine, Integer semestre) {
        synchronized (LOCK) {
            List<UniteEnseignement> resultat = new ArrayList<>();
            for (UniteEnseignement ue : unites.values()) {
                boolean bonDomaine = domaine == null || ue.getDomaine().equalsIgnoreCase(domaine);
                boolean bonSemestre = semestre == null || ue.getSemestre() == semestre;
                if (bonDomaine && bonSemestre) {
                    resultat.add(ue);
                }
            }
            return resultat;
        }
    }

    public UniteEnseignement getByCode(int code) {
        synchronized (LOCK) {
            UniteEnseignement ue = unites.get(code);
            if (ue == null) {
                throw new ApiException(Status.NOT_FOUND, "UE " + code + " introuvable");
            }
            return ue;
        }
    }

    public boolean exists(int code) {
        synchronized (LOCK) {
            return unites.containsKey(code);
        }
    }

    public void add(UniteEnseignement ue) {
        validate(ue);
        synchronized (LOCK) {
            if (unites.containsKey(ue.getCode())) {
                throw new ApiException(Status.CONFLICT, "Le code " + ue.getCode() + " existe déjà");
            }
            save(ue);
        }
    }

    public void update(int code, UniteEnseignement ue) {
        if (ue != null) {
            ue.setCode(code); // le code de l'URL fait foi
        }
        validate(ue);
        synchronized (LOCK) {
            getByCode(code);
            save(ue);
        }
    }

    public void delete(int code) {
        synchronized (LOCK) {
            getByCode(code);
            if (ModuleBusiness.getInstance().existsForUE(code)) {
                throw new ApiException(Status.CONFLICT,
                        "Impossible de supprimer l'UE " + code + " : elle contient des modules");
            }
            unites.remove(code);
        }
    }

    private void save(UniteEnseignement ue) {
        unites.put(ue.getCode(), ue);
    }

    private void validate(UniteEnseignement ue) {
        if (ue == null) {
            throw new ApiException(Status.BAD_REQUEST, "Corps de requête manquant");
        }
        if (ue.getCode() <= 0) {
            throw new ApiException(Status.BAD_REQUEST, "Le code doit être positif");
        }
        if (isBlank(ue.getDomaine()) || isBlank(ue.getResponsable())) {
            throw new ApiException(Status.BAD_REQUEST, "Le domaine et le responsable sont obligatoires");
        }
        if (ue.getCredits() <= 0) {
            throw new ApiException(Status.BAD_REQUEST, "Les crédits doivent être positifs");
        }
        if (ue.getSemestre() <= 0) {
            throw new ApiException(Status.BAD_REQUEST, "Le semestre doit être positif");
        }
    }

    private boolean isBlank(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
