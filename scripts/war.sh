docker run -it --rm \
-v "$HOME"/.m2:/root/.m2 \
-v $(pwd):/var/sources/ \
-w /var/sources/ \
maven:3.8.2-jdk-11 mvn clean install \
-f /var/sources/ \
-Dmaven.test.skip=true

