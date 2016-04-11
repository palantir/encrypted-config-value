FROM nimmis/java-centos:openjdk-7-jdk

ENV JAVA_HOME=/usr/lib/jvm/java

# set BouncyCastle as security provider
RUN echo "security.provider.10=org.bouncycastle.jce.provider.BouncyCastleProvider" >> "$JAVA_HOME/jre/lib/security/java.security"
