# TP Jour 5 : Alerte Rouge au Clearing

**Durée :** ~4h | **Fil Rouge :** Configuration Alertmanager

---

## Exercice 1 : Définir des SLOs (1h)

1. Rédige un document simple définissant 2 SLOs pour ATH :
   - Disponibilité : "Le moteur doit être UP 99.5% du temps".
   - Performance : "95% des transactions doivent être traitées en moins de 200ms".

---

## Exercice 2 : Règles Prometheus (1h30)

1. Crée un fichier `alert_rules.yml`.
2. Définis une règle `EngineDown` basée sur la métrique `up == 0`.
3. Définis une règle `HighErrorRate` basée sur le ratio succès/échecs.

---

## Exercice 3 : Alertmanager Simulation (1h30)

1. Ajoute le service `alertmanager` à ton Docker Compose.
2. Configure-le pour qu'il affiche les alertes dans ses propres logs (ou simule l'envoi d'un webhook).
3. Coupe ton moteur de clearing et attends 2 minutes. Vérifie que l'alerte apparaît bien dans l'interface Prometheus (onglet Alerts) puis dans Alertmanager.

**Livrable** : Fichier `alert_rules.yml` et capture d'écran de l'alerte active dans Prometheus.
