# TP Jour 3 : Exhaustivité & Sécurité

**Durée :** ~4h | **Fil Rouge :** Un moteur qui n'oublie jamais rien

---

## Exercice 1 : Routeur de Statuts (1h30)

1. Crée un objet `TransactionRouter`.
2. Implémente une fonction `route(tx: Transaction): String`.
3. Utilise un `match` sur `tx.status`.
4. Pour chaque cas (`Pending`, `Validated`, `Rejected`), retourne un message spécifique.
5. **Défi** : Ajoute un cas `case _` à la fin, puis retire-le. Observe si le compilateur te prévient (si tu as bien couvert tous les cas de l'enum).

---

## Exercice 2 : Reporter d'Erreurs Avancé (1h30)

1. Reprends ta hiérarchie `ClearingError`.
2. Crée un `ErrorReporter` qui prend une `List[ClearingError]`.
3. Pour chaque erreur de la liste, utilise le pattern matching pour générer un message détaillé.
4. Utilise la déstructuration pour extraire le `field` et le `message` de `ValidationError`.

---

## Exercice 3 : Test de Robustesse (1h)

1. Ajoute une nouvelle valeur `Suspicious` à ton enum `TransactionStatus`.
2. Recompile ton projet.
3. Le `TransactionRouter` devrait maintenant avoir un warning de compilation.
4. Corrige le code pour gérer ce nouveau cas.

**Livrable** : Code source montrant une gestion exhaustive des ADTs, avec preuve de la correction suite à l'ajout d'un nouveau statut.
