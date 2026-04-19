---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 8"
footer: "Jour 4 — Spring Context & @Repository Mock"
---

# Scala & Spring Boot
## Intégrer la puissance fonctionnelle dans un framework Java

**Durée :** ~2h | **Fil Rouge :** Accéder aux données de référence via Spring

---

# 📋 Objectifs du Jour

- Comprendre comment Scala s'intègre dans l'écosystème Spring.
- Apprendre à utiliser les annotations Spring (`@Service`, `@Repository`) en Scala.
- Maîtriser l'injection de dépendances bidirectionnelle.
- Simuler un accès base de données via un Repository Mock.

---

# 1. Spring ❤️ Scala

Spring Boot ne fait pas de distinction entre Java et Scala. Les deux produisent du bytecode compatible.

### Les règles de base
- Les classes Scala doivent être annotées normalement.
- Utilisez l'injection par constructeur (recommandé en Scala).

```scala
@Service
class ClearingService @Autowired() (
  repo: TransactionRepository // Un repo écrit en Java ou Scala
) {
  // Logique métier...
}
```

---

# 2. Les Annotations en Scala 3

La syntaxe est très proche de Java.

```scala
import org.springframework.stereotype.Repository

@Repository
class MemberRepository:
  def findByCode(code: String): Option[Member] = 
    // Simulation...
```

> 💡 Notez l'usage de `Option` dans l'interface Scala, même si Spring (en Java) utiliserait souvent `Optional`.

---

# 3. Le Pattern Mock d'Architecture

Dans ce stage, nous n'installons pas de base de données réelle. Nous utilisons des **Mocks** (simulations) persistants en mémoire.

```scala
@Repository
class BankRepositoryMock:
  private val banks = Map("001" -> Bank("ATH"), "002" -> Bank("CIH"))
  
  def findById(id: String): Option[Bank] = banks.get(id)
```

---

# 🏗️ Application : Le Registre Central

Nous allons remplacer notre objet `ReferenceData` codé en dur par un `@Repository` Spring fictif que notre moteur appellera.

---

# 🧠 Quiz Rapide

1. Spring peut-il injecter un composant Scala dans un composant Java ? (Oui).
2. Quelle est la manière recommandée pour faire de l'injection en Scala ? (Injection par constructeur).
3. Est-il possible d'utiliser Spring Data JPA avec Scala ? (Oui, bien que cela nécessite quelques ajustements de syntaxe).

---

# 📝 Résumé du Jour

- Scala s'insère sans couture dans un projet Spring Boot existant.
- Les annotations Spring fonctionnent exactement de la même manière.
- L'injection de dépendances permet de découpler le moteur de clearing de ses sources de données.
- Tu es prêt à travailler dans les environnements "Enterprise Java" modernes.

**Prochaine étape** : Simuler un repository Spring dans le TP 39 !
