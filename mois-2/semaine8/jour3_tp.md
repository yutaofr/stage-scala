# TP Jour 3 : Connexion aux Services Externes

**Durée :** ~4h | **Fil Rouge :** Un moteur de clearing alimenté par des données temps réel

---

## Exercice 1 : Ma première requête (1h)

1. Utilise `HttpClient` pour appeler une API publique gratuite (ex: `https://jsonplaceholder.typicode.com/posts/1`).
2. Affiche le code de statut et le corps de la réponse dans la console.
3. Gère le cas où l'URL est malformée.

---

## Exercice 2 : Le Service de Change (2h)

1. Crée un objet `ExchangeRateService`.
2. Implémente la méthode `fetchRate(currency: String): Option[BigDecimal]`.
3. Simule l'appel à une API de change.
4. Si l'appel réussit, retourne `Some(taux)`, sinon `None`.
   - *Note : Pour cet exercice, tu peux simplement vérifier si la réponse contient une sous-chaîne spécifique au lieu de parser le JSON complet.*

> [!NOTE]
> **Pourquoi on parse "à la main" ?** On n'a pas encore ajouté de librairie JSON au projet. En production, on utiliserait **Circe** (Scala) ou **Jackson** (Java) pour désérialiser proprement le JSON en `case class`. On découvrira ça au Mois 3 !

---

## Exercice 3 : Intégration & Cache (1h)

1. Modifie ton `NettingProcessor`.
2. Avant de commencer le calcul, appelle le service de taux pour chaque devise présente dans le batch.
3. **Optimisation** : Stocke les taux dans une `Map` locale (Cache) pour ne pas appeler l'API plusieurs fois pour la même devise.

**Livrable** : Code source du service HTTP et intégration dans le pipeline de clearing avec gestion élémentaire des erreurs réseau.
