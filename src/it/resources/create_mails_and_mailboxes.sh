#!/usr/bin/env bash

java -jar /root/james-cli.jar -h 127.0.0.1 -p 9999 CreateMailbox '#private' bart@simpson.cartoon rmbx0
java -jar /root/james-cli.jar -h 127.0.0.1 -p 9999 CreateMailbox '#private' bart@simpson.cartoon rmbx0.smbx0

java -jar /root/james-cli.jar -h 127.0.0.1 -p 9999 ImportEml '#private' 'bart@simpson.cartoon' rmbx0  /message.eml
java -jar /root/james-cli.jar -h 127.0.0.1 -p 9999 ImportEml '#private' 'bart@simpson.cartoon' rmbx0.smbx0  /message.eml