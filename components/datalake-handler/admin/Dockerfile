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

RUN chown -R datalake:datalake /etc/nginx
RUN chown -R datalake:datalake /var/cache/nginx


USER datalake

#CMD echo "domain-needed" >> /etc/dnsmasq.conf && \
#      echo "resolv-file=/etc/resolv.conf" >> /etc/dnsmasq.conf && \
#      echo "expand-hosts" >> /etc/dnsmasq.conf && \
#      echo "listen-address=127.0.0.1" >> /etc/dnsmasq.conf && \     
#      service dnsmasq restart && \
#      echo set \$upstreamName http://dl-feeder.`grep search /etc/resolv.conf | awk {'print $2'}`:1680/datalake/v1\$1\$is_args\$args\; > /etc/nginx/upstream.conf && \
#      nginx -g "daemon off;"

CMD echo resolver `grep nameserver /etc/resolv.conf |awk {'print $2'}` valid=10s\; > /etc/nginx/resolver.conf && \
    echo set \$upstreamName http://dl-feeder.`grep search /etc/resolv.conf | awk {'print $2'}`:1680/datalake/v1\$1\$is_args\$args\; > /etc/nginx/upstream.conf && \
    nginx -g "daemon off;"

#CMD ["sh", "-c", "tail -f /dev/null"]
