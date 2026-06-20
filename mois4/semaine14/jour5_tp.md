# TP Jour 5 : Le client HTTP résilient

**Durée :** ~4h | **Fil Rouge :** Finalisation de la couche asynchrone ZIO

---

## Point de départ

- Copie le **Kit 14.5** de `starter_kit_s14.md`.
- Le mock doit être déterministe : il échoue sur les trois premiers appels puis réussit.
- Les erreurs de validation et d'authentification ne doivent pas être retentées.

## Exercice 1 : Retry exponentiel borné (2h)

1. Implémente le mock déterministe avec un `Ref[Int]`.
2. Modélise `TemporaryRateError` et `InvalidCurrency`.
3. Applique un retry borné uniquement à `TemporaryRateError`.
4. Journalise le numéro de tentative et le délai.
5. Teste un succès après trois erreurs, puis une erreur non récupérable.

**Validation :** le premier scénario réussit au quatrième appel ; le second échoue sans retry.

---

## Exercice 2 : Le Circuit Breaker (Simulation) (1h30)

1. Utilise un `Ref` pour stocker l'état `Closed`, `Open(until)` ou `HalfOpen`.
2. Ouvre le circuit après trois échecs consécutifs.
3. Refuse les appels pendant la fenêtre d'ouverture sans appeler le service.
4. Autorise un seul appel de test en état `HalfOpen`.
5. Remets le circuit à zéro après un succès.

**Validation :** un compteur séparé prouve qu'aucun appel distant n'a lieu lorsque le circuit est ouvert.

---

## Exercice 3 : Revue de Code ZIO (30 min)

1. Analyse le moteur v2.4 avec une grille : dépendances, erreurs, ressources, concurrence, retry.
2. Identifie trois risques avant Kafka : saturation, doublons et reprise après panne.
3. Pour chaque risque, écris une preuve à produire pendant la S15.

**Validation :** la revue contient trois risques, trois décisions et trois preuves observables.

**Bilan Semaine 14** : le Clearing Engine v2.4 possède une orchestration ZIO explicite. La maîtrise sera confirmée par les tests de la semaine suivante.

**Livrable** : Clearing Engine v2.4, tests des politiques de retry/circuit breaker et grille de revue avant Kafka.
