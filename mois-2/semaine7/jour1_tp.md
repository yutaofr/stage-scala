# TP Jour 1 : Netting Bilatéral

**Durée :** ~4h | **Fil Rouge :** Calculer les dettes croisées entre banques

---

## Exercice 1 : Indexation par Paire (1h30)

1. Génère une liste de 100 transactions entre 5 banques différentes.
2. Utilise `groupBy` pour créer une Map dont la clé est le couple `(BanqueSource, BanqueDest)`.
3. Affiche le nombre de transactions pour chaque couple.

---

## Exercice 2 : Agrégation des Montants (1h30)

1. À partir de la Map précédente, utilise `mapValues` pour calculer la somme des montants pour chaque paire.
2. Le résultat doit être une `Map[(String, String), BigDecimal]`.
3. Affiche le résultat sous forme de tableau :
   `ATH -> CIH : 150 000 DH`
   `CIH -> ATH : 80 000 DH`

---

## Exercice 3 : Calcul du Solde Bilatéral (1h)

1. Pour deux banques A et B, le solde bilatéral est : `(A->B) - (B->A)`.
2. Crée une fonction `getNetDuo(b1: String, b2: String, data: Map[(String, String), BigDecimal]): BigDecimal`.
3. Elle doit retourner ce que la banque 1 doit réellement à la banque 2 après compensation de leurs dettes mutuelles.

**Livrable** : Code source capable de calculer la dette nette entre n'importe quel duo de banques.
