#!/bin/sh
set -e

echo "Waiting for Toxiproxy..."
until curl -s http://toxiproxy:8474/version > /dev/null; do
  sleep 1
done

echo "Creating InfluxDB proxy..."
curl -s -X POST http://toxiproxy:8474/proxies \
  -H "Content-Type: application/json" \
  -d '{
    "name": "influxdb",
    "listen": "0.0.0.0:8085",
    "upstream": "influxdb:8086"
  }'

echo "Postgres Toxiproxy proxy created on port 5433"
curl -s -X POST http://toxiproxy:8474/proxies \
  -d '{
        "name": "postgres",
        "listen": "0.0.0.0:5433",
        "upstream": "auth-postgres:5432"
      }'

echo "Adding latency for influxdb..."
curl -s -X POST http://toxiproxy:8474/proxies/influxdb/toxics \
  -H "Content-Type: application/json" \
  -d '{
    "name": "latency",
    "type": "latency",
    "stream": "downstream",
    "attributes": {
      "latency": 500,
      "jitter": 100
    }
  }'

echo "Adding latency for postgress..."
curl -X POST http://localhost:8474/proxies/postgres/toxics \
  -d '{
        "name": "latency_downstream",
        "type": "latency",
        "stream": "downstream",
        "toxicity": 1.0,
        "attributes": {"latency": 500, "jitter": 100}
      }'