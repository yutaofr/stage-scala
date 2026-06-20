# TP Jour 3 : Le Concierge du Temps Réel

**Durée :** ~4h | **Fil Rouge :** Transformer le moteur de clearing en Consumer

---

## Exercice 1 : Le Consumer Simple (Starter Kit)

> [!TIP]
> **Starter Kit fourni :** utilise le **Kit 15.2** et le **Kit 15.3**, fichier `distributed/kafka/KafkaPipeline.scala`.

1. Ouvre le squelette et repère `subscribe`, `poll`, traitement, publication, commit et fermeture.
2. Complète les tests du `RecordProcessor`.
3. Pour ce premier exercice, journalise clé, partition, offset et valeur.
4. Ajoute un hook d'arrêt qui ferme le consumer.

**Validation :** `Ctrl+C` ferme le consumer sans stacktrace et sans attendre le timeout maximal.

---

## Exercice 2 : Branchement du Domaine (1h30)

1. Transforme chaque JSON en `Transaction`.
2. Appelle le validateur v2.4.
3. Publie le résultat valide dans `clearing-output`.
4. Pour un JSON invalide, publie le payload et la cause dans `clearing-dlq` avant d'autoriser le commit.

**Validation :** un record valide atteint `clearing-output` ; un record invalide atteint `clearing-dlq`.

---

## Exercice 3 : Test de Reprise (1h)

1. Envoie dix messages puis traite-les jusqu'au commit.
2. Redémarre : aucun ancien message ne doit être rejoué.
3. Recommence en arrêtant le consumer après l'effet métier mais avant le commit.
4. Redémarre et observe le record relu.
5. Explique pourquoi ce doublon est normal en at-least-once.

**Validation :** les deux scénarios sont reproduits avec les offsets correspondants.

**Livrable** : Code source du consumer asynchrone intégré avec la logique de validation du moteur de clearing.
