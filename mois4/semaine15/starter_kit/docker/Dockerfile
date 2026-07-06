FROM sbtscala/scala-sbt:eclipse-temurin-17_1.x

WORKDIR /app

# Copier les fichiers de build
COPY build.sbt /app/
COPY project/build.properties /app/project/

# Lancer sbt update pour mettre en cache les dépendances sbt
RUN sbt update

# Copier le code source
COPY . /app

CMD ["sbt", "test"]
