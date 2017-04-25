FROM openjdk:8-jdk

WORKDIR /code
RUN apt-get update && apt-get install libgs-dev zbar-tools -y

ADD build/dist/edustor-recognition.jar .

HEALTHCHECK CMD curl -f http://localhost:8080/version
CMD java -jar edustor-recognition.jar