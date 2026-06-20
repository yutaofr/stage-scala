# TP Jour 1 : Des Logs pour les Machines

**Durée :** ~4h | **Fil Rouge :** Logback & JSON Layout

---

## Point de départ

- Copie le **Kit 17.1** de `starter_kit_s17.md`.
- N'ajoute jamais un montant, un nom de client ou un payload complet sans règle de masquage.
- Utilise `txId`, partition et offset comme champs structurés.

## Exercice 1 : Installation de Logback (1h)

1. Ajoute le backend SLF4J/Logback choisi dans le kit.
2. Crée `logback.xml`.
3. Configure un format lisible en local.
4. Vérifie qu'un seul backend SLF4J est présent.

**Validation :** le démarrage ne contient aucun avertissement de binding multiple.

---

## Exercice 2 : Passage au JSON (1h30)

1. Active l'encodeur JSON.
2. Journalise début, succès et échec d'une transaction.
3. Vérifie niveau, timestamp, message, service et environnement.
4. Prévois deux configurations : local lisible et production JSON.

**Validation :** chaque ligne est un objet JSON valide et les champs stables gardent le même nom.

---

## Exercice 3 : Mapped Diagnostic Context (MDC) (1h30)

1. Utilise `ZIO.logAnnotate` ou le contexte ZIO Logging fourni.
2. Forke deux traitements avec deux `txId` différents.
3. Produis plusieurs logs après des suspensions.
4. Vérifie qu'aucun ID ne fuit vers l'autre Fiber.
5. Enveloppe le `DurableProcessor` de la S16 ; ne crée pas un pipeline factice réservé aux logs.

**Validation :** chaque ligne garde le bon `txId`, même quand les Fibers s'entrelacent.

**Livrable** : Fichier `logback.xml` configuré et un extrait de log JSON généré par le moteur.
