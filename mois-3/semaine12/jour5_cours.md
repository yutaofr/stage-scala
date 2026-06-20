---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 12"
footer: "Jour 5 — Demo Mois 3 (Clearing Engine v2.3)"
---

# Bilan du Mois 3
## La Maîtrise de l'Abstraction Fonctionnelle

**Durée :** ~2h | **Fil Rouge :** Clearing Engine v2.3 — La Certification Mathématique

---

# 📋 Objectifs du Jour

- Récapituler les piliers de la FP (RT, Pureté, Composition, Monades, PBT).
- Comparer l'architecture v1.1 (Impérative) et v2.3 (Fonctionnelle).
- Démontrer la résilience et la précision du moteur face à des tests massifs.
- Découvrir le programme du Mois 4 : Programmation Distribuée & Big Data.

---

# 1. Évolution Architecturale

| Aspect          | v1.0 (Mois 1)       | v2.0 (Mois 2)          | v2.3 (Mois 3)             |
|-----------------|----------------------|------------------------|----------------------------|
| **Erreurs**     | `try/catch`          | `Either[Error, A]`     | Railway + MonadicLogger    |
| **Types**       | `String`, `Double`   | Case classes           | Opaque Types (BankCode, Money) |
| **Extension**   | Héritage             | Pattern Matching       | Type Classes (given/using) |
| **Sérialisation** | `toString`        | JSON (1 format)        | JSON / CSV / XML (multi)   |
| **Tests**       | `println` manuel     | ScalaTest unitaire     | ScalaCheck Property-Based  |
| **Logging**     | `println`            | Pas de logging         | MonadicLogger (pur)        |

---

# 2. Rétrospective du Mois 3

### Semaine 9 & 10 : Fondations & Résilience
- Pureté totale, fin des side-effects.
- Railway Oriented Programming (Either/Try).
- Zéro exception, reporting typé.

### Semaine 11 & 12 : Abstraction & Preuve
- Type Classes & Implicits pour l'extensibilité.
- Opaque Types pour la sécurité sans coût JVM.
- Functors & Monades pour le chaînage fluide.
- **ScalaCheck** pour la certification mathématique du netting.

---

# 🛡️ Les Pièges Surmontés

| Piège                        | Leçon apprise                                               |
|------------------------------|--------------------------------------------------------------|
| Big Bang Refactoring         | Migration incrémentale fichier par fichier                   |
| `@targetName` oublié         | Nécessaire pour les Opaque Types basés sur String            |
| `Some` vs `Option`           | Utiliser `Option(x)` dans les contextes génériques           |
| `Logger` en conflit          | Renommer en `MonadicLogger` pour éviter les collisions       |
| `Gen.filter` anti-pattern    | Générer exactement ce qu'on veut (`Gen.listOfN`)             |
| `import X.given` vs `X.*`    | `.*` pour les extensions, `.given` pour les instances         |

---

# 🚀 Vers le Mois 4 : Systèmes Distribués

On quitte le monde d'une seule machine pour le Cloud :
1. **Concurrence & Futures** : Gérer des milliers de tâches en parallèle.
2. **Akka / Pekko Actors** : Modéliser les banques comme des entités communicantes.
3. **Kafka Streams** : Traiter les transactions au fil de l'eau (Temps Réel).
4. **Sharding** : Distribuer le clearing sur 10 serveurs.

---

# 🧠 Grand Quiz du Mois 3

1. Pourquoi une monade est-elle plus puissante qu'un simple Functor ? (Elle a `flatMap` qui permet le chaînage dépendant).
2. Quelle bibliothèque permet de faire du Property-Based Testing ? (ScalaCheck).
3. Le `for-yield` permet-il d'écrire autre chose que des boucles ? (Oui, c'est du sucre pour `flatMap`/`map`).
4. Quel est l'effet de l'immuabilité sur la sécurité des threads ? (Les données partagées ne risquent jamais d'être corrompues).
5. **Nouveau** : Pourquoi `map` est "gratuit" une fois qu'on a `flatMap` + `pure` ? (Car `map(f) = flatMap(a => pure(f(a)))`).

---

# 📝 Conclusion

Félicitations ! Tu as terminé la phase la plus difficile et la plus noble du stage. Tu as acquis une rigueur de développement qui te suivra durant toute ta carrière de Lead Developer. Tu es maintenant prêt pour le "Big Data" et les "Systèmes Distribués".

**Dernière étape** : La grande démo finale du Mois 3 !
