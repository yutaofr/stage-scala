# TP Jour 2 : Architecturer avec ZLayer

**Durée :** ~4h | **Fil Rouge :** Refactorisation du ClearingEngine en ZIO

---

## Point de départ

- Pars du **Kit 14.0**, puis copie le **Kit 14.2** de `starter_kit_s14.md`.
- Réutilise les types métier du Clearing Engine v2.3.
- Commence avec les couches `mock`, puis branche les couches `live`.

## Exercice 1 : Modularisation du code (1h30)

> [!TIP]
> Le runner et les interfaces sont fournis. Concentre-toi sur les implémentations et sur l'assemblage.

1. Ouvre `distributed/zio/ClearingLayers.scala`.
2. Implémente `TransactionRepository.mock` avec deux transactions déterministes.
3. Implémente `ValidationService.live` en déléguant au validateur pur de la S12.
4. Implémente `NettingService.live` en déléguant au calcul de netting pur.
5. Lance le programme uniquement avec des couches de test.

**Validation :** le rapport contient les deux banques attendues et la somme des positions vaut zéro.

---

## Exercice 2 : Injection dans le Moteur (1h30)

**Objectif :** rendre la dépendance au taux de change explicite.

1. Ajoute l'interface `ExchangeRateService`.
2. Crée un effet `convertToMad` de type `ZIO[ExchangeRateService, RateError, Transaction]`.
3. Utilise `ZIO.serviceWithZIO` pour appeler le service.
4. Ajoute la couche au `provide` sans modifier la logique de validation.

**Validation :** supprimer la couche provoque une erreur de compilation ; la remettre permet l'exécution.

---

## Exercice 3 : Switch vers le Mock (1h)

1. Crée `ExchangeRateService.fixed(BigDecimal("10.0"))`.
2. Crée une seconde couche qui renvoie une erreur déterministe.
3. Exécute le même programme avec les deux couches, sans modifier `convertToMad`.
4. Ajoute un test ou une sortie qui compare le résultat et l'erreur attendus.

**Validation :** les deux scénarios utilisent le même programme métier et produisent des résultats reproductibles.

**Livrable** : Code source structuré en couches ZIO avec démonstration de substitution de services.
