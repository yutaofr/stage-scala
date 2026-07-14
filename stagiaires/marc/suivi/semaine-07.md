# Semaine 7 — Netting avancé et Clearing Engine v1.2

## Objectif

Passer du calcul global simple à un moteur de compensation explicable et
scalable : agréger les échanges par paire, calculer les positions N-à-N,
traiter le flux par lots, détecter des fenêtres suspectes et vérifier le
comportement sur 100 000 puis un million de transactions.

## Notions à maîtriser

- `groupBy` et paire `(String, String)` comme clé d'agrégation.
- `view.mapValues` et matérialisation contrôlée avec `toMap`.
- `foldLeft` et `updatedWith` pour un état agrégé immutable.
- Invariant du netting multilatéral : somme des positions égale à zéro.
- `sortBy`, `partition`, `grouped` et `sliding`.
- Fusion des résultats de lots et équivalence avec le calcul global.
- Différences d'accès et de construction entre `List` et `Vector`.
- Pipeline strict, `view`, calcul séquentiel et collections parallèles.
- Benchmark reproductible : égalité fonctionnelle avant comparaison des temps.

## Exercices

- [x] J1 — Générer 100 transactions, les grouper par paire, calculer le total
  par duo et fournir `getNetDuo`.
- [x] J2 — Calculer les positions multilatérales avec `foldLeft` et
  `updatedWith`, vérifier la somme nulle et rendre débiteurs puis créanciers.
- [x] J3 — Trier, séparer avec `partition`, traiter des lots de 10 puis 1 000
  avec `grouped` et détecter les fenêtres de cinq avec `sliding`.
- [x] J4 — Comparer `List`/`Vector`, strict/`view` et séquentiel/parallèle sans
  utiliser le temps comme assertion fonctionnelle.
- [x] J5 — Intégrer le moteur v1.2 et démontrer 100 000 transactions.

## Premier livrable

Une version `v1.2` qui réutilise la validation v1.1, marque les succès
`Validated`, calcule les règlements bilatéraux et les positions N-à-N, traite
des lots équilibrés, signale les fenêtres dépassant 500 000 DH et produit un
rapport métier déterministe sur 100 000 transactions.

## Critères de validation

- [x] L'index bilatéral utilise la paire demandée et ignore les statuts non
  validés.
- [x] `getNetDuo` retourne le total d'un duo indépendamment de son ordre.
- [x] Le règlement bilatéral produit au plus une instruction positive par
  paire.
- [x] Le calcul N-à-N existe pour `List`, `Vector`, `Map[String, BigDecimal]`
  et `Map[Bank, BigDecimal]`.
- [x] Cent lots déterministes vérifient l'invariant de somme nulle.
- [x] L'ordre de règlement place les débiteurs du plus négatif au moins
  négatif, puis les créanciers du plus positif au moins positif.
- [x] `partition` sépare les flux validés et non validés sans perte.
- [x] Le lot pédagogique utilise 10 éléments; le moteur utilise 1 000 par
  défaut et conserve le dernier lot partiel.
- [x] La fusion des positions par lot égale le calcul global.
- [x] Une fenêtre de cinq est signalée seulement au-dessus de 500 000 DH.
- [x] Les variantes `List`/`Vector`, strict/`view` et
  séquentiel/parallèle produisent exactement le même résultat métier.
- [x] La démonstration déterministe traite 100 000 transactions en 100 lots,
  avec un volume de 5 000 500 DH et un solde global nul.
- [x] Le fichier v1.1 conserve son contrat 57/45/3/12 dans v1.2.
- [x] Le benchmark d'un million de transactions est exécuté et documenté.
- [x] Tous les tests S1 à S7 passent avec Java 21 et Java 17 Docker.
- [x] Le code v1.2 passe l'audit fonctionnel et la revue mentor.

## Journal d'exécution

### Cycles TDD observés

- Le test d'intégration a d'abord montré que les succès v1.1 restaient
  `Pending`. La branche acceptée copie maintenant la transaction avec le statut
  `Validated`; les compteurs 57/45/3/12 restent inchangés.
- Le netting bilatéral a commencé par dix erreurs de compilation sur les APIs
  absentes. Les tests couvrent ensuite les paires, l'adaptateur tuple, les
  statuts et les sens de règlement.
