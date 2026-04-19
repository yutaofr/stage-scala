# TP Jour 1 : Sécurisation & Interop Java

**Durée :** ~4h | **Fil Rouge :** Anonymiser les données sensibles du clearing

---

## Exercice 1 : Utilisation de MessageDigest (1h30)

1. Crée un objet `SecurityUtils` dans ton projet.
2. Implémente une fonction `hashIban(iban: String): String`.
3. Utilise la classe Java `java.security.MessageDigest` avec l'algorithme "SHA-256".
4. Convertis le tableau d'octets résultant en une chaîne hexadécimale lisible.

---

## Exercice 2 : Anonymisation du flux (1h30)

1. Modifie ton parser CSV ou ton reporter.
2. Avant d'afficher ou de logger une transaction, remplace l'IBAN par sa version hashée (ex: `MA123...` -> `af234...`).
3. Vérifie que deux IBAN identitiques produisent toujours le même hash (déterminisme).

---

## Exercice 3 : Manipulation de temps Java (1h)

1. Utilise `java.time.ZonedDateTime` pour ajouter un horodatage précis à chaque transaction.
2. Formate cet horodatage selon le standard bancaire marocain (JJ/MM/AAAA HH:MM:SS) en utilisant `java.time.format.DateTimeFormatter`.

**Livrable** : Code source montrant l'utilisation de `MessageDigest` et `ZonedDateTime` pour sécuriser et dater les transactions du moteur.
