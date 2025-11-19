# AIAdvisor Python project setup

* Is it needed?
```shell
sudo apt install libgl1-mesa-glx libegl1-mesa libxrandr2 libxrandr2 libxss1 libxcursor1 libxcomposite1 libasound2 libxi6 libxtst6
```

* If you do not have conda
```shell
curl -O https://repo.anaconda.com/archive/Anaconda3-2024.02-1-Linux-x86_64.sh 
bash Anaconda3-2024.02-1-Linux-x86_64.sh
```

* Create venv for AIAdvisor
```shell
conda create --name AIAdvisor python=3.10
conda activate AIAdvisor
cd code/python
./requirements-install.sh
```

* Is it needed?
```shell
sudo vi /etc/systemd/system.conf 
Then
DefaultLimitNOFILE=65535:524288
Reboot
To verify, run ulimit -Sn
```

## FreeEed and AIAdvisor release
* Everything is baked into  VM as above
* start_all.sh
* AIAdvisor
```shell
cd projects/AIAdvisor/code/python
conda activate AIAdvisor
./run_fastapi.sh

```

* Start AIAdvisor on localhost
* Review on the VM IP, and AIAdvisor on the VM IP

```shell
cd projects/AIAdvisor/code/python
conda activate AIAdvisor
```