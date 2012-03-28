#!/bin/bash -x
# android list target to get target version
[ -d src ] && ACTION=update || { ACTION=create;OPTS="-k com.oux.$(basename $(pwd)) -a $(basename $(pwd))"; }
android $ACTION project --name $(basename $(pwd)) --target android-10 --path $(pwd) $OPTS
