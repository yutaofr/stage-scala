# Rétrospective — Semaine 17

## Résultat

La semaine 17 transforme le Clearing Engine v3.1 en v3.2 observable sans
modifier son ordre durable. Les observations se branchent sur des ports no-op
par défaut; les versions historiques conservent leur comportement.

## Ce qui a bien fonctionné

- Les tests RED ont forcé une séparation nette entre logique métier et outils
  d'observabilité.
- Les spans `parse`, `validate`, `netting` et `persist` entourent le vrai code,
  puis restent enfants du span `clearing.consume`.
- Le label Prometheus `status` reste borné à trois valeurs. Les IDs et offsets
  restent dans les logs et traces.
- Grafana, Prometheus et Alertmanager chargent des fichiers versionnés; le
  laboratoire ne dépend d'aucune configuration manuelle.
- Le webhook séparé reste disponible lorsque le moteur tombe et conserve les
  notifications firing et resolved.
- Le gate réel a traité 500 records avec un lag final nul et a traversé le
  cycle `pending → firing → resolved`.

## Difficultés et corrections

- La capture initiale de `System.out` ne testait pas correctement l'encoder
  Logback. Le test encode désormais un événement avec l'appender réellement
  configuré par XML.
- Les trace flags W3C ne se limitent pas à `00` et `01`; le test valide les deux
  chiffres hexadécimaux au lieu d'imposer une hypothèse erronée.
- Le registry Prometheus expose `close()` sans implémenter `AutoCloseable`. Un
  adapter de ressource explicite garantit sa fermeture en dernier.
- Un mot de passe Grafana de laboratoire était écrit dans Compose. Un test de
  régression l'a interdit et Grafana utilise maintenant un accès anonyme
  Viewer.
- La connexion navigateur de l'environnement a échoué avant la capture
  Grafana. Les APIs Grafana, Prometheus et Jaeger ont donc servi de preuves
  exécutables; aucune image statique n'a été présentée comme preuve runtime.
- La première version du gate cherchait deux IBAN fixes au lieu des données du
  lot. Le gate extrait désormais les 970 IBAN réels de l'input et exige les
  cardinalités 500/485/5 pour input/output/DLQ.
- La première vérification `resolved` pouvait réutiliser une notification de
  démarrage. Le cycle volontaire est maintenant isolé par baseline et par le
  même `startsAt` Alertmanager.
- Les headers Kafka sont non fiables : seuls les IDs positifs représentables
  en `Int` entrent dans le MDC et les traces; les autres deviennent `unknown`.
- Le hook SIGTERM attend maintenant la fin du thread principal et donc le
  `close()`/flush des ressources, avec un fallback borné à 30 secondes.

## Limites

Les preuves locales démontrent la cohérence des signaux et le cycle d'alerte;
elles ne qualifient ni la capacité de production, ni la haute disponibilité.
Les compteurs repartent à zéro au redémarrage et le lag reflète seulement le
consumer actif. La capture visuelle Grafana reste à refaire depuis un navigateur
fonctionnel. La semaine 18 devra injecter des pannes et mesurer la charge.

## Qualification

- gate v3.2 réel corrigé : 500 records, 485 sorties, 5 DLQ, 970 IBAN réels
  absents des sorties, lag 0, traces présentes, dashboard provisionné, alertes
  pending/firing/resolved corrélées;
- Cassandra live : 1 test d'intégration réussi;
- Java 21.0.6 : 594 tests réussis, 1 test live annulé par défaut;
- Java 17.0.14 : 594 tests réussis, 1 test live annulé par défaut.
- revue senior après correction : 0 Critical, 0 Important, 0 Minor;
  les quatre Important du premier passage sont fermés.

## Preuves

- `preuves/s17-j1-logs.md`;
- `preuves/s17-j2-metrics.md`;
- `preuves/s17-j3-traces.md`;
- `preuves/s17-j4-grafana.md`;
- `preuves/s17-j5-alerting.md`;
- `scripts/verify-v32-runtime.sh` pour le gate réel.
