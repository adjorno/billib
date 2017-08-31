FROM gcr.io/google_appengine/openjdk
VOLUME /tmp
ADD billib-rest-service-0.0.1.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT [ "java", "-jar", "/app.jar" ]