# TP Jour 2 : L'Orchestre Symphonique

**Durée :** ~4h | **Fil Rouge :** Finalisation de l'environnement complet

---

## Exercice 1 : Le Réseau Sacré (1h)

1. Crée un réseau Docker `ath-network`.
2. Configure tous les services pour qu'ils soient dans ce réseau.
3. Vérifie que ton moteur peut pinguer `kafka` et `cassandra` par leur nom.

---

## Exercice 2 : Résilience au démarrage (1h30)

1. Ajoute des `healthchecks` sérieux à Kafka et Cassandra.
2. Modifie le moteur de clearing pour qu'il ne démarre que quand les deux bases sont prêtes.
3. Teste en lançant tout d'un coup. Observe l'ordre de lancement.

---

## Exercice 3 : Dashboard Automatisé (1h30)

1. Ajoute Prometheus et Grafana au Compose.
2. Utilise les volumes pour injecter automatiquement :
   - Ta configuration `prometheus.yml`.
   - Ton dashboard JSON de la semaine 17.
3. Lance tout. Connecte-toi à Grafana : ton dashboard doit être déjà là avec des données !

**Livrable** : Fichier `docker-compose.yml` final et dossier `config/` contenant tous les fichiers injectés.
