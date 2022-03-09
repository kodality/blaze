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
#find ./*/target/ -name "*SNAPSHOT.jar" -exec cp {} $deploy/ \;

mkdir -p $deploy/com/kodality/blaze
cp -a ~/.m2/repository/com/kodality/blaze $deploy/com/kodality

ls $deploy


cd $this
docker build --no-cache -t $img:latest .
rm -rf $deploy/*
rm -rf $etc/*
