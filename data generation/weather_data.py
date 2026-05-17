from datetime import datetime, timedelta, timezone

from influxdb_client import InfluxDBClient
from influxdb_client.client.write_api import SYNCHRONOUS

INFLUX_URL = "http://localhost:8086"
INFLUX_TOKEN = "9e1e47d8e60f13ca12cccb05537e88c84aaa36d9d193ad2858fbb7daa06af7d2"
INFLUX_ORG = "org"
INFLUX_BUCKET = "measurements"

now = datetime.now(timezone.utc)
start_of_today = now.replace(hour=0, minute=0, second=0, microsecond=0)

num_points = 1000
total_seconds = (now - start_of_today).total_seconds()
step_seconds = total_seconds / num_points

lines = []
for i in range(num_points):
    timestamp = start_of_today + timedelta(seconds=i * step_seconds)
    timestamp_ns = int(timestamp.timestamp() * 1_000_000_000)

    temperature = 20 + i * 0.01
    humidity = 60 - i * 0.02

    lines.append(
        f"weather,location=upb "
        f"temperature={temperature:.2f},humidity={humidity:.2f} "
        f"{timestamp_ns}"
    )

with InfluxDBClient(url=INFLUX_URL, token=INFLUX_TOKEN, org=INFLUX_ORG) as client:
    with client.write_api(write_options=SYNCHRONOUS) as write_api:
        write_api.write(bucket=INFLUX_BUCKET, record=lines)

print(f"Wrote {len(lines)} points to bucket '{INFLUX_BUCKET}' (measurement 'weather').")
