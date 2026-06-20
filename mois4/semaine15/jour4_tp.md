# TP Jour 4 : La chasse aux doublons

**Durée :** ~4h | **Fil Rouge :** Rendre le clearing idempotent

---

## Point de départ

- Réutilise le **Kit 15.2** et ajoute le **Kit 15.3**.
- Le cache mémoire sert à observer le mécanisme ; il ne constitue pas la solution finale.
- Conserve l'ID métier en `String`.

## Exercice 1 : Commit Manuel (1h30)

1. Dans ton `ClearingConsumer`, désactive l'auto-commit.
2. Ajoute l'appel à `consumer.commitSync()` seulement après avoir affiché : "Validation réussie".
3. Simule un crash (`System.exit(1)`) juste après la validation mais AVANT le commit.
4. Relance et remarque que Kafka te redonne le même message. C'est le "At-Least-Once".

**Validation :** les logs montrent le même topic, la même partition et le même offset avant et après le redémarrage.

---

## Exercice 2 : Registre d'Idempotence (1h30)

1. Intègre `DeduplicationCache.isNew`.
2. Ajoute l'ID seulement après le succès de l'effet métier.
3. Compte les doublons dans une métrique ou un compteur.
4. Redémarre le processus et constate que le cache est vide.
5. Rédige le contrat de la future table Cassandra `processing_state` avec les états `Received`, `Projected` et `Completed`.

**Validation :** le doublon est bloqué pendant la même exécution et réapparaît après redémarrage.

---

## Exercice 3 : Test de Robustesse (1h)

1. Envoie trois fois la même transaction.
2. Vérifie un seul effet métier pendant l'exécution.
3. Provoque un crash entre l'effet et l'ajout dans le cache.
4. Explique pourquoi seule une opération atomique ou idempotente peut fermer cette fenêtre.

**Validation :** le rapport distingue la protection obtenue et la fenêtre de panne restante.

**Livrable** : Code source incluant la gestion manuelle des offsets et un mécanisme de dédoublonnage par ID de transaction.
