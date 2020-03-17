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
RUN apt-get update && \
    apt-get install -y dnsmasq 

RUN echo "\n\n# Docker extra config \nuser=root\naddn-hosts=/etc/hosts\n" >> /etc/dnsmasq.conf

COPY --from=builder /app/dist/* /usr/share/nginx/html/
COPY --from=builder /app/dl-admin-nginx.conf /etc/nginx/conf.d/default.conf

CMD echo "domain-needed" >> /etc/dnsmasq.conf && \
      echo "resolv-file=/etc/resolv.conf" >> /etc/dnsmasq.conf && \
      echo "expand-hosts" >> /etc/dnsmasq.conf && \
      echo "listen-address=127.0.0.1" >> /etc/dnsmasq.conf && \     
      service dnsmasq restart && \
      echo set \$upstreamName http://dl-feeder.`grep search /etc/resolv.conf | awk {'print $2'}`:1680/datalake/v1\$1\; > /etc/nginx/upstream.conf && \
      nginx -g "daemon off;"

#CMD ["sh", "-c", "tail -f /dev/null"]
