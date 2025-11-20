"""
This module provides functionality for
    finding similar documents in Pinecone index
    uses Langchain
    get ChatGPT answer from these documents

Functions:
-----------
    get_answer(query): gets an answer from ChatGPT
"""
import os
import openai
from dotenv import load_dotenv, find_dotenv
from pinecone import Pinecone
from langchain_pinecone import PineconeVectorStore
from langchain_openai import OpenAIEmbeddings
from langchain_text_splitters import RecursiveCharacterTextSplitter
import global_config
import logging


logger = logging.getLogger(global_config.LOG_FILE_NAME)

_ = load_dotenv(find_dotenv())  # read local .env file
openai.api_key = os.getenv('OPENAI_API_KEY')
MODEL = "gpt-4"
index_name = os.getenv('PINECONE_INDEX_NAME')

# initialize pinecone
pc = Pinecone(api_key=os.getenv('PINECONE_API_KEY'))

def clean_namespace(namespace: str):
    index_pc = pc.Index(os.getenv('PINECONE_INDEX_NAME'))
    index_pc.delete(deleteAll=True, namespace=namespace)


