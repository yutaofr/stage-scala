# TP Jour 5 : Répétition Générale

**Durée :** ~4h | **Fil Rouge :** Simulation de soutenance

---

## Point de départ

- Utilise le **Kit 19.5** de `starter_kit_s19.md`.
- Travaille sur un candidat de livraison, pas sur le tag final.
- Toute suppression doit être justifiée par les tests et l'historique Git.

## Exercice 1 : Polissage du Code (1h)

1. Relis ton code une dernière fois.
2. Supprime uniquement le code mort confirmé.
3. Exécute formatage, compilation et tests.
4. Vérifie les logs et la documentation.

**Validation :** la checklist contient la commande et le résultat de chaque contrôle.

---

## Exercice 2 : Scénario "Live" (1h30)

1. Prépare un terminal avec :
   - `docker compose logs -f engine`
   - Un navigateur ouvert sur Grafana.
   - Un navigateur ouvert sur Swagger.
2. Lance le simulateur et vérifie que les preuves apparaissent au moment prévu dans le script.

**Validation :** la répétition repart d'un environnement vide et suit un script chronométré.

---

## Exercice 3 : Rédaction des Points Forts (1h30)

1. Liste cinq décisions avec contexte, compromis et preuve.
2. Prépare la comparaison ZIO/Future/threads sans présenter ZIO comme une solution universelle.
3. Prépare deux limites connues et leur suite proposée.

**Validation :** chaque point fort s'appuie sur un artefact visible.

**Livrable** : candidat `v4.0.0-rc1`, checklist de revue et plan de présentation.
