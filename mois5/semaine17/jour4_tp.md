# TP Jour 4 : Création du Cockpit

**Durée :** ~4h | **Fil Rouge :** Dashboard Grafana pour le Clearing

---

## Exercice 1 : Setup Grafana (1h)

1. Ajoute le service `grafana` à ton `docker-compose.yml`.
2. Connecte-toi sur `http://localhost:3000` (admin/admin).
3. Ajoute Prometheus comme "Data Source".

---

## Exercice 2 : Création des Panels (2h)

1. Crée un nouveau Dashboard.
2. Ajoute un panel pour le **Débit de Transactions** (Transactions/sec).
3. Ajoute un panel pour le **Taux d'Erreur** (Succès vs Échecs).
4. Ajoute un panel pour la **Consommation Mémoire** de la JVM.

---

## Exercice 3 : Variable de Templating (1h)

1. Ajoute une variable Grafana `$bank`.
2. Modifie tes panneaux pour qu'ils se filtrent dynamiquement quand on change la banque dans le menu déroulant en haut du dashboard.

**Livrable** : Export JSON du dashboard Grafana et capture d'écran montrant le dashboard rempli avec les données du simulateur.
