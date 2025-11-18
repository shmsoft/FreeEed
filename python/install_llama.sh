#script to install llama3 and run it using ollama
#Download ollama and start server
curl -fsSL https://ollama.com/install.sh | sh
#Create a service file in /etc/systemd/system/ollama.service:
cd /etc/systemd/system/
sudo vim -c "%s/.*/[Unit]\nDescription=Ollama Service\nAfter=network-online.target\n\n[Service]\nExecStart=/usr/bin/ollama serve\nUser=ollama\nGroup=ollama\nRestart=always\nRestartSec=3\n\n[Install]\nWantedBy=default.target" -c "wq" ollama.service
#download and run llama3:70b
ollama run llama3:70b
