# TP Jour 2 : Chaos au Clearing

**Durée :** ~4h | **Fil Rouge :** Tests de résilience réelle

---

## Exercice 1 : Le "Crash" Cassandra (1h30)

1. Lance ton pipeline complet (Simulator -> Kafka -> Moteur -> Cassandra).
2. Pendant que les transactions défilent, lance : `docker-compose stop cassandra`.
3. Observe tes logs moteur : tu dois voir les erreurs ZIO et les tentatives de `retry`.
4. Relance Cassandra : `docker-compose start cassandra`.
5. Vérifie que le moteur a rattrapé son retard sans doublon (grâce à l'idempotence).

---

## Exercice 2 : Latence Réseau (1h30)

1. Installe `Toxiproxy` (ou simule avec un `sleep` conditionnel dans ton DAO).
2. Injecte une latence de 2 secondes sur chaque écriture Cassandra.
3. Observe l'impact sur ton dashboard Grafana : le débit s'écroule.
4. **Conclusion** : Pourquoi la latence de la base impacte-t-elle le débit de mon consumer ?

---

## Exercice 3 : Rapport de Résilience (1h)

1. Rédige un court rapport listant 3 types de pannes testées et le comportement observé.
2. Propose 1 amélioration pour rendre le système encore plus solide.

**Livrable** : Rapport de tests de chaos et logs prouvant la reprise après incident.
