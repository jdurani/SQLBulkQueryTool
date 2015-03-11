#!/bin/sh
DIRNAME=`dirname ${0}`

ROOTDIR=`cd "$DIRNAME/.."; pwd`

DEF_PATHS=${1}

DEF_PATHS="gui-defaults.properties"

CP="${ROOTDIR}:${ROOTDIR}/config/*:${ROOTDIR}/lib/*"

ARGS="-Dlog4j.configurationFile=${ROOTDIR}/config/log4j2-gui.xml"
ARGS="${ARGS} -Dbqt.gui.default.paths=${ROOTDIR}/config/${DEF_PATHS}"

echo "CP=${CP}"

java -cp "${CP}" ${ARGS} org.jboss.bqt.gui.GUIClient