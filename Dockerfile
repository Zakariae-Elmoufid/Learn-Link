# Utiliser une image Java officielle
FROM eclipse-temurin:17-jdk
# Copier le JAR généré dans le conteneur
COPY target/LearnLink-0.0.1-SNAPSHOT.jar app.jar


# Exposer le port 8081
EXPOSE 8081

# Lancer l'application
ENTRYPOINT ["java", "-jar", "/app.jar"]