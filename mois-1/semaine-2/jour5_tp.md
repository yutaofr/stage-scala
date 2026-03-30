# TP Jour 5 : Clearing Engine v0.2 — Assemblage Final

**Durée :** ~4h | **Fil Rouge :** Une application qui lit, branche et calcule

---

## Exercice 1 : Le Compagnon de Transaction (1h)

1. Dans `src/main/scala/clearing/Transaction.scala`, crée un `trait Transaction` (ou utilise le tuple pour l'instant si tu n'as pas encore créé de classe).
2. Crée `object Transaction`.
3. Ajoute une méthode `apply(line: String): (String, String, BigDecimal)` qui utilise ton code de parsing du Jour 3.

---

## Exercice 2 : Refactoring du Moteur (1h30)

1. Modifie `ClearingEngine.scala` pour qu'il soit plus modulaire.
2. Il doit utiliser le `trait ClearingProcessor` défini hier.
3. Intègre le parsing automatique via `Transaction(line)`.

---

## Exercice 3 : Moteur v0.2 (1h)

Ton application complète doit maintenant :
1. Lire un fichier `transactions.csv` (simulé ou réel).
2. Parser chaque ligne.
3. Filtrer les transactions invalides.
4. Calculer le solde net par banque.
5. Afficher le rapport final.

---

## Exercice 4 : Tests d'Intégration (30 min)

1. Crée `IntegrationSpec.scala`.
2. Prépare un petit fichier CSV de test.
3. Lance le pipeline complet et vérifie que le solde final correspond au calcul manuel.

**Note de Fin de Semaine** : Félicitations ! Ton moteur est désormais capable de traiter des flux de données externes de manière structurée et résiliente.

**Bilan Semaine 2 :** 
- Tu maîtrises les structures complexes (Tuples).
- Tu sais extraire des données avec Regex.
- Tu as construit une architecture modulaire avec des Traits.
- Tu gères la création d'objets via les Companion Objects.
