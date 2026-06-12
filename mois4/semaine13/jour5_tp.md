# TP Jour 5 : "Let it Crash" en Action

**Durée :** ~4h | **Fil Rouge :** Un système de clearing indestructible

> [!WARNING]
> **Piège Scala 3** : Si ton trait `SimpleActor` a un paramètre `strategy`, il faut toujours écrire `extends SimpleActor[T]()` ou `extends SimpleActor[T](SupervisorStrategy.Restart)` lors de l'héritage. Sinon : `Parameterized trait lacks argument list`.

---

## Exercice 1 : L'Acteur Fragile (1h30)

1. Modifie ton acteur `BankVault`.
2. Fais en sorte qu'il lève une `RuntimeException` si le montant du débit est exactement `999`.
3. Envoie une série de messages dont un avec le montant `999`.
4. Observe que l'acteur s'arrête et ne traite plus les messages suivants.

---

## Exercice 2 : Le Superviseur Vigilant (1h30)

1. Crée un acteur `Supervisor`.
2. Définis une `SupervisorStrategy` qui dit : "Sur n'importe quelle Exception, fais un `Restart`".
3. Fais en sorte que le `Supervisor` crée l'acteur `BankVault`.
4. Relance le test du montant `999`.
5. Observe que l'acteur redémarre et **continue de traiter** les messages après l'erreur.

> [!CAUTION]
> **Perte d'état** : Après le restart, le solde de l'acteur est réinitialisé. C'est normal ! En production, on résout ce problème avec l'**Event Sourcing** (persister chaque message dans un journal, puis le rejouer au redémarrage). Ce sera un sujet futur.

---

## Exercice 3 : Bilan Semaine 13 (1h)

1. Rédige un court rapport comparant :
   - Un crash dans une boucle `for` classique (Le programme s'arrête).
   - Un crash dans un système d'acteurs supervisés (Le système survit).

---

## 📊 Auto-Évaluation de la Semaine 13

| Concept | Je sais expliquer | J'ai codé | J'ai testé |
|---------|:-:|:-:|:-:|
| Race Condition & `synchronized` | ☐ | ☐ | ☐ |
| Future, map, flatMap, sequence | ☐ | ☐ | ☐ |
| Thread Starvation (Bulkhead) | ☐ | ☐ | ☐ |
| Acteur (Mailbox, `!`, receive) | ☐ | ☐ | ☐ |
| Supervision (Restart/Stop) | ☐ | ☐ | ☐ |
| Event Sourcing (concept seul) | ☐ | — | — |

**Livrable** : Code source complet incluant la hiérarchie d'acteurs et la stratégie de supervision.
