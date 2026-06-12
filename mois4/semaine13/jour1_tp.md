# TP Jour 1 : L'Enfer des Threads

**Durée :** ~4h | **Fil Rouge :** Simuler la corruption d'un solde bancaire

---

## Exercice 1 : Le Compte Bancaire (Starter Kit)

> [!TIP]
> **Nouveau Format Starter Kit :** Pour t'éviter de perdre du temps sur la configuration SBT, tu trouveras dans `src/main/scala/exercises/s13/` un squelette de code pré-configuré. Utilise ces fichiers pour tes TPs !

1. Ouvre `exercises/s13/UnsafeBankAccountApp.scala`.
2. Crée une classe interne `BankAccount` contenant une variable `var balance: Double = 1000.0`.
3. Crée une méthode `withdraw(amount: Double)` qui fait :
   ```scala
   val old = balance
   Thread.sleep(10) // Simule un traitement long
   balance = old - amount
   ```
3. Lance 10 threads qui retirent chacun 10.0 en même temps.
4. Observe que le solde final n'est pas 900.0. Pourquoi ?

---

## Exercice 2 : La Solution "Bête" (Synchronized) (1h)

1. Ajoute le mot-clé `synchronized` sur ta méthode `withdraw`.
2. Relance le test.
3. Vérifie que le solde est maintenant correct (900.0).
4. **Réflexion** : Quel est l'impact sur la vitesse de traitement ? (Le parallélisme est devenu séquentiel).

---

## Exercice 3 : Vers l'Immuabilité (1h30)

1. Réécris ton compte en mode immutable : `case class Account(balance: Double)`.
2. Comment gérerais-tu les modifications simultanées sans `var` ? 
   - *Indice : Imagine que chaque virement renvoie un nouvel objet Account.*
3. Réfléchis à comment un "Gestionnaire" pourrait centraliser ces nouveaux objets.

**Livrable** : Code source démontrant la race condition et son correctif par verrouillage, avec un paragraphe d'analyse sur la perte de performance liée au verrouillage.
