import subprocess
import time

try:
    while True:
        # Run the command "netstat -an | grep ESTABLISHED | wc"
        result = subprocess.run("netstat -an | grep ESTABLISHED | wc", shell=True, capture_output=True, text=True)
        print(result.stdout)

        # Wait for a few seconds before running the command again
        time.sleep(3)
except KeyboardInterrupt:
    print("Script stopped by user.")
