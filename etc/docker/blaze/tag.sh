img=kodality/blaze
tag=$(date +%Y%m%d)
docker tag $img $img:$tag
docker push $img:$tag
