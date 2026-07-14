# Rapport de scalabilité — Semaine 7

## But et méthode

Ce rapport qualifie le laboratoire v1.2 sur des transactions pseudo-aléatoires
reproductibles (`seed = 42`). Le générateur choisit cinq banques, interdit un
virement vers soi-même, varie les montants et couvre les vingt paires dirigées.

Les durées servent à observer une exécution. Les portes fonctionnelles sont :

- mêmes positions pour `List` et `Vector`;
- même digest pour le pipeline strict et la `view`;
- mêmes scores CPU en séquentiel et en parallèle;
- somme des positions égale à zéro.

Les collections `List` et `Vector` sont construites avant leur chronométrage.
La conversion ne fausse donc pas la comparaison du netting.

## Environnement

- Date : 14/07/2026.
- Machine : Apple M4 Pro, 12 cœurs, macOS 26.5.2, arm64.
- JVM locale : Eclipse Temurin 21.0.6.
- SBT : 1.10.11.
- Scala : 3.3.8.
- Docker : 29.6.1, Linux arm64.
- Image Java 17 :
  `sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0`.

## Benchmark nominal d'un million

Commande :

```bash
/usr/bin/time -l sbt \
  'runMain clearing.v12.runScalabilityLab 1000000'
```

Résultat sous Java 21 :

| Étape | Durée observée |
|---|---:|
| Génération | 1 300 ms |
| Netting `List` | 248 ms |
| Netting `Vector` | 157 ms |
| Pipeline strict | 274 ms |
| Pipeline `view` | 114 ms |
| CPU séquentiel, échantillon de 1 000 | 20 ms |
| CPU parallèle, échantillon de 1 000 | 23 ms |

La commande complète prend 9,35 s de temps mur, lancement et compilation SBT
inclus. La JVM du benchmark déclare un maximum de 6 144 MB. La commande
`time -l` rapporte un `peak memory footprint` brut de 918 947 496 octets pour
cette exécution; cette valeur dépend de la mesure macOS et ne constitue pas une
limite de déploiement.

Les quatre portes fonctionnelles sont vraies. Les temps ne prouvent pas qu'une
structure gagne toujours : ils montrent seulement le comportement de cette
exécution après correction du biais de conversion.

## Point de rupture sous mémoire bornée

Le seuil local utilise toujours les mêmes limites : conteneur à 1 280 MiB,
swap désactivé, JVM SBT à 256 MiB et JVM forkée du benchmark à 768 MiB.

Commande type :

```bash
docker run --rm \
  --memory=1280m --memory-swap=1280m \
  -v "$PWD:/app" -w /app \
  sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 \
  sbt -J-Xmx256m \
  'set Compile / run / javaOptions ++= Seq("-Xms128m", "-Xmx768m")' \
  'runMain clearing.v12.runScalabilityLab <volume>'
```

| Volume | Résultat | Preuve fonctionnelle |
|---:|---|---|
| 500 000 | succès | équivalences vraies, solde nul |
| 750 000 | succès | équivalences vraies, solde nul |
| 875 000 | succès | équivalences vraies, solde nul |
| 937 500 | succès | équivalences vraies, solde nul |
| 968 750 | succès | équivalences vraies, solde nul |
| 1 000 000 | échec, code 137 | conteneur tué après lancement du benchmark |

Dans cette configuration précise, le dernier volume réussi est 968 750 et le
premier volume échoué est 1 000 000. Le point de rupture observé se trouve donc
dans l'intervalle `(968 750, 1 000 000]`. Le processus SBT, ses caches et la JVM
forkée partagent la limite du conteneur; ce résultat ne mesure pas uniquement
la collection de transactions et ne doit pas être extrapolé à la production.

## Goulots et recommandations

- Conserver `Vector` comme stockage final lorsque l'accès indexé ou la
  construction en fin de séquence est nécessaire.
- Ne pas convertir un million de transactions en `List` dans le chemin réel;
  cette copie existe seulement pour le laboratoire comparatif.
- Préférer `Iterator`, `view`, `foldLeft` et les batches afin d'éviter des
  collections intermédiaires longues.
- Passer à un flux borné par batch lorsque le volume approche la mémoire
  disponible; le futur transport Kafka ne devra pas matérialiser tout le jour.
- Réserver `.par` aux calculs CPU suffisamment lourds, purs et mesurés. Sur
  l'échantillon actuel, son surcoût annule le gain.
- Dimensionner la mémoire et refaire le test dans l'environnement cible avant
  toute promesse de capacité.

## Conclusion

V1.2 traite un million de transactions dans l'environnement local non borné et
préserve tous les invariants. Sous la limite pédagogique choisie, elle atteint
un intervalle de rupture avant un million. Le prochain gain structurel ne
consiste pas à augmenter arbitrairement le tas : il consiste à traiter un flux
par batches bornés.
