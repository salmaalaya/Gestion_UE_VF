# Gestion UE - API REST

Application de gestion des **Unités d'Enseignement (UE)** et de leurs **modules** : une API REST JAX-RS sécurisée par JWT et une interface web. Projet réalisé dans le cadre de l'atelier SOA.

![Interface](docs/interface.png)

## Technologies

Java 17, JAX-RS (Jersey 2.35), Jackson, JWT (jjwt), JUnit 5, Maven, HTML/CSS/JavaScript.

## Lancer le projet

Prérequis : JDK 17 et Maven.

```bash
mvn jetty:run
```

Ouvrir ensuite http://localhost:8089/Gestion_UE_VF/

Pour Tomcat, déployer `target/Gestion_UE_VF.war` (généré par `mvn package`) sur **Tomcat 9** : l'application utilise `javax.*`, donc Tomcat 10 ne convient pas.

Pour lancer les tests :

```bash
mvn test
```

## Configuration

| Variable | Rôle | Défaut |
| --- | --- | --- |
| `ADMIN_USERNAME` | Identifiant | `admin` |
| `ADMIN_PASSWORD` | Mot de passe | `admin` |
| `JWT_SECRET` | Secret de signature des tokens | Aléatoire à chaque démarrage |

Les valeurs par défaut sont réservées au développement local.

## API

Base : `/Gestion_UE_VF/rest`. Les routes marquées 🔒 exigent l'en-tête `Authorization: Bearer <token>`.

| Méthode | Route | Description |
| --- | --- | --- |
| POST | `/authentification` | Renvoie un token valable 15 min (formulaire `username`, `password`) |
| GET | `/UE?domaine=&semestre=` | Lister les UE, avec filtres optionnels |
| GET | `/UE/{code}` | Détail d'une UE |
| POST 🔒 | `/UE` | Ajouter une UE |
| PUT 🔒 | `/UE/{code}` | Modifier une UE |
| DELETE 🔒 | `/UE/{code}` | Supprimer une UE sans module |
| GET | `/modules?codeUE=` | Lister les modules, avec filtre optionnel |
| GET | `/modules/{matricule}` | Détail d'un module |
| POST 🔒 | `/modules` | Ajouter un module |
| PUT 🔒 | `/modules/{matricule}` | Modifier un module |
| DELETE 🔒 | `/modules/{matricule}` | Supprimer un module |

Les erreurs sont renvoyées au format `{"error": "message"}` : 400 (données invalides), 401 (non authentifié), 404 (introuvable), 409 (conflit).

Exemple :

```bash
BASE=http://localhost:8089/Gestion_UE_VF/rest
TOKEN=$(curl -s -d "username=admin&password=admin" $BASE/authentification)

curl -X POST $BASE/modules \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"matricule":"M1","nom":"Java","coefficient":2,"volumeHoraire":30,"type":"TP","codeUE":1}'
```

## Structure

```
src/main/java
├── entities     Modèle (UE, Module)
├── exceptions   Erreurs métier et conversion en réponses JSON
├── filtres      Filtre JWT et annotation @Secured
├── metiers      Règles métier et stockage
├── ressources   Endpoints REST
└── utilities    Activation JAX-RS et sécurité
src/main/webapp  Interface web
src/test/java    Tests de l'API
```

## Limites

Les données sont stockées en mémoire et réinitialisées à chaque redémarrage.

## Auteur

Salma Alaya - Atelier SOA
