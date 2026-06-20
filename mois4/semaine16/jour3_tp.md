# TP Jour 3 : Le Grand Assemblage

**Durée :** ~4h | **Fil Rouge :** Intégration Kafka + ZIO + Cassandra

---

## Point de départ

- Utilise le **Kit 16.3** et les composants validés en S15.
- Le commit Kafka intervient uniquement après la persistance des données nécessaires.
- Chaque log doit contenir `txId`, partition et offset.

## Exercice 1 : Le Consumer-Persister (2h)

1. Pars du programme fourni `distributed.pipeline.ClearingPipelineApp`.
2. Il doit écouter le topic `clearing-input`.
3. Pour chaque transaction valide :
   - Persiste la transaction source avec son ID.
   - Met à jour les projections idempotentes.
   - Publie ou journalise le résultat.
   - Valide l'offset après succès.
4. Pour un doublon, vérifie les projections puis valide l'offset.

**Validation :** la trace d'un record montre l'ordre `persist → project → commit`.

---

## Exercice 2 : Gestion de Panne (1h)

1. Coupe Cassandra pendant le traitement.
2. Vérifie que les offsets concernés ne sont pas validés.
3. Utilise un retry borné puis laisse échouer l'effet afin d'éviter une boucle infinie silencieuse.
4. Relance Cassandra et redémarre le consumer.
5. Vérifie la relecture et l'absence de double projection.

**Validation :** les offsets et les lignes Cassandra prouvent la reprise.

---

## Exercice 3 : Monitoring Réel (1h)

1. Envoie cinq cents transactions dont dix doublons et cinq invalides.
2. Compare les comptes : reçues, valides uniques, doublons, invalides.
3. Mesure le lag final du groupe.
4. Vérifie l'invariant de netting.

**Validation :** tous les comptes s'équilibrent et le lag revient à zéro.

**Livrable** : Code source de l'application connectée de bout en bout et rapport sur le test de crash/reprise.
