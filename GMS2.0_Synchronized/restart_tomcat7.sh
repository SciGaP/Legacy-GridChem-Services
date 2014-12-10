export CATALINA_HOME=/home/ccguser/production/apache-tomcat-7.0.56
export CATALINA_OPTS="-Xms1024m -Xmx1500m"

/home/ccguser/production/apache-tomcat-7.0.56/bin/shutdown.sh
sleep 5
/home/ccguser/GMS2.0_Synchronized/startup_tomcat7.sh &
sleep 10
