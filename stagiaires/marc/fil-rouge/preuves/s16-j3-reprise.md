# Preuve S16 J3 — État durable et fenêtres de crash

Date : 14 juillet 2026

## Ordre imposé

```text
load state
→ Received
→ projections idempotentes
→ Projected
→ publication Kafka acquittée
→ Completed
→ offset + 1 committable
```

`Completed` vient volontairement après l'ack Kafka. Le déplacer avant la
publication pourrait perdre une sortie après un crash.

## Reprises testées

- projection échouée : état `Received`, aucune publication, replay complet ;
- publication échouée : état `Projected`, replay sans nouvelle projection ;
- panne après ack et avant `Completed` : replay et seconde publication
  possibles, projections inchangées ;
- état `Completed` : doublon absorbé durablement et offset autorisé ;
- même ID, autre fingerprint : `EVENT_ID_CONFLICT` durable en DLQ ;
- deux offsets invalides portant les mêmes octets : deux DLQ distinctes; le
  replay du même topic/partition/offset est absorbé ;
- échec d'une partition : arrêt local et `retryOffset` au premier record non
  terminé, sans bloquer les autres partitions.

La fenêtre ack/`Completed` démontre la limite at-least-once : un doublon output
reste possible. Le système préfère ce doublon à une perte silencieuse.
