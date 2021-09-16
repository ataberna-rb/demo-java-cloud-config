PROYECTO=demo-springboot
VERSION=1.0.0

source ./scripts/war.sh

docker build \
--build-arg TAG=$VERSION \
-t $PROYECTO:$VERSION .
