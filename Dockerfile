FROM balenalib/raspberry-pi-openjdk:8-stretch-run
COPY build/libs/bisecur2mqtt.jar /app/bisecur2mqtt.jar
WORKDIR /app
CMD ["java", "-jar", "bisecur2mqtt.jar"]