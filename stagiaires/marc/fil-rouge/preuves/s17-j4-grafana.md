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

## Preuve de contrat

La commande suivante valide le JSON, les cinq panneaux, leurs unités, les
requêtes PromQL, le provisioning et les montages Compose :

```bash
sbt 'testOnly clearing.v32.GrafanaProvisioningSpec'
docker compose -f docker/docker-compose-v32.yml config --quiet
```

La capture d'écran et la vérification de données du simulateur seront ajoutées
au gate runtime S17. Le contrat de fichier seul ne prouve pas encore que
Grafana a rendu les séries d'un processus réel.
