Compile
-------

clif requires a clif-core artifact, that can be installed using following command

    mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -DgroupId=org.ow2.clif -DartifactId=clif-core -Dversion=2.1.0-SNAPSHOT -Dpackaging=jar -Dfile=./clif-core.jar

(clif-core should be at least revision 2886)

