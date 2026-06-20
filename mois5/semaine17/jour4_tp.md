# TP Jour 4 : Création du Cockpit

**Durée :** ~4h | **Fil Rouge :** Dashboard Grafana pour le Clearing

---

## Point de départ

- Copie le **Kit 17.4** de `starter_kit_s17.md`.
- Versionne le dashboard JSON et son provisioning.
- Affiche les unités dans chaque panel.

## Exercice 1 : Setup Grafana (1h)

1. Ajoute le service `grafana` à ton `docker-compose.yml`.
2. Connecte-toi sur `http://localhost:3000` (admin/admin).
3. Ajoute Prometheus comme "Data Source".

**Validation :** le test de datasource réussit sans configuration manuelle après recréation du conteneur.

---

## Exercice 2 : Création des Panels (2h)

1. Crée les panels débit, taux d'erreur, p99, lag Kafka et mémoire JVM.
2. Donne à chaque panel une unité et une fenêtre.
3. Ajoute une valeur « aucune donnée » explicite.
4. Compare un incident Cassandra avec la ligne temporelle des métriques.

**Validation :** le dashboard permet de dater l'incident et d'identifier son impact.

---

## Exercice 3 : Variable de Templating (1h)

1. Ajoute `$status` à partir des labels réellement disponibles.
2. N'ajoute `$bank` que si le nombre de banques reste borné et utile.
3. Vérifie que la sélection « All » produit une requête correcte.

**Validation :** la variable ne génère pas de séries non maîtrisées.

**Livrable** : Export JSON du dashboard Grafana et capture d'écran montrant le dashboard rempli avec les données du simulateur.
