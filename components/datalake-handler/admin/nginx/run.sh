#!/bin/sh

echo resolver `grep nameserver /etc/resolv.conf |awk {'print $2'}` valid=10s\; > /etc/nginx/resolver.conf
echo set \$upstreamName http://dl-feeder.`grep ^search /etc/resolv.conf | awk {'print $2'}`:1680/datalake/v1\$1\$is_args\$args\; > /etc/nginx/upstream.conf
nginx -g "daemon off;"
