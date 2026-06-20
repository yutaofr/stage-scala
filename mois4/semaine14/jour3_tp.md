# TP Jour 3 : Zéro Fuite de Ressources

**Durée :** ~4h | **Fil Rouge :** Lecture de batchs CSV avec détection d'erreurs

---

## Point de départ

- Copie le **Kit 14.3** de `starter_kit_s14.md`.
- Prépare un CSV avec une ligne valide, une ligne invalide et une seconde ligne valide.
- Le test doit prouver la fermeture ; il ne suffit pas de supposer qu'elle a eu lieu.

## Exercice 1 : Le Lecteur Sécurisé (1h30)

1. Implémente `openSource(path): ZIO[Scope, IOException, Source]`.
2. Utilise `ZIO.acquireRelease` et convertis l'erreur d'ouverture en `IOException`.
3. Consomme les lignes à l'intérieur du scope ; ne retourne pas un `Iterator` après fermeture.
4. Ajoute un compteur ou un log dans le finaliseur.

**Validation :** toutes les lignes sont lues et le finaliseur s'exécute une seule fois.

---

## Exercice 2 : Crash Test (1h30)

1. Lis les lignes jusqu'à la première erreur de parsing.
2. Retourne `ZIO.fail(ParseError(...))` sans lever volontairement une exception.
3. Vérifie que le finaliseur s'exécute aussi sur ce chemin.
4. Répète le test en interrompant la Fiber de lecture.

**Validation :** succès, échec typé et interruption ferment tous la ressource.

---

## Exercice 3 : Ressources Multiples (1h)

1. Crée un programme qui copie un fichier source vers une destination temporaire.
2. Acquiers le lecteur puis le writer dans le même scope.
3. Provoque une erreur après quelques lignes.
4. Vérifie la fermeture des deux ressources et supprime le fichier temporaire.

**Validation :** aucun descripteur ne reste ouvert et le fichier temporaire est nettoyé.

**Livrable** : Code source montrant l'usage de `Scope` pour la manipulation de fichiers, avec preuve de fermeture en cas d'erreur.