- Le netting multilatéral a commencé par douze erreurs de compilation. Les
  exemples, les deux collections, l'adaptateur `Bank` et cent lots générés ont
  ensuite validé l'invariant.
- La segmentation a commencé par dix erreurs de compilation. Les tests couvrent
  lots complets et partiels, fusion, seuil exact, dépassement et ordre des IDs.
- Le laboratoire de performance a commencé par six erreurs de compilation. Les
  assertions comparent les résultats; elles n'imposent jamais qu'une durée soit
  inférieure à une autre.
- Le reporter et l'intégration v1.2 ont commencé par douze erreurs de
  compilation. Onze tests valident le rapport, le scénario réel, les erreurs
  d'entrée et la démonstration de 100 000 transactions.
- La première revue mentor a relevé quatre points importants : conversion
  incluse dans le temps `List`, générateur trop régulier, seuil mémoire absent
  et CLI permissives. Les nouveaux tests ont d'abord échoué sur les APIs de
  comparaison et de seed absentes, puis ont reproduit les deux lancements CLI
  indésirables. Les corrections préparent les collections avant mesure,
  couvrent les vingt paires avec un seed, refusent les arguments invalides et
  documentent le seuil borné. La seconde revue ne conserve aucun point.

### Décisions mentor

La production nomme une paire avec `BankPair`; l'API tuple reste un adaptateur
de cours. De même, le cœur retourne des codes bancaires, et une méthode dédiée
expose `Map[Bank, BigDecimal]`. Ces formes répondent aux deux formulations du
support sans dupliquer l'algorithme.

Le lot de 10 sert à apprendre `grouped`. Le moteur prend 1 000 par défaut pour
la démonstration de 100 000. La fusion des positions protège le passage du petit
exemple au traitement par lots.

Les transactions S7 sont en MAD et déjà validées. Le projet ne simule pas une
conversion implicite : additionner des monnaies différentes donnerait un
résultat faux. Les frais, le collatéral et la liquidité restent hors périmètre.

Le modèle n'a pas encore d'horodatage. Le générateur donne des IDs croissants;
le laboratoire les utilise comme ordre déterministe des fenêtres, sans les
présenter comme une date métier.

Les temps varient avec l'échauffement de la JVM, le matériel et la mémoire.
Le benchmark publie donc chaque durée et l'environnement, mais son acceptation
repose sur l'égalité des résultats et l'équilibre du netting.

### Benchmark d'un million

Mesure du 14/07/2026 sous Java 21, avec une mémoire JVM maximale déclarée de
6 144 MB. La commande complète prend 9,35 s de temps mur, démarrage et
compilation SBT inclus.

| Étape | Durée observée |
|---|---:|
| Génération | 1 300 ms |
| Netting `List` | 248 ms |
| Netting `Vector` | 157 ms |
| Pipeline strict | 274 ms |
| Pipeline `view` | 114 ms |
| CPU séquentiel | 20 ms |
| CPU parallèle | 23 ms |

Les deux nettings, les deux pipelines et les scores CPU retournent des
résultats identiques. Le solde global est nul. Cette exécution particulière ne
permet pas de conclure qu'une variante sera toujours plus rapide : le petit
échantillon CPU rend même le coût de parallélisation supérieur au gain.

Le [rapport de scalabilité](../fil-rouge/benchmarks/semaine-07.md) documente
aussi le test Docker borné : avec 1 280 MiB pour le conteneur et 768 MiB pour
la JVM du benchmark, 968 750 transactions réussissent et 1 000 000 échouent
avec le code 137. Cet intervalle est propre à cette configuration.

## Validation mentor

**Décision : acceptée.** Le benchmark d'un million conserve toutes les
équivalences et l'invariant de somme nulle. La suite complète compile 50
sources de production et 38 sources de test, puis exécute 266 tests dans 38
suites, sans échec, sous Java 21 local et Java 17 Docker. Les deux
environnements reproduisent la démonstration
`100000/100/5009401.25/0`; le fichier réel conserve `57/45/3/12`.

L'audit ne trouve aucun `var`, `null`, `???`, `while`, collection mutable ou
`Double` dans le paquet v1.2. La dépendance applicative descend uniquement vers
le modèle stable et la validation v1.1. Le tuple de paire et le tuple retourné
par `partition` correspondent aux exercices. Après correction des quatre
points importants, la seconde revue mentor ne conserve aucun point critique,
important ou mineur.
