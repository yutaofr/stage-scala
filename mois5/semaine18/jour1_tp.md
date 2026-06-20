# TP Jour 1 : 100 000 Transactions par Minute

**Durée :** ~4h | **Fil Rouge :** Scénario Gatling

---

## Point de départ

- Copie le **Kit 18.1** de `starter_kit_s18.md`.
- Lance l'adaptateur de charge, Kafka, le moteur et l'observabilité.
- Fixe une seed et garde la même distribution de données entre deux essais.

## Exercice 1 : Installation de Gatling (1h)

1. Ajoute le plugin `gatling-sbt` à ton projet.
2. Crée une classe `BasicSimulation` qui étend `Simulation`.
3. Configure le protocole HTTP vers `http://localhost:8080`.
4. Exécute un test d'un seul utilisateur avant toute montée en charge.

**Validation :** une requête Gatling crée exactement un record dans `clearing-input`.

---

## Exercice 2 : Définition du Scénario (1h30)

1. Crée un feeder avec des IDs uniques et une seed fixe.
2. Envoie une transaction par requête.
3. Configure une charge ouverte progressive, par exemple de 10 à 100 utilisateurs démarrés par seconde.
4. Ajoute des assertions sur le taux d'échec et la p95.

**Validation :** le test échoue automatiquement si les seuils du laboratoire sont dépassés.

---

## Exercice 3 : Analyse du Point de Rupture (1h30)

1. Lance trois paliers de charge, du plus faible au plus élevé.
2. Pour chaque palier, relève débit, p50, p95, p99, erreurs et lag Kafka.
3. Corrèle le premier SLO dépassé avec CPU, GC, Kafka et Cassandra.
4. Identifie le goulot avec une preuve.

**Validation :** le rapport contient la charge maximale soutenue et la méthode de calcul.

**Livrable** : Rapport Gatling HTML (zippé) et un court texte expliquant le goulot d'étranglement identifié (CPU ? I/O ?).
