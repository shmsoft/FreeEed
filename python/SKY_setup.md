# AIAdvisor

## Developer setup

#### Install RDP on the VM
* [MS Instruction](https://learn.microsoft.com/en-us/azure/virtual-machines/linux/use-remote-desktop?tabs=azure-cli)

#### Java
* [Coretto 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/generic-linux-install.html)

#### FreeEed and AIAdvisor

* On the Desktop
* Download and unzip latest freeeed_complete_package.zip
```shell
sudo apt update  
sudo apt install pst-utils  
sudo apt install wkhtmltopdf   
sudo apt-get install tesseract-ocr    
sudo apt install libreoffice  
```

In the home directory, install `run_on_startup.sh` with the following content
```shell
cd /home/azureuser/Desktop/freeeed_complete_pack/
./start_all.sh
```

#### AIAdvisor

# Python project setup

```shell
sudo apt install libgl1-mesa-glx libegl1-mesa libxrandr2 libxrandr2 libxss1 libxcursor1 libxcomposite1 libasound2 libxi6 libxtst6
curl -O https://repo.anaconda.com/archive/Anaconda3-2024.02-1-Linux-x86_64.sh 
bash Anaconda3-2024.02-1-Linux-x86_64.sh
```

* Get the AIAdvisor code
  * Zip up AIAdvisor
  * Put the zip file here: `https://shmsoft.s3.amazonaws.com/AIAdvisor.zip`
  * Unzip it on the SKY VM

```shell```
cd code/python
./requirements-install.sh
```

```shell```
pip install openai==0.28
``` 

* Start AIAdvisor on localhost
* Review on the VM IP, and AIAdvisor on the VM IP
