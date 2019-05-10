FROM node:alpine AS builder
MAINTAINER Ekko Chang <ekko.chang@qct.io>

WORKDIR /app
COPY ./src .
COPY ./nginx .

# Data feeder host name for proxy config
ENV DL_FEEDER_HOST="http://192.168.1.97:1680"
RUN sed -i "s@DL_FEEDER_HOST@$DL_FEEDER_HOST@g" ./proxy.conf.json
RUN npm install && \
    npm run build


FROM nginx:alpine
COPY --from=builder /app/dist/* /usr/share/nginx/html/
COPY --from=builder /app/dl-admin-nginx.template /etc/nginx/conf.d/

# Data feeder host name for nginx config
ENV DL_FEEDER_HOST="http://192.168.1.97:1680"
RUN envsubst '${DL_FEEDER_HOST}' < /etc/nginx/conf.d/dl-admin-nginx.template > /etc/nginx/conf.d/default.conf
