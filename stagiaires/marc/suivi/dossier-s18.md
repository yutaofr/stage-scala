# Dossier d'apprentissage S18 — Marc

> Ce document est à compléter par Marc. Les questions guident le raisonnement ;
> elles ne constituent pas une réponse modèle. Toute valeur numérique doit être
> marquée **hypothétique** ou accompagnée de sa méthode de mesure.

## Fiche 1 — Modèle de charge

### Question étudiée

<!-- Quelle propriété du système veux-tu évaluer ? -->

### Profil proposé

| Phase | Durée | Charge | Pourquoi cette phase ? |
|---|---:|---:|---|
| Ramp-up | À compléter | À compléter | À compléter |
| Plateau | À compléter | À compléter | À compléter |
| Pic | À compléter | À compléter | À compléter |

### Indicateurs et seuils

| Indicateur | Seuil ou observation attendue | Justification |
|---|---|---|
| Débit | À compléter | À compléter |
| Taux d'erreur | À compléter | À compléter |
| p95 | À compléter | À compléter |
| p99 | À compléter | À compléter |
| Lag Kafka | À compléter | À compléter |

**Condition d'arrêt :** à compléter.

**Comment distinguer une limite du système d'une limite du générateur ?**

À compléter.

**Limite de cette fiche :** à compléter.

## Fiche 2 — Expérience de chaos

### Hypothèse falsifiable

<!-- Exemple de forme : si ..., alors ..., observable par ... -->

À compléter.

### Protocole

| Élément | Décision de Marc |
|---|---|
| Baseline | À compléter |
| Panne injectée | À compléter |
| Métriques observées | À compléter |
| Condition d'arrêt | À compléter |
| Procédure de retour | À compléter |

### Prédiction avant l'expérience

- Offsets : à compléter.
- Lag : à compléter.
- Retries : à compléter.
- Écritures Cassandra : à compléter.

### Conclusion possible selon les observations

À compléter sans inventer de résultat d'exécution.

**Limite de cette fiche :** à compléter.

## Fiche 3 — Interprétation d'un profil JVM

### Courbe heap fournie pendant le cours

<!-- Insère ou référence la courbe remise par le mentor, puis annote-la. -->

- Phase d'allocation : à compléter.
- Cycle GC : à compléter.
- Niveau après GC : à compléter.
- Signal qui justifierait une investigation : à compléter.

### Profil CPU

**Observation :** à compléter.

**Interprétation :** à compléter.

**Preuve supplémentaire nécessaire :** à compléter.

**Limite de cette fiche :** à compléter.

## Fiche 4 — Comparaison GC

### Variables contrôlées

- Version JVM : à compléter.
- Image et code : à compléter.
- Données et seed : à compléter.
- Profil de charge : à compléter.
- Ressources CPU/mémoire : à compléter.

### Matrice d'analyse

| Configuration | Répétitions | Débit médian | p95 | p99 | Temps GC | Variabilité | Compromis |
|---|---:|---:|---:|---:|---:|---:|---|
| A | 3 | À compléter | À compléter | À compléter | À compléter | À compléter | À compléter |
| B | 3 | À compléter | À compléter | À compléter | À compléter | À compléter | À compléter |
| C | 3 | À compléter | À compléter | À compléter | À compléter | À compléter | À compléter |

> En mode enseignement, utilise des données remises par le mentor ou marque les
> valeurs comme hypothétiques. Ne prétends pas avoir exécuté un benchmark.

**Recommandation conditionnelle :** à compléter.

**Limite de cette fiche :** à compléter.

## Fiche 5 — Consumer group et rebalance

### Affectation initiale

<!-- Dessine au moins trois partitions et trois instances. -->

À compléter.

### Arrêt d'une instance

1. Que deviennent ses partitions ? À compléter.
2. Que devient le lag pendant le rebalance ? À compléter.
3. Dans quelles conditions une relecture est-elle possible ? À compléter.
4. Comment distinguer relecture, doublon et perte ? À compléter.
5. Quelles métriques soutiennent la conclusion ? À compléter.

### Compose et mise à jour progressive

Explique ce que Docker Compose permet de lancer et ce qu'il n'orchestre pas
automatiquement.

À compléter.

**Limite de cette fiche :** à compléter.

## Autoévaluation avant revue

- [ ] J'ai répondu avec mes propres mots.
- [ ] J'ai séparé observation, interprétation et conclusion.
- [ ] J'ai marqué chaque donnée non mesurée comme hypothétique.
- [ ] J'ai indiqué une limite par fiche.
- [ ] Je peux défendre chaque décision sans lire le document mot à mot.

## Retour du mentor

À remplir après la revue orale.
