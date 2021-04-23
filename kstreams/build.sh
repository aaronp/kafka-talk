#!/usr/bin/env bash

# only requires docker to build/run
docker run --rm \
  -v $HOME/.sbt:/root/.sbt.sbt \
  -v $HOME/.cache:/root/.cache \
  -v $HOME/.m2:/root/.m2 \
  -v $HOME/.ivy2:/root/.ivy2 \
  -v $PWD:/root/app \
  hseeberger/scala-sbt:8u222_1.3.5_2.13.1 cd /root/app & pwd $ echo "testing!" & sbt run