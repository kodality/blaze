img=kodality/blaze
cd `dirname $0`
this=`pwd`
deploy=$this/deploy
etc=$this/etc

mkdir $deploy
mkdir $etc

cd ../../..
cp -r etc/conf/* $etc/

find ./*/target/ -name "*SNAPSHOT.jar" -exec rm {} \;
mvn clean install -DskipTests || exit 1
find ./*/target/ -name "*SNAPSHOT.jar" -exec cp {} $deploy/ \;
ls $deploy


cd $this
docker build -t $img .
rm -rf $deploy/*
rm -rf $etc/*
