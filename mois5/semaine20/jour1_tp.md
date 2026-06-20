# TP Jour 1 : Le Grand Ménage

**Durée :** ~4h | **Fil Rouge :** Polissage et README final

---

## Point de départ

- Copie le **Kit 20.1** de `starter_kit_s20.md`.
- Travaille depuis le candidat `v4.0.0-rc1`.
- Ne supprime aucun volume ou objet Docker hors du projet.

## Exercice 1 : Chasse aux Warnings (1h30)

1. Lance compilation et tests avec les options strictes déjà choisies par le projet.
2. Classe les warnings : bug probable, dette planifiée ou faux positif.
3. Corrige les warnings bloquants.
4. Documente toute exception temporaire avec une issue.

**Validation :** aucune suppression globale de warning et aucune exception non tracée.

---

## Exercice 2 : Documentation d'Exécution (1h30)

1. Rédige un `README.md` contenant :
   - Un schéma d'architecture.
   - Les pré-requis (Docker, Java).
   - Les commandes pour lancer la démo.
   - Comment accéder au Dashboard Grafana et au Swagger.
   - Comment arrêter et nettoyer uniquement la stack du projet.
2. Ajoute une section « limites connues ».
3. Fais relire les commandes par une autre personne.

**Validation :** le lecteur lance la stack sans explication orale.

---

## Exercice 3 : Test de "Boîte Noire" (1h)

1. Exécute `docker compose down -v --remove-orphans` dans le projet.
2. Vérifie `docker compose config`.
3. Suis exactement le README depuis cet état.
4. Exécute le scénario de smoke test du kit.
5. Corrige toute étape ambiguë.

**Validation :** le smoke test retourne un code de sortie nul.

**Livrable** : Code source final (propre) et le fichier `README.md` complet.
