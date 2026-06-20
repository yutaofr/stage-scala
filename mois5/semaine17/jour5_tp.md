# TP Jour 5 : Alerte Rouge au Clearing

**Durée :** ~4h | **Fil Rouge :** Configuration Alertmanager

---

## Point de départ

- Copie le **Kit 17.5** de `starter_kit_s17.md`.
- Distingue le seuil de démonstration du SLO proposé.
- Chaque alerte doit contenir une description et une action.

## Exercice 1 : Définir des SLOs (1h)

1. Définis deux SLI à partir des métriques disponibles.
2. Propose une cible et une période pour chacun.
3. Calcule l'error budget correspondant.
4. Liste les limites de mesure : absence de trafic, données invalides, maintenance.

**Validation :** chaque SLO contient indicateur, population, cible, fenêtre et source de données.

---

## Exercice 2 : Règles Prometheus (1h30)

1. Pars du fichier fourni.
2. Ajoute `for:` pour éviter les alertes transitoires.
3. Protège le ratio d'erreur contre une division par zéro.
4. Ajoute labels de sévérité et annotations.

**Validation :** `promtool check rules` accepte le fichier.

---

## Exercice 3 : Alertmanager Simulation (1h30)

1. Lance Alertmanager et vérifie sa configuration.
2. Déclenche `EngineDown`.
3. Vérifie les états pending, firing puis resolved.
4. Teste le routage vers un webhook local.
5. Ajoute au livrable la procédure de résolution.

**Validation :** la même alerte est visible dans Prometheus, Alertmanager et le webhook.

**Livrable** : Fichier `alert_rules.yml` et capture d'écran de l'alerte active dans Prometheus.
