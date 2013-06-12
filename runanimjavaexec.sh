TOOL_JAR=/usr/local/jdk1.6.0_20/lib/tools.jar
TOOL_JAR=$JAVA_HOME/lib/tools.jar
# AnimJavaExec with JavaUtils...
ANIMJAVAEXEC_JAR=~/workspace/AnimJavaExec/animexe.jar

# We can not use -jar option as we HAVE to set . in -cp for
# the jvm spanw by jdi.

java -cp .:$ANIMJAVAEXEC_JAR:$TOOL_JAR fr.loria.madynes.animjavaexec.Main $*
