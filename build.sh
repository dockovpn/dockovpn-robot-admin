#!/usr/bin/env bash

rm -R target

./bump.sh "$@"

version=$(cat VERSION)
ver="v$version"

sbt ';build' && \
docker build -t alekslitvinenk/dockovpn-robot-admin:"$ver" -t alekslitvinenk/dockovpn-robot-admin:latest . --no-cache && \
docker push alekslitvinenk/dockovpn-robot-admin:"$ver"