#script to extract data from text file using llama3:70b

#!/bin/bash
if [ "$#" -lt 2 ]; then
    echo "Please provide the file path as an argument."
    exit 1
fi

# Extract the file path argument
file_path="$1"
question="$2"

# Check if the file exists
if [ ! -f "$file_path" ]; then
    echo "File not found: $file_path"
    exit 1
fi

# Read content from the file
content=$(cat "$file_path")

# Sanitize content for JSON
sanitized_content=$(printf '%s' "$content" | sed 's/\\/\\\\/g' | sed 's/"/\\"/g' | tr -d '\n')


# Define the request bod
request_body=$(cat <<EOF
{
    "model": "llama3:70b",
    "messages": [
        {
            "role": "system",
            "content": "$sanitized_content"
        },
        {
            "role": "user",
            "content": "$question"
        }
    ],
    "stream": false
}
EOF
)


echo "Request Body:"
echo "$request_body"
# Perform the API call
response=$(curl --location --request POST 'http://localhost:11434/api/chat' \
--header 'Content-Type: application/json' \
--data-raw "$request_body")

# Extract the "content" field using jq
result=$(echo "$response" | jq -r '.message.content')
echo "Response:"
echo "$result"