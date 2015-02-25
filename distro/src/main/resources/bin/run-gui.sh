#!/bin/sh
DIRNAME=`dirname ${0}`

ROOTDIR=`cd "$DIRNAME/.."; pwd`

DEF_PATHS=${1}

if [ "x${DEF_PATHS}" == "x" ]; then
  DEF_PATHS="gui-defaults.properties"
fi

CP="${ROOTDIR}:${ROOTDIR}/config/*:${ROOTDIR}/lib/bqt-qui*:${ROOTDIR}/lib/*"

ARGS="-Dlog4j.configurationFile=${ROOTDIR}/config/log4j2-gui.xml"
ARGS="${ARGS} -Dbqt.gui.default.paths=${DEF_PATHS}"

echo "CP=${CP}"

java -cp "${CP}" ${ARGS} org.jboss.bqt.gui.GUIClient