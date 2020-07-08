From openjdk:8
copy ./target/OrderbookManagement-0.0.1-SNAPSHOT.jar OrderbookManagement-0.0.1-SNAPSHOT.jar	
CMD ["java","-jar","OrderbookManagement-0.0.1-SNAPSHOT.jar"]