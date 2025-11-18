import subprocess
import time

try:
    while True:
        # Run the command "netstat -an | grep ESTABLISHED | wc"
        result = subprocess.run("netstat -an | grep ESTABLISHED | wc", shell=True, capture_output=True, text=True)

        # Extract the number of connections from the result
        num_connections = int(result.stdout.split()[0])
        print(f"Number of ESTABLISHED connections: {num_connections}")

        # Check if the number of connections exceeds a limit
        high_limit = 200
        if num_connections > high_limit:
            # Touch a file
            subprocess.run("touch touch_this_file.py", shell=True)
            print("Touched the file to reset FastAPI: touch_this_file.py")

        # Wait for a few seconds before running the command again
        time.sleep(10)
except KeyboardInterrupt:
    print("Script stopped by user.")
