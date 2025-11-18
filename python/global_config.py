import os
import logging
from dotenv import load_dotenv
load_dotenv()

AI_ADVISOR_VERSION = "10.6.3"
LLM_MODEL = os.getenv("LLM_MODEL")
LOG_FILE_NAME = "aiadvisor.log"
LOG_LEVEL = logging.DEBUG
PINECONE_INDEX = os.getenv("PINECONE_INDEX_NAME")
OPENAI_API_KEY = os.getenv('OPENAI_API_KEY')
