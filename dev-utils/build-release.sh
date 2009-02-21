#!/bin/bash
DIR=`dirname $0`
ant release
RET=$?
git checkout master
git stash apply
USER=`head -n 1 .gcpasswd`
PW=`tail -n 1 .gcpasswd`
FN=`ls dist/*.zip | sed -n "s/.*\/\(.*\)\.zip/\1/p"`
if [[ $RET == 0 ]]; then
	echo "Compile succesful.  Uploading"
	$DIR/googlecode_upload.py --user $USER --password $PW -s $FN -p streambaby --labels Featured dist/$FN.zip 
else
	echo "Compile failed."
fi

