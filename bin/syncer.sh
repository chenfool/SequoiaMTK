#!/bin/bash

localFileName=`basename $0`
binpath=$( cd "$(dirname $0)"; pwd; )

CP=${CLASSPATH}
CANALLIBPATH=${binpath}/../lib/canal

for jar in $(ls ${binpath}/../lib/canal );
do
   CP=${CANALLIBPATH}/${jar}:${CP};
done

syncer_jar=$(ls ${binpath}/../lib/syncer*.jar)
syncer_jar=$(basename ${syncer_jar})

java -Dlog4j.configuration="file:${binpath}/../conf/syncer.log4j.properties" -cp $CP:${binpath}/../lib/${syncer_jar} com.sequoiadb.canal.client.Syncer &
jps | grep Syncer | awk -F ' ' '{print $1}'
