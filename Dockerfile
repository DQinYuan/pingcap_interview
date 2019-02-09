# Builder container
FROM openjdk:8-jre-alpine AS builder

RUN mkdir -p /root/workspace

WORKDIR /root/workspace

COPY ./target/pingcap-1.0-jar-with-dependencies.jar ./

COPY ./docker-entrypoint.sh ./

RUN chmod 777 ./docker-entrypoint.sh

ENTRYPOINT ["./docker-entrypoint.sh"]

# --cpus 4    CPU core nums
# -m 4g     limit memory
