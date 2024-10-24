#!/bin/bash -e

launch_engine() {
  echo "Launching engine"

  ENGINE_PATH="/opt/supra/java/"
  JAVA_FILE=$(find $ENGINE_PATH -name "*.jar")

  DEFAULT_JAVA_OPTS="-XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m \
                     -Xms512m -Xmx512m -Xmn128m -Xss256k \
                     -XX:SurvivorRatio=8 \
                     -XX:+PrintGCDetails \
                     -XX:ParallelGCThreads=4 \
                     -Xverify:none"

  JAVA_RUNNING_OPTIONS=${JAVA_OPTS:=$DEFAULT_JAVA_OPTS}

  java -jar $JAVA_RUNNING_OPTIONS $JAVA_FILE > /opt/supra/logs/app.log &
  echo "Engine Java launched with options: $JAVA_RUNNING_OPTIONS"
}

get_vmsettings() {
  if [ ! -f /opt/supra/config/vmSettings.json ] ; then
    echo "Copying default vmSettings..."
    cp /opt/supra/config.default/vmSettings.json /opt/supra/config/vmSettings.json
  fi
}

get_rule() {
  if [ ! -f /opt/supra/config/rule.json ] ; then
    echo "Copying default rules..."
    cp /opt/supra/config.default/rule.json /opt/supra/config/rule.json
  fi
}

get_vmsettings

get_rule

launch_engine

# Wait for any process to exit
wait -n

# Exit with status of process that exited first
exit $?