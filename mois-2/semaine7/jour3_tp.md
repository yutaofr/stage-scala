# TP Jour 3 : Segmentation & Fenêtrage

**Durée :** ~4h | **Fil Rouge :** Affiner le traitement des lots de clearing

---

## Exercice 1 : Partitionnement des Flux (1h)

1. Prends une liste de 100 transactions contenant des statuts variés.
2. Utilise `partition` pour séparer les transactions `Validated` des autres.
3. Affiche le montant total des transactions validées vs le montant total des transactions en erreur.

---

## Exercice 2 : Traitement par Paquets (1h30)

1. Imagine que le système de règlement ne peut traiter que 10 transactions à la fois.
2. Utilise `grouped(10)` pour découper ta liste.
3. Pour chaque paquet, calcule le solde net partiel et affiche : `[Batch] Netting partiel : X DH`.

---

## Exercice 3 : Détection de Fraude par Fenêtre (1h30)

1. Trie tes transactions par timestamp.
2. Utilise `sliding(5)` pour analyser les transactions par groupes de 5 consécutifs.
3. Si la somme des montants dans une fenêtre de 5 dépasse 500 000 DH, affiche une alerte de suspicion de fraude : `🚨 Alerte Fenêtre : [Liste des IDs]`.

**Livrable** : Code source démontrant l'usage de `partition`, `grouped` et `sliding` sur des flux de transactions réels.
