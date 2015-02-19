#!/bin/bash

# First figure out where we're being run from
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
CONFIG="$(dirname $DIR)/configs/config.xml"

MACHINE=$(hostname)
if [ x"$MACHINE" = x"" ] ; then
  MACHINE=MACHINE
fi

RACK=$(ifconfig | grep "$(hostname -I)" | sed -e 's/^.*Bcast://' -e 's/ .*//')
if [ x"$RACK" = x"" ] ; then
  RACK=RACK
fi

SITE=$(host $(route -n | grep '^0.0.0.0' | awk '{print $2}') | grep 'domain name pointer' | awk '{print $5}' | sed -e 's/^[^.]*\.//' -e 's/\.$//')
if [ x"$SITE" = x"" ] ; then
  SITE=$(route -n | grep '^0.0.0.0' | awk '{print $2}')
fi

case "$1" in
  left)
    ARGS="
  -Dinfinispan.config=$CONFIG
  -Dinfinispan.ajp.port=8009
  -Dinfinispan.http.port=8080
  -Dinfinispan.hotrod.port=11222
  -Dinfinispan.memcached.port=11211
  -Dinfinispan.transport.tcp.node=left
  -Dinfinispan.transport.tcp.machine="$MACHINE"
  -Dinfinispan.transport.tcp.rack="$RACK"
  -Dinfinispan.transport.tcp.site="$SITE"
"
  ;;
  right)
    ARGS="
  -Dinfinispan.config=configs/config.xml
  -Dinfinispan.ajp.port=8010
  -Dinfinispan.http.port=8081
  -Dinfinispan.hotrod.port=11223
  -Dinfinispan.memcached.port=11212
  -Dinfinispan.transport.tcp.node=right
  -Dinfinispan.transport.tcp.machine="$MACHINE"
  -Dinfinispan.transport.tcp.rack="$RACK"
  -Dinfinispan.transport.tcp.site=$SITE
"
  ;;
  *)
    echo "Pick a side <left | right>"
    exit 1
  ;;
esac

java -jar $ARGS target/infinispan.war
