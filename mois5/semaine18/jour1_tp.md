# TP Jour 1 : 100 000 Transactions par Minute

**Durée :** ~4h | **Fil Rouge :** Scénario Gatling

---

## Exercice 1 : Installation de Gatling (1h)

1. Ajoute le plugin `gatling-sbt` à ton projet.
2. Crée une classe `BasicSimulation` qui étend `Simulation`.
3. Configure un protocole HTTP pointant vers ton moteur de clearing (ex: endpoint d'ingestion).

---

## Exercice 2 : Définition du Scénario (1h30)

1. Crée un scénario qui :
   - Génère une transaction aléatoire.
   - L'envoie au moteur.
   - Attend 100ms.
2. Configure l'injection : `rampUsers(1000).during(10.seconds)`.

---

## Exercice 3 : Analyse du Point de Rupture (1h30)

1. Lance le test : `sbt gatling:test`.
2. Ouvre le rapport HTML.
3. Observe le graphique de latence. À quel moment la latence dépasse-t-elle 1 seconde ?
4. Regarde tes logs moteur (JSON) et tes métriques Grafana pendant le test. Que vois-tu ?

**Livrable** : Rapport Gatling HTML (zippé) et un court texte expliquant le goulot d'étranglement identifié (CPU ? I/O ?).
