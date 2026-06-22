Voici une **première liste d’audit**, pour vérifier si l’application AIX / Java 8 utilise réellement le **SecurityManager**.

## Liste d’audit SecurityManager

### 1. Audit des paramètres de démarrage

Vérifier dans les scripts de lancement, fichiers `.sh`, `.ksh`, `.conf`, `.properties`, `jvm.options`, etc. :

```text
-Djava.security.manager
-Djava.security.manager=default
-Djava.security.manager=allow
-Djava.security.manager=<classe custom>
-Djava.security.policy=...
-Dpolicy.provider=...
-Dpolicy.url.1=...
-Dpackage.access=...
-Dpackage.definition=...
-Djava.security.debug=...
```

Risque principal : en Java 25, ces paramètres ne sont plus compatibles avec le SecurityManager.

---

### 2. Audit des processus Java en runtime

Sur AIX, vérifier les processus Java actifs :

```sh
ps -ef | grep '[j]ava'
```

Si disponible :

```sh
jcmd <PID> VM.command_line
jcmd <PID> VM.system_properties
jcmd <PID> VM.flags
```

Objectif : confirmer si le SecurityManager est réellement activé au démarrage.

---

### 3. Audit des variables d’environnement

Vérifier :

```text
JAVA_OPTS
JVM_ARGS
IBM_JAVA_OPTIONS
JDK_JAVA_OPTIONS
SERVER_JVM_OPTIONS
CATALINA_OPTS
MAVEN_OPTS
GRADLE_OPTS
```

Commandes utiles :

```sh
env | egrep -i 'JAVA|JDK|JRE|IBM|WAS|WEBSPHERE|LIBERTY|TOMCAT|CATALINA'
```

---

### 4. Audit des fichiers de policy

Rechercher les fichiers :

```text
*.policy
java.policy
server.policy
app.policy
client.policy
```

Commandes :

```sh
find /app /opt /usr /etc -type f -name "*.policy" 2>/dev/null
```

Dans ces fichiers, rechercher :

```text
grant
permission
AllPermission
FilePermission
SocketPermission
RuntimePermission
ReflectPermission
PropertyPermission
MBeanPermission
AuthPermission
PrivateCredentialPermission
```

Objectif : identifier les règles de sécurité qui devront être remplacées hors JVM en Java 25.

---

### 5. Audit du code source : SecurityManager direct

Rechercher dans le code :

```text
SecurityManager
System.setSecurityManager
System.getSecurityManager
extends SecurityManager
new SecurityManager
RMISecurityManager
checkPermission
checkRead
checkWrite
checkDelete
checkConnect
checkExit
checkPackageAccess
```

Commande :

```sh
grep -RInE "SecurityManager|System\.setSecurityManager|System\.getSecurityManager|RMISecurityManager|checkPermission|checkRead|checkWrite|checkDelete|checkConnect|checkExit" src/
```

---

### 6. Audit du code source : AccessController / Policy

Rechercher :

```text
AccessController
AccessControlContext
doPrivileged
doPrivilegedWithCombiner
PrivilegedAction
PrivilegedExceptionAction
Policy.getPolicy
Policy.setPolicy
ProtectionDomain
PermissionCollection
CodeSource
DomainCombiner
```

Commande :

```sh
grep -RInE "AccessController|AccessControlContext|doPrivileged|PrivilegedAction|Policy\.getPolicy|Policy\.setPolicy|ProtectionDomain|PermissionCollection|CodeSource" src/
```

---

### 7. Audit des permissions Java utilisées dans le code

Rechercher :

```text
FilePermission
SocketPermission
RuntimePermission
ReflectPermission
PropertyPermission
SecurityPermission
AllPermission
MBeanPermission
AuthPermission
PrivateCredentialPermission
ServicePermission
DelegationPermission
```

Commande :

```sh
grep -RInE "FilePermission|SocketPermission|RuntimePermission|ReflectPermission|PropertyPermission|SecurityPermission|AllPermission|MBeanPermission|AuthPermission|PrivateCredentialPermission" src/
```

---

### 8. Audit JAAS / Subject

Rechercher :

```text
Subject.doAs
Subject.doAsPrivileged
Subject.getSubject
Subject.current
Subject.callAs
LoginContext
PrivateCredentialPermission
```

Commande :

