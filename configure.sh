#!/usr/bin/env bash
#
# Find SQLCL in the path
#
# Grab home and version info
#

SQL=`which sql`
SQLCL_BIN=`dirname ${SQL}`
SQLCL_HOME=`dirname $SQLCL_BIN`
SQLCL_VERSION=`$SQLCL_BIN/sql -v | grep -o -E '[0-9.]+' `


echo Using SQLCL_BIN as ${SQLCL_BIN}
echo Using SQLCL_HOME as ${SQLCL_HOME}
echo Using SQLCL_VERSION  as ${SQLCL_VERSION}

#
## Install sqlcl jars into the local maven
#
mvn install:install-file -Dfile=${SQLCL_BIN}/../lib/dbtools-common.jar  -DgroupId=oracle.dbtools -DartifactId=dbtools-common -Dversion=${SQLCL_VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=${SQLCL_BIN}/../lib/dbtools-sqlcl.jar	-DgroupId=oracle.dbtools -DartifactId=dbtools-sqlcl  -Dversion=${SQLCL_VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=${SQLCL_BIN}/../lib/dbtools-http.jar	-DgroupId=oracle.dbtools -DartifactId=dbtools-http   -Dversion=${SQLCL_VERSION} -Dpackaging=jar

#
# Create property file for maven to read
#

echo "sqlcl.bin=$SQLCL_BIN"         > sqlcl.properties
echo "sqlcl.home=$SQLCL_HOME"       >> sqlcl.properties
echo "sqlcl.version=$SQLCL_VERSION" >> sqlcl.properties
