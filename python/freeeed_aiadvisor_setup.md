# AIAdvisor

## Developer setup

#### Java
[Coretto 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)

#### FreeEed Processing
* Install FreeEed complete pack current version, then
```shell
cd freeeed_complete_pack
./start_dev_services.sh 
```
* Clone FreeEed
```shell
git clone git@github.com:shmsoft/FreeEed.git
```
* Run FreeEed in IntelliJ
![](images/int.png)

#### FreeEed Review

* Clone FreeEedUI
```shell
git clone git@github.com:shmsoft/FreeEedUI.git
```
* Run FreeEedUI in IntelliJ
![](images/ui.png) 

#### AIAdvisor

# Python project setup

```shell
sudo apt install libgl1-mesa-glx libegl1-mesa libxrandr2 libxrandr2 libxss1 libxcursor1 libxcomposite1 libasound2 libxi6 libxtst6
curl -O https://repo.anaconda.com/archive/Anaconda3-2024.02-1-Linux-x86_64.sh 
bash Anaconda3-2024.02-1-Linux-x86_64.sh
conda create --name AIAdvisor python=3.10
conda activate AIAdvisor
cd code/python
./requirements-install.sh
```

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
python reload_on_high_connections.py
```