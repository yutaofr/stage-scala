# TP Jour 2 : L'Orchestre Symphonique

**Durée :** ~4h | **Fil Rouge :** Finalisation de l'environnement complet

---

## Point de départ

- Copie le **Kit 19.2** de `starter_kit_s19.md`.
- Commence par `docker compose config`.
- Compose crée déjà un réseau par défaut ; ajoute un réseau nommé seulement si cela sert l'isolation ou la lisibilité.

## Exercice 1 : Réseau et résolution DNS (1h)

1. Crée un réseau Docker `ath-network`.
2. Configure tous les services pour qu'ils soient dans ce réseau.
3. Vérifie la résolution DNS et la connexion avec les clients Kafka/Cassandra ou leurs healthchecks ; `ping` peut être absent des images minimales.

**Validation :** l'application se connecte avec les noms `kafka` et `cassandra`, sans `localhost`.

---

## Exercice 2 : Résilience au démarrage (1h30)

1. Ajoute des healthchecks utilisant les outils natifs.
2. Utilise `depends_on.condition: service_healthy` pour le laboratoire.
3. Conserve un retry applicatif borné.
4. Teste démarrage nominal puis dépendance lente.

**Validation :** le moteur attend la readiness et produit une erreur explicite si le délai maximal expire.

---

## Exercice 3 : Dashboard Automatisé (1h30)

1. Ajoute Prometheus et Grafana au Compose.
2. Utilise les volumes pour injecter automatiquement :
   - Ta configuration `prometheus.yml`.
   - Ton dashboard JSON de la semaine 17.
3. Lance tout. Connecte-toi à Grafana : ton dashboard doit être déjà là avec des données !

**Validation :** après `down -v` puis `up --wait`, datasource et dashboard sont provisionnés sans clic manuel.

**Livrable** : Fichier `docker-compose.yml` final et dossier `config/` contenant tous les fichiers injectés.
