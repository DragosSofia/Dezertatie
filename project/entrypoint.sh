#!/bin/bash
set -eu -o pipefail

# Export environment variables
export DOCKER_INFLUXDB_INIT_MODE=$DOCKER_INFLUXDB_INIT_MODE
export DOCKER_INFLUXDB_INIT_USERNAME=$DOCKER_INFLUXDB_INIT_USERNAME
export DOCKER_INFLUXDB_INIT_PASSWORD=$DOCKER_INFLUXDB_INIT_PASSWORD
export DOCKER_INFLUXDB_INIT_ORG=$DOCKER_INFLUXDB_INIT_ORG
export DOCKER_INFLUXDB_INIT_BUCKET=$DOCKER_INFLUXDB_INIT_BUCKET
export DOCKER_INFLUXDB_INIT_RETENTION=$DOCKER_INFLUXDB_INIT_RETENTION
export DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=$DOCKER_INFLUXDB_INIT_ADMIN_TOKEN
export DOCKER_INFLUXDB_INIT_PORT=$DOCKER_INFLUXDB_INIT_PORT
export DOCKER_INFLUXDB_INIT_HOST=$DOCKER_INFLUXDB_INIT_HOST

# Start the InfluxDB daemon in background
influxd --prometheus-enabled=true --prometheus-bind-address=:8089 &

# Wait for the daemon to be ready
echo "Waiting for InfluxDB to start..."
until curl -s http://localhost:8086/health | grep "pass"; do
  sleep 1
done
echo "InfluxDB is up"

# Run setup only once
if [ ! -f /var/lib/influxdb2/.setup_done ]; then
  echo "Running InfluxDB initial setup..."
  influx setup --skip-verify \
    --bucket "${DOCKER_INFLUXDB_INIT_BUCKET}" \
    --retention "${DOCKER_INFLUXDB_INIT_RETENTION}" \
    --token "${DOCKER_INFLUXDB_INIT_ADMIN_TOKEN}" \
    --org "${DOCKER_INFLUXDB_INIT_ORG}" \
    --username "${DOCKER_INFLUXDB_INIT_USERNAME}" \
    --password "${DOCKER_INFLUXDB_INIT_PASSWORD}" \
    --host "http://localhost:8086" \
    --force
  touch /var/lib/influxdb2/.setup_done
fi

# Keep the container running by waiting for the daemon
wait
