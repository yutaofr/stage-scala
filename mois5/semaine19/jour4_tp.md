# TP Jour 4 : Le Swagger du Clearing

**Durée :** ~4h | **Fil Rouge :** Documentation interactive avec Tapir

---

## Exercice 1 : Installation de Tapir (1h)

1. Ajoute les dépendances `sttp.tapir`.
2. Définis un premier endpoint `health` qui renvoie "OK".
3. Intègre-le dans ton serveur HTTP ZIO existant.

---

## Exercice 2 : Décrire le Clearing (1h30)

1. Crée un endpoint `ingest` qui prend une `case class Transaction` en body.
2. Ajoute des descriptions aux champs (ex: "ID unique de la transaction").
3. Configure la réponse pour renvoyer un objet JSON `ClearingResult`.

---

## Exercice 3 : Génération de la Doc (1h30)

1. Utilise `SwaggerInterpreter` pour générer les routes de documentation.
2. Lance ton moteur et va sur `http://localhost:8080/docs`.
3. Teste l'envoi d'une transaction directement depuis l'interface Swagger.

**Livrable** : Lien (ou capture d'écran) vers ton Swagger UI fonctionnel et montrant les schémas de données du clearing.
