# TP Jour 2 : Chaos au Clearing

**Durée :** ~4h | **Fil Rouge :** Tests de résilience réelle

---

## Point de départ

- Copie le **Kit 18.2** de `starter_kit_s18.md`.
- Écris hypothèse, métriques et condition d'arrêt avant l'injection.
- Exécute ces expériences uniquement sur l'environnement local.

## Exercice 1 : Le "Crash" Cassandra (1h30)

1. Lance ton pipeline complet (Simulator -> Kafka -> Moteur -> Cassandra).
2. Pendant que les transactions défilent, lance `docker compose stop cassandra`.
3. Observe les erreurs, le retry borné, le lag et les offsets non validés.
4. Relance Cassandra avec `docker compose start cassandra`.
5. Vérifie le rattrapage et compte les relectures/doublons détectés.

**Validation :** le rapport compare l'hypothèse aux offsets et aux lignes persistées.

---

## Exercice 2 : Latence Réseau (1h30)

1. Place Toxiproxy entre l'application et Cassandra.
2. Mesure une baseline.
3. Ajoute progressivement 100 ms, 500 ms puis 2 s.
4. Observe débit, p99, lag et timeouts.
5. Retire le toxic et mesure le retour à la normale.

**Validation :** le rapport montre la relation entre latence Cassandra et lag Kafka.

---

## Exercice 3 : Rapport de Résilience (1h)

1. Documente panne, hypothèse, métriques, résultat et récupération.
2. Classe les écarts par sévérité.
3. Propose une amélioration et la preuve qui validerait son efficacité.

**Validation :** une autre personne peut rejouer l'expérience avec le rapport.

**Livrable** : Rapport de tests de chaos et logs prouvant la reprise après incident.
