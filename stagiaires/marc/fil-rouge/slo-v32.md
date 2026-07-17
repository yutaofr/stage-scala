# Clearing Engine v3.2 — SLI, SLO et budgets d'erreur

## Portée

Ces objectifs décrivent le laboratoire S17. Ils ne constituent pas un
engagement de production : la semaine 17 ne comporte pas encore de
qualification de charge représentative ni de déploiement multi-instance. La
fenêtre glissante est de 30 jours pour les deux objectifs.

## SLO 1 — Disponibilité de traitement

| Élément | Définition |
|---|---|
| SLI | ratio des records terminés avec `status=success` ou `status=duplicate` sur tous les records traités |
| population | records Kafka effectivement remis au processor durable |
| target | au moins 99,5 % sur 30 jours |
| source | `clearing_transactions_processed_total{status}` dans Prometheus |
| budget d'erreur | 0,5 %, soit 1 record en échec pour 200 records traités |

La requête protège le dénominateur avec `clamp_min`. Une période sans trafic
n'est ni un succès ni un échec; le dashboard affiche zéro et l'alerte de ratio
reste inactive. Les données invalides correctement routées en DLQ comptent
comme `success`, car le système les a classées et persistées comme prévu. Une
erreur de publication ou de persistance compte comme `failure`.

## SLO 2 — Latence de traitement

| Élément | Définition |
|---|---|
| SLI | proportion des records dont la durée durable complète est inférieure ou égale à 500 ms |
| population | records observés par `clearing_processing_duration_seconds` |
| target | au moins 99 % sous 500 ms sur 30 jours |
| source | histogramme Prometheus `clearing_processing_duration_seconds_bucket` |
| budget d'erreur | 1 %, soit 1 record lent pour 100 records observés |

Le p99 sert de signal d'alerte à court terme; le calcul d'un SLO exact sur 30
jours utiliserait le bucket `le="0.5"` rapporté au count total. Sans trafic,
l'histogramme ne produit pas de nouveau point et le panneau indique `No data`.

## Maintenance et limites

Une maintenance planifiée n'est pas automatiquement soustraite des fenêtres.
Elle doit être annotée et exclue par une règle de reporting approuvée avant le
calcul; supprimer les points après coup est interdit. Les périodes où
Prometheus ne scrape aucune cible sont une perte de données, pas une preuve de
respect du SLO. `EngineDown` les signale séparément.

Le redémarrage remet les counters en mémoire à zéro. Les fonctions `rate`
tolèrent ce reset, mais une longue interruption de Prometheus détruit la source
du calcul. Le SLO sera réévalué en semaine 18 avec le gate de robustesse et de
performance.
