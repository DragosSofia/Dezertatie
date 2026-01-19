from datetime import datetime, timedelta

# Current time in UTC
now = datetime.utcnow()

num_points = 1000
total_seconds = 4 * 60 * 60  # 4 hours
step_seconds = total_seconds / num_points

lines = []
for i in range(num_points):
    # Spread points evenly over the last 4 hours
    timestamp = now - timedelta(seconds=total_seconds - i * step_seconds)

    # Convert to nanoseconds
    timestamp_ns = int(timestamp.timestamp() * 1_000_000_000)

    # Example varying data
    temperature = 20 + i * 0.01      # slow increase
    humidity = 60 - i * 0.02         # slow decrease

    line = (
        f"weather,location=upb "
        f"temperature={temperature:.2f},humidity={humidity:.2f} "
        f"{timestamp_ns}"
    )
    lines.append(line)

# Output result
print("\n".join(lines))
