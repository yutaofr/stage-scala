# TP Jour 1 : L'Enfer des Threads

**Durée :** ~4h | **Fil Rouge :** Simuler la corruption d'un solde bancaire

---

## Exercice 1 : Le Compte Bancaire (Starter Kit)

> [!TIP]
> **Starter Kit Fourni :** Pour t'éviter de perdre du temps sur la configuration, tu trouveras dans `src/main/scala/distributed/concurrency/` un squelette de code pré-configuré (voir **Kit 13.1** dans le starter kit). Utilise ces fichiers pour tes TPs !

1. Ouvre `distributed/concurrency/RaceDemo.scala` (fourni dans le Kit 13.1).
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
2. Utilise un `java.util.concurrent.atomic.AtomicReference[Account]` pour gérer les modifications concurrentes sans verrou :
   ```scala
   import java.util.concurrent.atomic.AtomicReference
   val account = AtomicReference(Account(1000.0))
   // Utilise account.updateAndGet pour modifier de manière atomique
   ```
3. Réfléchis : pourquoi cette combinaison (donnée immuable + référence atomique) est-elle plus sûre que `synchronized` ?
4. *Bonus :* Imagine qu'un "Acteur" centralisé pourrait jouer le même rôle que l'AtomicReference. On découvrira ce modèle au Jour 4.

**Livrable** : Code source démontrant la race condition, son correctif par `synchronized`, et la version immuable + `AtomicReference` avec un paragraphe d'analyse comparant les trois approches.
