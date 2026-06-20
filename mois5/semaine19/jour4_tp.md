# TP Jour 4 : Le Swagger du Clearing

**Durée :** ~4h | **Fil Rouge :** Documentation interactive avec Tapir

---

## Point de départ

- Copie le **Kit 19.4** de `starter_kit_s19.md`.
- Réutilise les types de domaine ; ne crée pas un second modèle incompatible.
- Décris aussi les erreurs et statuts HTTP.

## Exercice 1 : Installation de Tapir (1h)

1. Ajoute les dépendances `sttp.tapir`.
2. Définis un premier endpoint `health` qui renvoie "OK".
3. Intègre-le avec l'interpréteur serveur choisi.
4. Ajoute un test HTTP de `GET /health`.

**Validation :** le test vérifie statut et body.

---

## Exercice 2 : Décrire le Clearing (1h30)

1. Crée `GET /banks/{id}/position` et `GET /clearing/history?date=...`.
2. Décris les paramètres et les réponses JSON.
3. Modélise `404`, erreur de validation et erreur interne.
4. Branche les endpoints au repository de la S16.

**Validation :** les cas succès, banque inconnue et date invalide sont testés.

---

## Exercice 3 : Génération de la Doc (1h30)

1. Génère les endpoints Swagger avec `SwaggerInterpreter`.
2. Expose `/docs` et le document OpenAPI.
3. Vérifie que les schémas et les erreurs apparaissent.
4. Teste une lecture depuis Swagger UI.

**Validation :** le fichier OpenAPI est versionné et sa génération est reproductible.

**Livrable** : Lien (ou capture d'écran) vers ton Swagger UI fonctionnel et montrant les schémas de données du clearing.
