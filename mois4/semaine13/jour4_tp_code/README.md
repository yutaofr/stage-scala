# Jour 4 — Corrigé exécutable : mini-système d'acteurs (Pekko Typed)

Code de référence **vérifié** (compilé + exécuté) pour le TP du Jour 4, Semaine 13.
À utiliser pour s'auto-évaluer après avoir tenté les ZONE STAGIAIRE du `starter_kit_s13.md`.

| Fichier | Rôle | Exercice |
|---|---|---|
| `BankVaultActor.scala` | Acteur coffre : `Credit`/`Debit` + `sleep` aléatoire | Ex 1 + Ex 3 |
| `ClearingManager.scala` | Orchestrateur : annuaire `Map[bankId → ActorRef]` + dispatch + `@main clearingDemo` | Ex 2 |

## Lancer la démo

```bash
scala-cli run . --main-class distributed.actors.clearingDemo
```

## Ce qu'on observe

- Le `ClearingManager` retrouve chaque coffre dans son **annuaire** (`vaults.get(bankId)`) puis envoie
  `Credit` (position nette > 0) ou `Debit` (position nette < 0) via `!`.
- Soldes finaux corrects, sans `synchronized` : `BMCE → 1250`, `CIH → 700`, `BP → 1050`.
- L'ordre de traitement varie d'une exécution à l'autre (sleep aléatoire) → preuve du modèle **non-bloquant**.

## Points d'attention pédagogiques

- La `Response` porte son `bankId` car **Pekko n'enregistre qu'UN `messageAdapter` par classe de message** :
  un adaptateur par banque ne marche pas (le dernier écrase les autres).
- `Thread.sleep` dans un acteur bloque son thread de dispatcher : OK pour *illustrer*, anti-pattern en prod
  (préférer `Behaviors.withTimers` / messages planifiés).
