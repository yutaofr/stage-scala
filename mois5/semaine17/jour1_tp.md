# TP Jour 1 : Des Logs pour les Machines

**Durée :** ~4h | **Fil Rouge :** Logback & JSON Layout

---

## Exercice 1 : Installation de Logback (1h)

1. Ajoute les dépendances `ch.qos.logback` et `logstash-logback-encoder`.
2. Crée un fichier `logback.xml` dans tes ressources.
3. Configure un Appender qui affiche les logs dans la console dans un format "Human Readable".

---

## Exercice 2 : Passage au JSON (1h30)

1. Modifie ton `logback.xml` pour utiliser le `LogstashEncoder`.
2. Relance ton moteur et observe les logs : ils doivent maintenant apparaître comme des lignes JSON compactes.
3. **Répartition** : Comment ferais-tu pour avoir du texte en local et du JSON en production ? (Utiliser des profils ou des variables d'environnement).

---

## Exercice 3 : Mapped Diagnostic Context (MDC) (1h30)

1. Utilise `MDC.put("txId", tx.id)` au début de ton traitement ZIO.
2. Vérifie que chaque log généré dans cette Fiber contient automatiquement le champ `"txId"`.
3. N'oublie pas de nettoyer le MDC à la fin (ou utilise les fonctionnalités natives de ZIO-Logging).

**Livrable** : Fichier `logback.xml` configuré et un extrait de log JSON généré par le moteur.
