#FROM --platform=linux/amd64 amazonlinux:2
FROM amazoncorretto:17.0.5-al2 AS builder

#RUN echo "#Added , the client\'s certificate" >> /etc/pki/tls/certs/ca-bundle.crt
#COPY certs/host-bundle.crt .
#RUN cat host-bundle.crt >> /etc/pki/tls/certs/ca-bundle.crt

#RUN yum -y update
RUN yum install -y maven zip
#RUN yum -y -q install maven >/dev/null 2>&1


ENV HOME=/root
# RUN mkdir -p $HOME
# WORKDIR $HOME
# ADD . $HOME

#RUN #mkdir $HOME/.m2
#RUN touch $HOME/.m2/settings.xml

#RUN echo \
# " <settings \
#	xmlns='http://maven.apache.org/SETTINGS/1.0.0' \
#	xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \
#	xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 \
#						http://maven.apache.org/xsd/settings-1.0.0.xsd'> \
#	<profiles> \
#		<profile> \
#			<id>wipo</id> \
#			<repositories> \
#				<repository> \
#          			<id>wipo-public</id> \
#          			<name>wipo public repo</name> \
#          			<url>https://intranet.wipo.int/nexus/repository/wipo-public</url> \
#          			<releases><enabled>true</enabled></releases> \
#          			<snapshots><enabled>true</enabled></snapshots> \
#                </repository> \
#		    </repositories> \
#			<pluginRepositories> \
#				<pluginRepository> \
#					<id>wipo-plugins</id> \
#					<name>wipo repo plugins</name> \
#					<url>https://intranet.wipo.int/nexus/repository/wipo-public</url> \
#					<releases> \
#						<enabled>true</enabled> \
#					</releases> \
#					<snapshots> \
#						<enabled>true</enabled> \
#					</snapshots> \
#				</pluginRepository> \
#			</pluginRepositories> \
#		</profile> \
#	</profiles> \
#	<activeProfiles> \
#		<!--make the profile active all the time --> \
#		<activeProfile>wipo</activeProfile> \
#	</activeProfiles> \
#</settings>" \
#    >> $HOME/.m2/settings.xml;


# Set Java 18 as the default
#RUN update-alternatives --set java "/usr/lib/jvm/java-18-amazon-corretto/bin/java"
#RUN update-alternatives --set javac "/usr/lib/jvm/java-18-amazon-corretto/bin/javac"

# ENV JAVA_HOME /usr/lib/jvm/java-18-amazon-corretto
# RUN export JAVA_HOME

#COPY wipo.cer $JAVA_HOME/lib/security
#COPY fw2.cer $JAVA_HOME/lib/security
#RUN cd $JAVA_HOME/lib/security \
#    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias wipocert -file wipo.cer
#RUN \
#    cd $JAVA_HOME/lib/security \
#    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias fw2cert -file fw2.cer

## Builder-0 ENDS

# Copy the software folder to the image and build the function
# FROM builder
COPY software software
WORKDIR /software/artefacts
RUN mvn clean package -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

# FROM builder 
# FROM --platform=linux/amd64 amazonlinux:2
# COPY --from=build /usr/app/target/artefact.jar target/artefact.jar  

# Find JDK module dependencies dynamically from the uber jar
RUN jdeps -q \
    --ignore-missing-deps \
    --multi-release 18 \
    --print-module-deps \
    target/artefact.jar > jre-deps.info

# Create a slim Java 18 JRE which only contains the required modules to run the function
RUN jlink --verbose \
    --compress 2 \
    --strip-java-debug-attributes \
    --no-header-files \
    --no-man-pages \
    --output /jre18-slim \
    --add-modules $(cat jre-deps.info)

# Use Javas Application Class Data Sharing feature
# It creates the file /jre18-slim/lib/server/classes.jsa
RUN /jre18-slim/bin/java -Xshare:dump

# Package everything together into a custom runtime archive
WORKDIR /
COPY bootstrap bootstrap
RUN chmod 755 bootstrap
RUN cp /software/artefacts/target/artefact.jar artefact.jar
RUN zip -r runtime.zip \
    bootstrap \
    artefact.jar \
    /jre18-slim
