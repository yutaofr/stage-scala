# S17 J4 — Dashboard Grafana provisionné

## Réalisation

Grafana 13.0.2 charge une datasource Prometheus portant l'UID stable
`prometheus-clearing`. Le provider lit le dashboard versionné depuis
`/var/lib/grafana/dashboards`. Les deux arborescences de configuration sont
montées en lecture seule; seul le volume interne Grafana reste persistant.

Le dashboard `clearing-engine-v32` fixe une fenêtre initiale de 15 minutes et
un rafraîchissement toutes les 5 secondes. Il contient :

- le débit en records par seconde;
- le ratio d'échec protégé contre la division par zéro;
- la latence p99 issue de l'histogramme;
- le lag Kafka en records;
- la mémoire JVM en octets.

Chaque panneau documente son comportement sans données. La seule variable est
`$status`, issue du label borné `status`; la valeur All devient `.*`. Aucune
variable `$bank` n'est créée, car le moteur n'expose pas une métrique bancaire
utile et bornée en semaine 17.

## Preuves

La commande suivante valide le JSON, les cinq panneaux, leurs unités, les
requêtes PromQL, le provisioning et les montages Compose :

```bash
sbt 'testOnly clearing.v32.GrafanaProvisioningSpec'
docker compose -f docker/docker-compose-v32.yml config --quiet
```

Le gate runtime a ensuite interrogé le Grafana réellement démarré. L'API a
retourné la datasource `prometheus-clearing` reliée à
`http://prometheus:9090`, puis le dashboard `clearing-engine-v32` intitulé
`Clearing Engine v3.2` avec exactement cinq panneaux. Prometheus avait au même
moment réconcilié 500 traitements et un lag nul.

La capture visuelle n'a pas été conservée : la connexion du navigateur de
l'environnement de travail a échoué avant l'ouverture de l'interface. Cette
limite concerne l'artefact d'illustration, pas le provisioning ni les données,
qui ont été contrôlés par les API réelles du conteneur.
