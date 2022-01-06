#!/bin/bash

export JMIX_UI_PRODUCTIONMODE="true"
export JMIX_UI_LOGIN_DEFAULTUSERNAME="<disabled>"
export JMIX_UI_LOGIN_DEFAULTPASSWORD="<disabled>"

java -jar akkount-0.4-SNAPSHOT.jar & echo $! > ./pid.file &
