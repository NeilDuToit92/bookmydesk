FROM azul/zulu-openjdk-alpine:17
EXPOSE 80
ADD deskbooking*.jar /app/deskbooking.jar
WORKDIR /app
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar -Dspring.profiles.active=prod deskbooking.jar"]