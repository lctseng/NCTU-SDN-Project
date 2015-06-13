#!/bin/sh -e
curl -d {\"target_ip\":\"$1\"} -s http://localhost:8080/wm/nctuforward/target_ip/json
