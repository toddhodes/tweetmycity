#!/bin/bash

dir=`dirname $0`

CLASSPATH=/usr/share/java/bsh.jar

CLASSPATH=$CLASSPATH:$OAUTH_BUILD/lib/oauth-client-1.2.jar
CLASSPATH=$CLASSPATH:$CLIENT_BUILD/lib/veriplace-client-1.2.jar
CLASSPATH=$CLASSPATH:$CLIENT_HOME/lib/commons-codec/commons-codec-1.3.jar
CLASSPATH=$CLASSPATH:$CLIENT_HOME/lib/commons-collections/commons-collections-3.2.jar
CLASSPATH=$CLASSPATH:$CLIENT_HOME/lib/commons-logging/commons-logging-1.1.jar
CLASSPATH=$CLASSPATH:$CLIENT_HOME/lib/commons-httpclient/commons-httpclient-3.1.jar
CLASSPATH=$CLASSPATH:$CLIENT_HOME/lib/tomcat/servlet-api.jar

echo CLASSPATH=$CLASSPATH
java -classpath $CLASSPATH bsh.Interpreter $dir/query.bsh | tee /tmp/tmc

city=`cat /tmp/tmc | tail -1 | sed 's/ /+/g'`
if echo $city | grep my.city
then
  echo "status=$city"
  echo "status=$city" | POST -C ren_n_stimpy:twitpass http://twitter.com/statuses/update.xml >/dev/null
else
  echo "no city to tweet"
fi
