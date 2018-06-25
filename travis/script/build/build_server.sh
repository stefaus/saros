#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

cd $WORKSPACE
. $SCRIPT_DIR/build/build_utils.sh

set_jdk "1.8"

gradle :de.fu_berlin.inf.dpp.server:build

