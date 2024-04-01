call mvn install:install-file -Dfile="resources\\jpo-ode-plugins-2.0.0-SNAPSHOT.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-plugins -Dversion="2.0.0-SNAPSHOT" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\jpo-ode-core-2.0.0-SNAPSHOT.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-core -Dversion="2.0.0-SNAPSHOT" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\jpo-ode-common-2.0.0-SNAPSHOT.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-common -Dversion="2.0.0-SNAPSHOT" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\jpo-ode-svcs.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-svcs -Dversion="2.0.0-SNAPSHOT" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\postgresql-42.6.0.jar" -DgroupId="org.postgresql" -DartifactId=postgresql -Dversion="42.6.0" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\ucp.jar" -DgroupId="com.oracle" -DartifactId=ucp -Dversion="12.2.0.1.0" -Dpackaging=jar