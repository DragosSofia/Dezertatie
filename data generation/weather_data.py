from datetime import datetime, timedelta

# Current time in UTC
now = datetime.utcnow()

lines = []
for i in range(20):
    # For each point, subtract i minutes from now
    timestamp = now - timedelta(minutes=19 - i)
    # Convert to nanoseconds
    timestamp_ns = int(timestamp.timestamp() * 1000000000)
    # Example random-ish data
    temperature = 20 + i * 0.5  # Just to vary the temperature
    humidity = 60 - i           # Humidity going down
    # Create line
    line = f"weather,location=upb temperature={temperature:.1f},humidity={humidity} {timestamp_ns}"
    lines.append(line)

# Output result
print("\n".join(lines))
