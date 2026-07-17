# Semaine 18 — Robustesse et performance

## Contrat pédagogique

- **Mode :** enseignement uniquement.
- **Point de référence :** le fil rouge `v3.2`, tagué `marc-v3.2`.
- **Hors périmètre :** aucun code de production, aucune version `v3.3`, aucune
  modification du fil rouge.
- **Responsabilité de Marc :** formuler ses hypothèses, compléter le dossier
  d'apprentissage et défendre ses conclusions.
- **Responsabilité du mentor :** expliquer, questionner et donner un retour sans
  fournir la réponse prête à rendre.

## Objectif de la semaine

À la fin de S18, Marc doit savoir concevoir et critiquer une démarche de test
de robustesse et de performance pour un backend Scala utilisant Kafka et
Cassandra. Il doit distinguer mesure, interprétation et preuve, puis expliquer
les limites de chaque expérience.

## Notions à maîtriser

- charge ouverte, ramp-up, plateau, pic, test de charge, stress test et test
  d'endurance ;
- débit, taux d'erreur, p50, p95, p99, lag Kafka et point de rupture ;
- hypothèse de chaos, condition d'arrêt, panne injectée, observation et reprise ;
- heap, allocation, GC, CPU, thread dump, profil d'échantillonnage, hot method
  et GC root ;
- comparaison expérimentale : baseline, variable unique, répétitions, médiane
  et variabilité ;
- consumer group, partitions, affectation, rebalance, relecture, doublon et
  continuité de service ;
- limites de Docker Compose pour une mise à jour progressive.

## Déroulement quotidien

### Jour 1 — Construire un modèle de charge

**Cours guidé :** relier un profil de trafic à une question mesurable ; lire
throughput, erreurs, p50, p95 et p99 sans confondre moyenne et queue de latence.

**Exercices de Marc :**

- [ ] Classer trois scénarios proposés par le mentor : charge, stress ou
  endurance, puis justifier chaque choix.
- [ ] Dessiner une rampe, un plateau et un pic adaptés au fil rouge `v3.2`.
- [ ] Définir le premier SLO qui doit arrêter l'expérience.
- [ ] Expliquer comment une seed fixe aide la comparaison sans garantir à elle
  seule une expérience reproductible.

**Question de validation :** comment prouver qu'un seuil mesuré est une limite
du système et non un artefact du générateur de charge ?

### Jour 2 — Raisonner comme un chaos engineer

**Cours guidé :** écrire l'enchaînement hypothèse → expérience → observation →
conclusion, avec une condition d'arrêt définie avant la panne.

**Exercices de Marc :**

- [ ] Préparer une expérience d'indisponibilité Cassandra.
- [ ] Préparer une expérience de latence réseau progressive.
- [ ] Prédire l'évolution des offsets, du lag, des retries et des écritures
  avant de consulter les observations de référence.
- [ ] Séparer reprise, relecture, doublon et perte dans le compte rendu.

**Question de validation :** pourquoi couper un broker Kafka isolé ne démontre
pas la tolérance de panne d'un cluster Kafka ?

### Jour 3 — Lire un profil JVM

**Cours guidé :** comprendre heap, cycles GC, CPU sampling, hot methods,
dominator tree et chemins vers les GC roots.

**Exercices de Marc :**

- [ ] Annoter une courbe heap et repérer allocation, GC et niveau après GC.
- [ ] Expliquer ce qu'un profil CPU permet d'affirmer et ce qu'il ne prouve pas.
- [ ] Proposer une méthode pour comparer un profil avant et après un changement.
- [ ] Distinguer objet dominant, fuite mémoire et cache volontaire.

**Question de validation :** quelles preuves faut-il réunir avant d'appeler une
méthode un goulot d'étranglement ?

### Jour 4 — Comparer des hypothèses GC

**Cours guidé :** considérer les paramètres JVM comme des hypothèses à tester,
pas comme des recettes universelles.

**Exercices de Marc :**

- [ ] Définir les variables qui doivent rester identiques entre deux essais.
- [ ] Préparer une comparaison de trois configurations heap/GC.
- [ ] Expliquer l'intérêt de trois répétitions, de la médiane et de la
  variabilité.
- [ ] Formuler une recommandation avec marge et limites, sans promettre un gain
  non mesuré.

**Question de validation :** pourquoi une p95 plus basse ne suffit-elle pas à
déclarer une configuration meilleure ?

### Jour 5 — Expliquer la haute disponibilité

**Cours guidé :** relier partitions Kafka, membres du consumer group,
affectation, rebalance, lag et sémantique de traitement.

**Exercices de Marc :**

- [ ] Dessiner l'affectation d'au moins trois partitions à trois instances.
- [ ] Prédire ce qui change quand une instance s'arrête.
- [ ] Distinguer transaction manquante, dupliquée et relue.
- [ ] Expliquer pourquoi trois conteneurs et un rolling update automatique sont
  deux propriétés différentes.

**Question de validation :** quelles observations démontrent une continuité de
traitement sans perte après un rebalance ?

## Premier livrable

Marc complète
[`dossier-s18.md`](dossier-s18.md). Le dossier contient cinq fiches courtes :

1. un modèle de charge et ses seuils ;
2. une expérience de chaos ;
3. l'interprétation d'un profil JVM ;
4. une matrice de comparaison GC ;
5. un raisonnement sur l'arrêt d'une instance et le rebalance.

Le dossier reste analytique : il ne demande ni modification du fil rouge ni
résultat de benchmark présenté comme réellement exécuté.

## Critères de validation

- [ ] Marc emploie correctement les notions de charge et les percentiles.
- [ ] Chaque expérience part d'une hypothèse falsifiable et possède une
  condition d'arrêt.
- [ ] Marc sépare mesure, interprétation et conclusion.
- [ ] La comparaison GC contrôle les variables et traite la variabilité.
- [ ] Le raisonnement Kafka explique affectation, rebalance et sémantique des
  doublons/relectures.
- [ ] Marc formule au moins une limite pour chacune des cinq fiches.
- [ ] Marc défend le dossier pendant une revue orale de 30 minutes.

## État

**En cours — enseignement uniquement.** Aucun critère n'est coché avant la
remise et la revue du travail personnel de Marc.
