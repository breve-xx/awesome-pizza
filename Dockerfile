FROM gcr.io/distroless/java21-debian12

ADD build/libs/pizza-0.0.1-SNAPSHOT.jar app.jar

CMD ["app.jar"]