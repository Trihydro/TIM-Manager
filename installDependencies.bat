call mvn install:install-file -Dfile="resources\\jpo-ode-plugins-1.0.4.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-plugins -Dversion="1.0.4" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\jpo-ode-core-1.0.4.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-core -Dversion="1.0.4" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\jpo-ode-common-1.0.4.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-common -Dversion="1.0.4" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\jpo-ode-svcs.jar" -DgroupId="us.dot.jpo.ode" -DartifactId=jpo-ode-svcs -Dversion="1.0.4" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\ojdbc8.jar" -DgroupId="com.oracle" -DartifactId=ojdbc8 -Dversion="12.2.0.1.0" -Dpackaging=jar

call mvn install:install-file -Dfile="resources\\ucp.jar" -DgroupId="com.oracle" -DartifactId=ucp -Dversion="12.2.0.1.0" -Dpackaging=jar