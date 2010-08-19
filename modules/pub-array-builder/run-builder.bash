#!/bin/bash
#true_script_location=`readlink -fn $0`
#APPDIR=`dirname $true_script_location`;
APPDIR=`dirname $0`

unset CP
for i in $APPDIR/dist/lib/*.jar; do CP=$CP:$i; done

CP=$CP:$APPDIR/dist/pub-array-builder-1.0.jar

echo $CP

# Run with debugging disabled
java -Xmx1g -cp $CP org.jax.pubarray.builder.PubArrayBuilderMain
