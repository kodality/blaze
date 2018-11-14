cd `dirname $0`
this=`pwd`
deploy=$this/deploy
etc=$this/etc

mkdir $deploy
mkdir $etc

cd ../../..
cp -r etc/conf/* $etc/

cp fhir-stu3/fhir-stu3.jar $deploy
find ./*/target/ -name "*SNAPSHOT.jar" -exec rm {} \;
mvn clean install -DskipTests
find ./*/target/ -name "*SNAPSHOT.jar" -exec cp {} $deploy/ \;
rm $deploy/*blockchain*

cd $this
docker build -t kodality/blaze .
rm -rf $deploy/*
rm -rf $etc/*
