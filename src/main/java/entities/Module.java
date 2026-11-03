package entities;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Module {

    public enum TypeModule {
        COURS,
        TD,
        TP
    }

    private String matricule;
    private String nom;
    private int coefficient;
    private int volumeHoraire;
    private TypeModule type;
    private int codeUE;

    public Module() {}

    public Module(String matricule, String nom, int coefficient, int volumeHoraire,
                  TypeModule type, int codeUE) {
        this.matricule = matricule;
        this.nom = nom;
        this.coefficient = coefficient;
        this.volumeHoraire = volumeHoraire;
        this.type = type;
        this.codeUE = codeUE;
    }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getCoefficient() { return coefficient; }
    public void setCoefficient(int coefficient) { this.coefficient = coefficient; }

    public int getVolumeHoraire() { return volumeHoraire; }
    public void setVolumeHoraire(int volumeHoraire) { this.volumeHoraire = volumeHoraire; }

    public TypeModule getType() { return type; }
    public void setType(TypeModule type) { this.type = type; }

    public int getCodeUE() { return codeUE; }
    public void setCodeUE(int codeUE) { this.codeUE = codeUE; }
}
