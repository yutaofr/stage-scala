# TP Jour 4 : La chasse aux doublons

**Durée :** ~4h | **Fil Rouge :** Rendre le clearing idempotent

---

## Exercice 1 : Commit Manuel (1h30)

1. Dans ton `ClearingConsumer`, désactive l'auto-commit.
2. Ajoute l'appel à `consumer.commitSync()` seulement après avoir affiché : "Validation réussie".
3. Simule un crash (`System.exit(1)`) juste après la validation mais AVANT le commit.
4. Relance et remarque que Kafka te redonne le même message. C'est le "At-Least-Once".

---

## Exercice 2 : Registre d'Idempotence (1h30)

1. Crée un `Set[String]` nommé `processedTxIds` dans ton moteur.
2. Avant de valider une transaction, vérifie si son ID est déjà dans le Set.
3. Si oui, affiche : "Doublon détecté, ignoré" et ne fais rien d'autre.
4. Si non, traite la transaction et ajoute son ID au Set.

---

## Exercice 3 : Test de Robustesse (1h)

1. Envoie 3 fois la même transaction via ton simulateur (en hackant l'ID).
2. Vérifie que ton moteur n'en comptabilise qu'une seule dans ses totaux finaux.

**Livrable** : Code source incluant la gestion manuelle des offsets et un mécanisme de dédoublonnage par ID de transaction.
