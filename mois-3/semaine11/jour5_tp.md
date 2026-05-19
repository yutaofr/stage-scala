# TP Jour 5 : Clearing Engine v2.2 — La Forteresse Typée

**Durée :** ~4h | **Fil Rouge :** Assemblage final avec types opaques et extensions

---

## Exercice 1 : Finalisation de la Migration Incrémentale (1h30)

> Tu as commencé la migration au TP J4. Maintenant, on termine proprement.

1. Vérifie que **tous** les fichiers du pipeline compilent avec les Opaque Types :
   - `DomainTypes.scala` ✅ (créé au J4)
   - `Transaction.scala` / `Bank.scala` ✅ (migré au J4)
   - `Serializers.scala` ✅ (migré au J4)
   - `Validator.scala` — si pas encore fait, migre-le maintenant.
   - `ClearingPipeline.scala` — idem.

2. Pour chaque fichier restant :
   - Ajoute `import clearing.model.DomainTypes.*`
   - Remplace les `String` par `BankCode.unsafe(...)` aux points de construction
   - Remplace les `BigDecimal` par `Money(...)` aux points de construction
   - Utilise `.value` pour accéder à la valeur brute dans les interpolations

3. Compile après chaque fichier migré. Note le nombre d'erreurs restantes.

---

## Exercice 2 : Export Multi-Format Contextuel (1h30)

### 2a. Ajouter le support XML (30 min)
1. Crée un trait `XmlSerializer[T]` dans `Serializers.scala` (à côté de `JsonSerializer` et `CsvSerializer`).
2. Implémente les instances `given` pour `Transaction` et `Bank`.
3. Ajoute des méthodes `exportItemXml` et `exportBatchXml` à `ExportEngine`.

### 2b. Démonstration Multi-Format (30 min)
1. Crée un script `MultiExportDemo.scala` qui :
   - Crée un batch de 3 transactions.
   - L'exporte en JSON.
   - L'exporte en CSV.
   - L'exporte en XML.
2. Observe : la logique de création du batch est identique à chaque fois. Seule la méthode d'export change.

### 2c. Test de Validation (30 min)
1. Crée un `MultiFormatSpec.scala` qui vérifie :
   - L'export XML d'une transaction contient `<sender>...</sender>`.
   - L'export CSV d'un batch produit une ligne par transaction.
   - Le changement de format est transparent (même données, format différent).

---

## Exercice 3 : Revue de Code S11 (1h)

### 3a. Démonstration au Tuteur (30 min)
1. Présente ton DSL de validation : `if tx.amount.isPositive then ...`
2. Montre un cas où le compilateur refuse une erreur de typage métier :
   ```scala
   val code: BankCode = BankCode.unsafe("ATH")
   val iban: Iban = code  // ❌ Échec de compilation !
   ```
3. Montre l'utilisation de `@targetName` et explique pourquoi c'est nécessaire.

### 3b. Auto-Évaluation (30 min)
Réponds à ces questions dans un fichier `retro_s11.md` :
1. Quel concept as-tu trouvé le plus difficile cette semaine ? Pourquoi ?
2. Combien d'erreurs de compilation as-tu rencontrées lors de la migration ? Quel fichier a posé le plus de problèmes ?
3. Qu'est-ce que tu changerais dans l'ordre de migration si tu devais le refaire ?
4. En quoi les Opaque Types auraient pu éviter un bug réel dans un système bancaire ?

---

**Bilan Mois 3 - Semaine 11** : Les abstractions de Scala 3 sont maîtrisées. Le moteur est une forteresse typée, prête pour sa validation mathématique finale.

**Livrables** :
- Code source v2.2 utilisant types opaques, extensions et context abstraction.
- `MultiFormatSpec.scala` avec les tests multi-format.
- `retro_s11.md` avec l'auto-évaluation.
