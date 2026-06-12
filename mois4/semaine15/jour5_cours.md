---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 15"
footer: "Jour 5 — Revue Pipeline & Clearing Engine v3.0"
---

# Bilan Semaine 15
## Le Temps Réel au service du Clearing

**Durée :** ~2h | **Fil Rouge :** Clearing Engine v3.0 — Le Moteur Réactif

---

# 📋 Objectifs du Jour

- Synthétiser l'architecture de streaming Kafka.
- Analyser les bénéfices du dédoublonnage et du commit manuel.
- Visualiser le pipeline complet : Simulation -> Kafka -> Moteur v2.3 -> Display.
- Livrer la version 3.0 (Streaming) du moteur.

---

# 1. Rétrospective Architecturale

### Version 2.3 (Mois 3)
- Pure, Typée, Indestructible.
- Mais s'exécutait sur une collection statique en mémoire (`List`).

### Version 3.0 (S15)
- **Connectée** : Lit ses données depuis un flux Kafka.
- **Réactive** : Traite chaque transaction en quelques millisecondes.
- **Scalable** : Prête à être déployée en plusieurs instances.
- **Garantie** : Plus aucun message n'est perdu ni traité en double.

---

# 🛡️ La Puissance du Streaming

En passant à Kafka, ATH peut maintenant proposer un clearing "Intraday" :
- Les banques connaissent leur position nette en temps réel.
- Le risque systémique est réduit par une visibilité immédiate.
- On ne dépend plus de la livraison d'un fichier CSV par email à minuit.

---

# 🏗️ Architecture du Clearing Engine v3.0

```mermaid
graph LR
    Bank[Banque Simulator] -- Push CSV --> Kafka((Kafka Topic))
    Kafka -- Poll --> Consumer[Clearing Engine v3.0]
    Consumer -- Validate --> Pure[Pure Logic v2.3]
    Pure -- Result --> UI[Console Display]
```

---

# 🚀 Vers la Semaine 16

Dernière étape du mois 4 : La mémoire à long terme.
1. **Cassandra** : Pourquoi une base NoSQL pour stocker des milliards de transactions ?
2. **Modélisation par Query** : Oublie le SQL classique, pense à tes résultats d'abord.
3. **Persistance Asynchrone** : Enregistrer le résultat du clearing sans ralentir le flux Kafka.
4. **Démo Finale Mois 4** : Le système distribué complet.

---

# 🧠 Quiz de Fin de Semaine

1. Quelle est la différence entre un Offset et un ID de transaction ? (L'offset est technique Kafka, l'ID est métier).
2. Pourquoi subdiviser un topic en plusieurs partitions ? (Pour permettre à plusieurs consumers de travailler en même temps).
3. Quel est le risque de désactiver l'auto-commit ? (Si on oublie de commiter manuellement, on relira toujours les mêmes messages).

---

# 📝 Conclusion

Félicitations ! Tu as fait entrer le système d'ATH dans l'ère du Big Data. Ton moteur est maintenant une brique d'infrastructure moderne, capable de gérer les flux financiers d'un pays entier.

**Dernière étape** : Finaliser la v3.0 dans le TP 75 !
