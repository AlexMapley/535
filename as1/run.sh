# mvn compile
mvn compile assembly:single
cd target
java -jar COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar ../conf/router$1.conf
