#!/bin/sh
# mock 10G user data, 10G item data
java -jar -Dpath=./mockdata -Dnum=167772160 pingcap-1.0-jar-with-dependencies.jar mockdata
java -jar -Duser=./mockdata/user.dat -Ditem=./mockdata/item.dat -Dout=./mockdata/out.dat -Dtemp=./bigtemp pingcap-1.0-jar-with-dependencies.jar run
