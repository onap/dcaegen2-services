FROM node:alpine AS builder
MAINTAINER Ekko Chang <ekko.chang@qct.io>

LABEL org.label-schema.docker.build="docker build -t dl-admin . --no-cache" \
      org.label-schema.docker.run="docker run -p 80:80 --add-host=dl_feeder:your_feeder_ipaddress dl-admin"

WORKDIR /app
COPY ./src .
COPY ./nginx .
RUN npm install && \
    npm run build


FROM nginx:1.17.9

RUN groupadd -r datalake && useradd -r -g datalake datalake

COPY --from=builder /app/dist/* /usr/share/nginx/html/
COPY --from=builder /app/dl-admin-nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/run.sh /tmp

RUN chown -R datalake:datalake /etc/nginx
RUN chown -R datalake:datalake /var/cache/nginx
RUN chown -R datalake:datalake /tmp/run.sh
RUN chmod 0744 /tmp/run.sh


USER datalake

CMD ["sh", "-c", "/tmp/run.sh"]

