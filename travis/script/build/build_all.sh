#!/bin/bash -e

[ ! -z "$DEBUG_BASH" ] && set -x

# Move into Docker env if done
apt-get update && apt-get --assume-yes install gradle

$SCRIPT_DIR/build/build_eclipse.sh
$SCRIPT_DIR/build/build_intellij.sh
# following build steps are conditional
$SCRIPT_DIR/build/build_server.sh
$SCRIPT_DIR/build/build_netbeans.sh
$SCRIPT_DIR/build/build_frontend.sh
