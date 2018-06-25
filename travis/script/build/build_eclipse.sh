#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

cd $WORKSPACE
. $SCRIPT_DIR/build/build_utils.sh

set_jdk "1.8"

gradle -PeclipsePluginDir=$ECLIPSE_HOME/plugins :de.fu_berlin.inf.dpp.core:test :de.fu_berlin.inf.dpp.ui:test :de.fu_berlin.inf.dpp:test :de.fu_berlin.inf.dpp:jar
gradle :de.fu_berlin.inf.dpp.whiteboard:test :de.fu_berlin.inf.dpp.whiteboard:jar
