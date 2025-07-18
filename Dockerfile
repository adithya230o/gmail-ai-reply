FROM openjdk
ADD target/gmail-ai-reply.jar gmail-ai-reply.jar
ENTRYPOINT ["java","-jar","/gmail-ai-reply.jar"]