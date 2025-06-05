#!/bin/bash
# run-k6-test.sh

echo "Starting K6 performance test..."

# InfluxDB로 결과 전송하는 K6 실행
docker run --rm -i \
  --network host \
  -v "$(pwd)":/app \
  -w /app \
  grafana/k6:latest run \
  --out influxdb=http://localhost:8086/k6 \
  --summary-trend-stats="avg,min,med,max,p(90),p(95),p(99)" \
  k6-test.js

echo "K6 test completed. Check Grafana dashboard for results."