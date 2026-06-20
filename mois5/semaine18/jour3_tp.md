# TP Jour 3 : Sherlock Holmes de la JVM

**Durée :** ~4h | **Fil Rouge :** Analyse de performance avec VisualVM

---

## Point de départ

- Utilise le **Kit 18.3** de `starter_kit_s18.md`.
- Préfère une connexion locale ; n'expose pas JMX sans authentification sur un réseau partagé.
- Enregistre la version JVM et les paramètres de lancement.

## Exercice 1 : Connexion JMX (1h)

1. Lance ton moteur de clearing avec les flags JMX activés (pour permettre la connexion externe).
2. Ouvre `VisualVM` (ou un outil équivalent).
3. Connecte-toi au processus de ton application.
4. Observe le graphique de la Heap.

**Validation :** la courbe montre au moins deux cycles GC et revient vers un niveau comparable après collecte.

---

## Exercice 2 : Détection de "Hotspots" (1h30)

1. Lance une charge stable, pas une rampe.
2. Capture une baseline puis un échantillon CPU.
3. Identifie les méthodes dominantes et leur pourcentage.
4. Modifie une seule chose et répète la mesure.

**Validation :** le gain annoncé compare deux profils sous la même charge.

---

## Exercice 3 : Capture de Heap Dump (1h30)

1. Active le cache de démonstration borné/déborné fourni.
2. Capture un heap dump dans l'environnement jetable.
3. Analyse le dominator tree et les chemins vers les GC roots.
4. Remets une limite au cache et répète.

**Validation :** le second profil ne montre plus une croissance continue du même type d'objet.

**Livrable** : Rapport d'analyse CPU montrant les fonctions les plus gourmandes et capture d'écran de l'analyse de mémoire.
