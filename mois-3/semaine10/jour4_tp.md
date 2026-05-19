# TP Jour 4 : Blindage du Moteur avec Try

**Durée :** ~4h | **Fil Rouge :** Sécuriser les briques de la Semaine 8

---

## Exercice 1 : Parsing CSV ultra-robuste (1h30)

1. Reprends ton parser `Transaction.fromCsv`.
2. Encapsule tout l'intérieur du parsing dans un `Try(...)`.
3. Convertis ce `Try` en `Either[ParsingError, Transaction]`.
4. Teste avec des données corrompues (fichiers vides, formats de nombres invalides). Vérifie que rien ne "plante".

---

## Exercice 2 : Sécurisation du Hash & HTTP (1h30)

1. Reprends tes services `SecurityUtils` (Hashing) et `ExchangeRateService` (HTTP).
2. Encapsule les appels `MessageDigest.getInstance` et `client.send` dans des `Try`.
3. Assure-toi que si l'API de change est hors-ligne, tu reçois un `Failure` (ou un `Left`) et pas un crash complet du thread.

---

## Exercice 3 : Le Catch-All final (1h)

1. Dans ton point d'entrée principal (`ClearingApp`), ajoute un wrapper `Try` autour du traitement complet.
2. Utilise `recover` ou `recoverWith` pour proposer un message de secours si une erreur inconnue survient.
3. Observe comment l'imprédictibilité est devenue une donnée gérée.

**Livrable** : Code source où tous les appels risqués (I/O, Reflection, Parsing) sont protégés par le type `Try`.
