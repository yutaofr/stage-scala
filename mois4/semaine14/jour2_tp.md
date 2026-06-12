# TP Jour 2 : Architecturer avec ZLayer

**Durée :** ~4h | **Fil Rouge :** Refactorisation du ClearingEngine en ZIO

---

## Exercice 1 : Le Service de Taux ZIO (1h30)

## Exercice 1 : Modularisation du Code (Starter Kit)

> [!TIP]
> **Nouveau Format Starter Kit :** ZLayer peut être frustrant au début avec les erreurs `Requires Env`. Dans `exercises/s14/ZLayerApp.scala`, le `Main` est déjà branché. Concentre-toi uniquement sur l'écriture des Layers !

1. Reprends ton code de calcul de frais d'hier (S14-J1).
2. Ouvre le squelette `ZLayerApp.scala`. Tu y trouveras un service vide `ZioExchangeRateService` qui te donne le taux de conversion EUR/MAD.
2. Implémente la méthode `process(tx: Transaction)` qui appelle le service de taux.
3. Écris un programme principal (`run`) qui assemble les couches et lance un calcul de test.

---

## Exercice 2 : Injection dans le Moteur (1h30)

1. Crée un service `ClearingService` qui dépend de `ExchangeRateService` via son canal **R**.
2. Implémente la méthode `process(tx: Transaction)` qui appelle le service de taux.
3. Écris un programme principal (`run`) qui assemble les couches et lance un calcul de test.

---

## Exercice 3 : Switch vers le Mock (1h)

1. Crée une implémentation `MockExchangeRateService` qui renvoie toujours un taux fixe de `10.0`.
2. Modifie ton programme principal pour utiliser cette couche de mock au lieu de la couche réelle.
3. Observe la facilité avec laquelle tu as changé le comportement système sans toucher à la logique métier.

**Livrable** : Code source structuré en couches ZIO avec démonstration de substitution de services.
