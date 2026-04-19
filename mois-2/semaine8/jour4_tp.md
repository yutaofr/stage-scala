# TP Jour 4 : Simulation d'Architecture Spring

**Durée :** ~4h | **Fil Rouge :** Découpler le moteur via l'injection de dépendances

---

## Exercice 1 : Annotations & Composants (1h30)

1. Crée une classe `BankRepository` avec l'annotation `@Repository`.
2. Implémente une méthode `getAllBanks(): List[Bank]`.
3. Crée une classe `ClearingService` avec l'annotation `@Service`.
4. Injecte le repository dans le service via le constructeur (utilise l'annotation `@Autowired`).

---

## Exercice 2 : Le Validateur Service (1h30)

1. Transforme ton `TransactionValidator` en un `@Component` Spring.
2. Il doit maintenant utiliser le `BankRepository` pour vérifier si une banque existe avant de valider une transaction.
3. Vérifie que tu peux appeler ce service depuis ton application principale.

---

## Exercice 3 : Simulation du démarrage Spring (1h)

1. Crée un `DemoApp` qui instancie manuellement tes composants (en simulant ce que ferait Spring) :
   ```scala
   val repo = new BankRepository()
   val validator = new TransactionValidator(repo)
   val service = new ClearingService(validator)
   ```
2. Lance un cycle complet de validation.

**Livrable** : Code source structuré selon les padrões Spring (Annotations, Injection) prêt pour une intégration réelle.
