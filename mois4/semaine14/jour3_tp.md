# TP Jour 3 : Zéro Fuite de Ressources

**Durée :** ~4h | **Fil Rouge :** Lecture de batchs CSV avec détection d'erreurs

---

## Exercice 1 : Le Lecteur Sécurisé (1h30)

1. Implémente une fonction `openSource(path: String): ZIO[Scope, Throwable, Iterator[String]]`.
2. Utilise `ZIO.acquireRelease` pour ouvrir un `scala.io.Source` et appeler `.close()` à la fin.
3. Teste ton lecteur sur un petit fichier.

---

## Exercice 2 : Crash Test (1h30)

1. Crée un programme qui commence à lire un fichier mais s'arrête brutalement avec un `ZIO.fail` après la 5ème ligne.
2. Ajoute un print dans ton bloc `release`.
3. Vérifie que le print s'affiche bien, prouvant que le fichier a été fermé malgré l'échec.

---

## Exercice 3 : Ressources Multiples (1h)

1. Crée un programme qui copie un fichier source vers un fichier destination.
2. Utilise deux `acquireRelease` dans le même `for-yield`.
3. Vérifie que les deux ressources sont gérées séparément et proprement.

**Livrable** : Code source montrant l'usage de `Scope` pour la manipulation de fichiers, avec preuve de fermeture en cas d'erreur.
