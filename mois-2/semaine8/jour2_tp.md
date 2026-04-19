# TP Jour 2 : Le Pont entre les Mondes

**Durée :** ~4h | **Fil Rouge :** Intégrer une librairie Java de calcul de frais

---

## Exercice 1 : Simulation d'une API Java (1h)

1. Crée un fichier `JavaMock.java` (ou simule son comportement dans un objet Scala via `java.util.*`).
2. Implémente une méthode qui retourne une `java.util.ArrayList[String]` contenant des codes de transactions fictifs.

---

## Exercice 2 : Consommation en Scala (1h30)

1. Appelle la méthode Java de l'exercice 1.
2. Utilise `asScala.toList` pour transformer le résultat en `List[String]`.
3. Applique un filtre `startsWith("TX-")` sur cette liste Scala.
4. Vérifie que tu peux utiliser toutes les méthodes de HOF (`map`, `filter`) sur ce nouveau flux.

---

## Exercice 3 : Conversion de Map (1h30)

1. Crée une `java.util.HashMap[String, Double]` représentant les soldes de banques.
2. Convertis-la en `Map[String, BigDecimal]` Scala.
   - Indice : Tu devras mapper les valeurs pour passer de `Double` à `BigDecimal`.
3. Affiche la Map finale triée par nom de banque.

**Livrable** : Code source montrant la conversion bidirectionnelle fluide entre les collections des deux langages.
