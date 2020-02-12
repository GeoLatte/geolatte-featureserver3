## sources environment

PORT=8080
HOST=localhost

export BASEURL=http://$HOST:$PORT/api


if [ -z "$VERSION" ]
then
  export VERSION=v1
fi