```sh
grep -RInE "Subject\.doAs|Subject\.doAsPrivileged|Subject\.getSubject|Subject\.current|Subject\.callAs|LoginContext|PrivateCredentialPermission" src/
```

Objectif : identifier les usages liés à l’identité, à JAAS ou à Kerberos.

---

### 9. Audit des dépendances JAR

Lister les JAR :

```sh
find /app/myapp -type f -name "*.jar" > jars.txt
```

Scanner les dépendances avec `jdeprscan` :

```sh
jdeprscan --release 25 --for-removal app.jar
```

Scanner les API internes :

```sh
jdeps --jdk-internals app.jar
```

Faire une recherche simple dans les classes :

```sh
while read j
do
  unzip -p "$j" '*.class' 2>/dev/null | strings | \
  egrep "SecurityManager|AccessController|Policy|doPrivileged|checkPermission|Subject.doAs"
done < jars.txt
```

---

### 10. Audit des fichiers de build

Vérifier :

```text
pom.xml
build.gradle
build.gradle.kts
gradle.properties
build.xml
```

Rechercher :

```text
java.security.manager
java.security.policy
SecurityManager
doPrivileged
argLine
jvmArgs
```

---

### 11. Audit des serveurs d’application

Selon le contexte, vérifier :

```text
WebSphere
Liberty
Tomcat
Spring Boot wrapper
scripts maison
batch launcher
cron
inittab
```

Fichiers typiques :

```text
server.xml
server.env
jvm.options
setenv.sh
setenv.ksh
bootstrap.properties
application.properties
```

---

### 12. Audit runtime applicatif

Ajouter temporairement un log au démarrage :

```java
SecurityManager sm = System.getSecurityManager();

System.out.println("SecurityManager active: " + (sm != null));
System.out.println("SecurityManager class : " + (sm == null ? "<null>" : sm.getClass().getName()));
System.out.println("java.security.manager = " + System.getProperty("java.security.manager"));
System.out.println("java.security.policy  = " + System.getProperty("java.security.policy"));
```

Objectif : confirmer si le SecurityManager est actif réellement, et pas seulement présent dans les fichiers.

---

### 13. Audit comportemental

Tester concrètement les anciennes restrictions :

```text
Lecture d’un fichier autorisé
Lecture d’un fichier interdit
Écriture dans un répertoire interdit
Connexion réseau vers un host autorisé
Connexion réseau vers un host interdit
Appel à System.exit
Usage de la réflexion
Chargement dynamique de classes
Exécution de scripts ou plugins
Propagation du Subject JAAS
```

Objectif : détecter les contrôles qui étaient réellement appliqués par le SecurityManager.

---

### 14. Classification des résultats

Classer chaque finding :

| Niveau | Description                                             |
| ------ | ------------------------------------------------------- |
| P0     | Bloquant Java 25 : démarrage ou exécution impossible    |
| P1     | Risque sécurité : ancienne restriction devenue inactive |
| P2     | Risque fonctionnel : JAAS, Subject, audit, identité     |
| P3     | Code legacy non bloquant mais à nettoyer                |

---

### 15. Matrice finale à produire

Pour chaque élément trouvé :

| ID     | Source  | Fichier / JAR  | Élément détecté           | Usage actuel         | Impact Java 25         | Risque | Action                         |
| ------ | ------- | -------------- | ------------------------- | -------------------- | ---------------------- | ------ | ------------------------------ |
| SM-001 | startup | start.ksh      | `-Djava.security.manager` | Active SM            | JVM refuse de démarrer | P0     | Supprimer                      |
| SM-002 | code    | ExitGuard.java | `extends SecurityManager` | Bloque `System.exit` | Non supporté           | P0     | Refactorer                     |
| SM-003 | policy  | app.policy     | `FilePermission`          | Limite fichiers      | Contrôle perdu         | P1     | Remplacer par ACL AIX          |
| SM-004 | jar     | legacy.jar     | `doPrivileged`            | Privilège local      | Sémantique changée     | P2     | Analyser                       |
| SM-005 | code    | Auth.java      | `Subject.doAs`            | Contexte JAAS        | À migrer               | P2     | Remplacer par `Subject.callAs` |

---

La logique d’audit est simple : **ne pas seulement chercher si le mot “SecurityManager” existe, mais vérifier si l’application dépendait réellement de lui comme barrière de sécurité**. C’est cette dépendance qui devient critique en migration Java 25.
