python -m venv myenv
source myenv/bin/activate
pip install ipykernel 
python -m ipykernel install --user --name=myenv --display-name "My env"
pip install -r requirements.txt


