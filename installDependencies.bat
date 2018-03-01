call mvn install:install-file -Dfile="resources\\jpo-ode-plugins-0.0.1-SNAPSHOT.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-plugins -Dversion="0.0.1-SNAPSHOT" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\jpo-ode-core-0.0.1-SNAPSHOT.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-core -Dversion="0.0.1-SNAPSHOT" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\jpo-ode-common-0.0.1-SNAPSHOT.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-common -Dversion="0.0.1-SNAPSHOT" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\jpo-ode-svcs-0.0.1-SNAPSHOT.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-svcs -Dversion="0.0.1-SNAPSHOT" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\ojdbc8.jar" -DgroupId="com.oracle" -DartifactId=ojdbc8 -Dversion="12.2.0.1.0" -Dpackaging=jar