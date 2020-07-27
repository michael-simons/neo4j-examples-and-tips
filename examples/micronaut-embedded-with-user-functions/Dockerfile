FROM openjdk:14-alpine
COPY target/embedded_with_user_functions-*.jar embedded_with_user_functions.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "embedded_with_user_functions.jar"]