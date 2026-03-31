# SAE - Documentation BDD et API

## Auteurs
- CERDAN SULIVAN
- GAVOILLE florian

## 1. Objectif du document
Ce document resume le fonctionnement du projet en version simple:
- le schema de la base de donnees,
- les requetes SQL les plus importantes,
- la description de l'API avec les codes de retour.

## 2. Base de donnees

### 2.1. Contexte technique
- SGBD: PostgreSQL
- Connexion JDBC: jdbc:postgresql://psqlserv:5432/but2
- Driver: org.postgresql.Driver

### 2.2. Tables principales

![image](MCD.svg)

#### users
- id (PK)
- login
- password
- role (ADMIN ou USER)

#### wastetype
- id (PK)
- nom
- pointsPerKilos

#### collectionpoint
- id (PK)
- adresse
- capaciteMax

#### deposit
- id (PK)
- userid (FK vers users.id)
- pointid (FK vers collectionpoint.id)
- wastetypeid (FK vers wastetype.id)
- poids
- datedepot
- collecte (false = pas encore collecte, true = deja collecte)

#### accepts
Table de liaison entre collectionpoint et wastetype (relation N-N):
- pointid (FK vers collectionpoint.id)
- wastetypeid (FK vers wastetype.id)

### 2.3. Relations entre tables
- Un utilisateur peut faire plusieurs depots.
- Un point de collecte peut recevoir plusieurs depots.
- Un type de dechet peut apparaitre dans plusieurs depots.
- Un point de collecte peut accepter plusieurs types de dechets (via accepts).

## 3. Requetes SQL importantes

### 3.1. Leaderboard (top utilisateurs)
Utilisee pour calculer le score des utilisateurs en fonction des depots:

```sql
SELECT u.id AS userid,
       SUM(d.poids * w.pointsperkilos) AS score
FROM users AS u
JOIN deposit AS d ON (u.id = d.userid)
JOIN wastetype AS w ON (d.wastetypeid = w.id)
GROUP BY u.id
ORDER BY SUM(d.poids * w.pointsperkilos) DESC
LIMIT 10;
```

### 3.2. Types de dechets acceptes par un point

```sql
SELECT wt.*
FROM WasteType wt
JOIN Accepts a ON wt.id = a.wastetypeid
WHERE a.pointid = ?;
```

### 3.3. Capacite restante d'un point

```sql
SELECT SUM(poids) AS somme
FROM deposit
WHERE pointid = ?
  AND collecte = false;
```

Dans le code, on fait ensuite:
- capacite_restante = capaciteMax - somme

### 3.4. Vider un point (vidage logique)

```sql
UPDATE deposit
SET collecte = true
WHERE pointid = ?;
```

### 3.5. Verification d'acceptation d'un dechet

```sql
SELECT *
FROM Accepts
WHERE pointid = ?
  AND wastetypeid = ?;
```

Cette verification permet d'autoriser ou non l'ajout d'un depot.

## 4. API REST

### 4.1. Base d'URL
Contexte de deploiement: /sae

Exemples:
- /sae/auth/token
- /sae/wasteTypes
- /sae/deposits
- /sae/points
- /sae/users/leaderboard

### 4.2. Authentification
Les routes protegees utilisent:
- Authorization: Basic base64(login:password)

Roles:
- ADMIN
- USER

### 4.3. Signification des principaux codes
- 200: requete reussie
- 400: requete invalide (URL, id, body)
- 401: non authentifie / non autorise
- 403: action refusee par regle metier
- 404: ressource introuvable
- 409: conflit (souvent contrainte BDD)
- 500: erreur serveur

## 5. Endpoints et codes de retour

### 5.1. Auth

#### GET /auth/token
But: recuperer un token a partir de login/password.

Codes:
- 200: token renvoye si identifiants valides
- 200: message Inconnu au bataillon! sinon

### 5.2. WasteTypes

#### GET /wasteTypes
Codes:
- 200

#### GET /wasteTypes/{id}
Codes:
- 200
- 400
- 404

#### POST /wasteTypes (ADMIN)
Codes:
- 200
- 400
- 401
- 409
- 500

#### PUT /wasteTypes (ADMIN)
Codes:
- 200
- 400
- 401
- 404
- 500

#### DELETE /wasteTypes/{id} (ADMIN)
Codes:
- 200
- 400
- 401
- 404
- 409

### 5.3. Deposits

#### GET /deposits
Codes:
- 200

#### GET /deposits/{id}
Codes:
- 200
- 400
- 404

#### POST /deposits (USER ou ADMIN connecte)
Regles metier:
- poids negatif interdit
- depot refuse si le point est plein
- depot refuse si le type de dechet n'est pas accepte par le point

Codes:
- 200
- 400
- 401
- 403
- 500

#### PUT /deposits/{id} (ADMIN)
Codes:
- 200
- 400
- 401
- 404
- 500

#### PATCH /deposits/{id} (ADMIN)
Codes:
- 200
- 400
- 401
- 404
- 500

### 5.4. Points de collecte

#### GET /points
Codes:
- 200

#### GET /points/{id}
Codes:
- 200
- 400
- 404
- 500

#### GET /points/{id}/status
Retourne notamment: id, adresse, remplissage, full.

Codes:
- 200
- 400

#### GET /points/overloaded (ADMIN)
Codes:
- 200
- 401

#### PUT /points/{id} (utilisateur authentifie)
Codes:
- 200
- 400
- 401
- 404
- 500

#### PATCH /points/{id} (ADMIN)
Codes:
- 200
- 400
- 401
- 404
- 500

#### DELETE /points/{id}/clear (ADMIN)
Codes:
- 200
- 400
- 401
- 404
- 409

### 5.5. Users

#### GET /users/leaderboard
Codes:
- 200
- 400
- 404

#### PUT /users/{id} (ADMIN)
Codes:
- 200
- 400
- 401
- 404
- 500

#### PATCH /users/{id} (ADMIN)
Codes:
- 200
- 400
- 401
- 404
- 500

### 5.6. Import CSV

#### POST /import
Format attendu des colonnes:
id;userid;pointid;poids;wastetypeid;datedepot

Codes:
- 200 (la route renvoie du texte, y compris en cas d'erreur fonctionnelle)
