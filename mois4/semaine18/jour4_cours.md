---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 18"
footer: "Jour 4 — Tuning GC & JVM"
---

# Tuning JVM
## Configurer la machine pour le Streaming

**Durée :** ~2h | **Fil Rouge :** Supprimer les pauses du Garbage Collector

---

# 📋 Objectifs du Jour

- Comprendre le fonctionnement du **Garbage Collector** (GC).
- Découvrir les différents types de GC (G1GC, ZGC).
- Apprendre à configurer les flags JVM (`-Xmx`, `-Xms`, `-XX:+UseG1GC`).
- Optimiser le moteur de clearing pour réduire la latence "Stop-the-World".

---

# 1. Le Garbage Collector (GC)

C'est l'agent d'entretien de la JVM. Il passe régulièrement pour ramasser les objets dont personne ne se sert.
- **Problème** : Parfois, il doit arrêter TOUS les threads de l'application pour travailler (**Stop-the-World**).

> [!CAUTION]
> Une pause GC de 2 secondes au milieu d'un virement bancaire est inacceptable.

---

# 2. Quel GC choisir ?

- **G1GC** : Le standard moderne. Équilibré entre débit et latence.
- **ZGC** : Le nouveau champion. Garantit des pauses < 1ms, même sur des To de RAM ! (Idéal pour ATH).

---

# 3. Les Flags Indispensables

```bash
java -Xms2g -Xmx2g \
     -XX:+UseZGC \
     -jar clearing-engine.jar
```

- `-Xms` / `-Xmx` : Doivent souvent être égaux pour éviter que la JVM ne redimensionne le tas sans arrêt.
- `-XX:+UseZGC` : Active le Garbage Collector ultra-rapide.

---

# 🏗️ Application : Optimisation du Moteur

Nous allons comparer les performances de notre moteur avec les réglages par défaut vs nos réglages optimisés. Nous chercherons à obtenir une courbe de latence la plus "plate" possible, sans pics liés au GC.

---

# 🧠 Quiz Rapide

1. Que se passe-t-il pendant une pause "Stop-the-World" ? (L'application est totalement figée).
2. Pourquoi mettre `Xms` et `Xmx` à la même valeur ? (Pour éviter l'instabilité et le coût CPU lié au redimensionnement du tas).
3. Quel GC est recommandé pour des applications exigeant une latence ultra-faible ? (ZGC).

---

# 📝 Résumé du Jour

- La JVM est une bête puissante qu'il faut savoir dompter.
- Le réglage du GC peut doubler les performances de ton application.
- Un bon Lead Dev connaît les entrailles de sa machine virtuelle.

**Prochaine étape** : Configurer ta JVM dans le TP 89 !
