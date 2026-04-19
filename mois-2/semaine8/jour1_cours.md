---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 8"
footer: "Jour 1 — Import Java & Cryptographie"
---

# L'Interopérabilité Java
## Profiter de la puissance de l'écosystème Java

**Durée :** ~2h | **Fil Rouge :** Anonymisation des IBANs par Hashing

---

# 📋 Objectifs du Jour

- Comprendre comment Scala s'exécute sur la JVM.
- Apprendre à importer et utiliser n'importe quelle classe Java.
- Manipuler les bibliothèques standards Java (ex: Sécurité).
- Gérer les types Java (Tableaux, Primitifs) depuis Scala.

---

# 1. Scala & Java : Une seule Famille

Puisque Scala compile en **Bytecode JVM**, il peut utiliser 100% des bibliothèques Java existantes sans effort.

```scala
// Import Java standard
import java.time.LocalDateTime
import java.util.UUID

val id = UUID.randomUUID().toString
val now = LocalDateTime.now()
```

> 💡 Pour Scala, une classe Java est juste une classe comme une autre.

---

# 2. Utiliser la Cryptographie Java

Exemple : Sécuriser les données sensibles d'ATH avec `MessageDigest`.

```scala
import java.security.MessageDigest

def sha256(input: String): String =
  val digest = MessageDigest.getInstance("SHA-256")
  val hash = digest.digest(input.getBytes("UTF-8"))
  hash.map("%02x".format(_)).mkString
```

- `input.getBytes` : Appel d'une méthode de `String` (commune Java/Scala).
- `MessageDigest.getInstance` : Appel de méthode statique.

---

# 3. Les Tableaux Java (`Array`)

En Scala, `Array[Byte]` est exactement un `byte[]` Java.

```scala
val bytes: Array[Byte] = Array(1, 2, 3)
val head = bytes(0) // Accès comme une fonction
```

- **Interopérabilité parfaite** : On peut passer cet `Array` directement à une méthode Java qui attend un tableau.

---

# 🏗️ Application : Anonymisation

Pour respecter le RGPD, nous ne stockerons plus les IBANs en clair dans les logs techniques, mais leur version hashée.

```scala
case class SecureTransaction(
  idHash: String, 
  amount: BigDecimal
)
```

---

# 🧠 Quiz Rapide

1. Faut-il une bibliothèque spéciale pour appeler du Java depuis Scala ? (Non).
2. Comment appelle-t-on une méthode statique Java en Scala ? (Comme une méthode d'objet : `NomClasse.methode()`).
3. Peut-on utiliser `java.util.ArrayList` en Scala ? (Oui, bien sûr).

---

# 📝 Résumé du Jour

- L'interopérabilité est totale et bidirectionnelle.
- Tu as accès à 25 ans de bibliothèques Java (Spring, Hibernate, Apache Commons).
- Utiliser la cryptographie Java est simple et direct.
- Ton moteur devient capable de manipuler des concepts de bas niveau (octets, hashs).

**Prochaine étape** : Hasher les IBANs de ton flux dans le TP 36 !
