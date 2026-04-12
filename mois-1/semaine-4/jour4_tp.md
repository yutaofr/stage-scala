# TP Jour 4 : Algorithmes Bancaires Récursifs

**Durée :** ~4h | **Fil Rouge :** Analyse de flux et détection de seuils

---

## Exercice 1 : Le Relevé Cumulé (1h30)

1. Tu as une liste de montants : `List(100, -50, 200, -100)`.
2. Crée une fonction récursive terminale `calculerSoldeHistorique(txs: List[BigDecimal]): List[BigDecimal]`.
3. Elle doit retourner la liste des soldes à chaque étape.
   Exemple : `List(100, 50, 250, 150)`.
4. Utilise un accumulateur et `@tailrec`.

---

## Exercice 2 : Détection d'Incident (1h30)

1. Un compte ne doit jamais passer en dessous de -500 DH.
2. Crée une fonction récursive `trouverMomentIncident(txs: List[BigDecimal], soldeInitial: BigDecimal): Option[Int]`.
3. Elle retourne le numéro de la transaction (son index) qui a provoqué l'incident.
4. **Important** : La fonction doit s'arrêter dès que l'incident est trouvé (arrêt précoce).

---

## Exercice 3 : Recherche de Signature (1h)

1. Chaque transaction a une signature (String).
2. Crée une fonction récursive pour vérifier si une signature précise existe dans une liste immense sans utiliser les méthodes natives de List (pour t'entraîner à la manipulation `head :: tail`).

---

## Exercice 4 : Fibonacci Bancaire (Optionnel)

1. Calcule la suite de Fibonacci de manière récursive terminale.
2. Pourquoi est-ce utile d'utiliser un accumulateur ici plutôt que la formule mathématique simple `f(n-1) + f(n-2)` ?

**Livrable** : Code des fonctions récursives terminales avec annotation `@tailrec` et tests unitaires validant les comportements.
