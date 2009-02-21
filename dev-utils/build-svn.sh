#!/bin/bash
DIR=`dirname $0`
REV=`git svn log --oneline --limit 1 | sed -n "s/\(r[0-9]*\).*/\1/p"`
GITREV=`git svn find-rev $REV`
echo "SVNRev: $REV"
echo "GitRev: $GITREV"
git stash
git checkout $GITREV
ant -Dversion=svn-$REV
RET=$?
git checkout master
git stash apply
USER=`head -n 1 .gcpasswd`
PW=`tail -n 1 .gcpasswd`
if [[ $RET == 0 ]]; then
	echo "Compile succesful.  Uploading"
	$DIR/googlecode_upload.py --user $USER --password $PW -s streambaby-svn-$REV -p streambaby dist/streambaby-svn-$REV.zip 
else
	echo "Compile failed."
fi

