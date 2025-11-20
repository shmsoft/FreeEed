import logging
import os
import openai
from langchain_text_splitters import CharacterTextSplitter
from langchain_openai import OpenAIEmbeddings
from langchain_pinecone import PineconeVectorStore
from pinecone import Pinecone as Pinecone_Client
from dotenv import load_dotenv, find_dotenv
from typing import List, Dict
import global_config

logger = logging.getLogger(global_config.LOG_FILE_NAME)

num_splits = 0


def split_texts(texts, chunk_size=10, chunk_overlap=10):
    logger.info("split_docs with " + str(chunk_size) + " and " + str(chunk_overlap))
    # text_splitter = RecursiveCharacterTextSplitter(chunk_size=chunk_size, chunk_overlap=chunk_overlap)
    text_splitter = CharacterTextSplitter(chunk_size=chunk_size, chunk_overlap=chunk_overlap)
    docs = text_splitter.split_text(texts)
    global num_splits
    num_splits = len(docs)
    return docs


def find_long_strings(documents, max_length=40000):
    """Find strings in the array longer than a specified length."""
    return [s for s in documents if len(s) > max_length]


def split_string(input_string, chunk_size=20000):
    """Split a string into chunks of a specified size."""
    return [input_string[i:i + chunk_size] for i in range(0, len(input_string), chunk_size)]


def filter_strings(documents, max_length=40000):
    """Filter strings from the array based on length."""
    return [s for s in documents if len(s) <= max_length]


def do_store_document(content: str, namespace: str, filename: str):
    logger.info("do_store_document ")
    logger.info("namespace: " + namespace)
    logger.info("index_name = " + str(global_config.PINECONE_INDEX))
    #temp_folder = tempfile.gettempdir()
    #temp_file = os.path.join(temp_folder, "temp.txt")
    #with open(temp_file, 'w') as f:
    #    f.write(content)
    #    f.close()

    _ = load_dotenv(find_dotenv())  # read local .env file
    openai.api_key = os.getenv('OPENAI_API_KEY')
    #loader = TextLoader(temp_file)
    #documents = loader.load()
    docs = split_texts(content)
    long_strings = find_long_strings(docs)
    docs = filter_strings(docs)
    logger.info(f"Long strings {str(len(long_strings))}")

    for value in long_strings:
        splitted_strings = split_string(value)
        for txt in splitted_strings:
            docs.append(txt)

    source = f"[[Source: {filename}]]"
    logger.info(f"Source: {source}")

    for idx, document in enumerate(docs):
        docs[idx] = f"{source} {document}"

    logger.info("Document split into " + str(len(docs)) + " paragraphs completed")
    embeddings = OpenAIEmbeddings(openai_api_key=openai.api_key)

    pc = Pinecone_Client(api_key=os.getenv('PINECONE_API_KEY'))

    PineconeVectorStore.from_texts(docs, embeddings, index_name=global_config.PINECONE_INDEX, namespace=namespace)
    #Pinecone.from_documents(docs, embeddings, index_name=global_config.PINECONE_INDEX, namespace=namespace, pool_threads=2)


    logger.info("Stored in index " + global_config.PINECONE_INDEX + " namespace " + namespace)
    #os.remove(temp_file)
    return str(len(docs))


def do_store_documents(namespace: str, documents: List[Dict[str, str]]):
    embeddings = OpenAIEmbeddings(openai_api_key=os.getenv('OPENAI_API_KEY'))
    all_docs = []
    for document in documents:
        content, filename = document['content'], document['filename']

        docs = split_texts(content)
        long_strings = find_long_strings(docs)
        docs = filter_strings(docs)

        for value in long_strings:
            splitted_strings = split_string(value)
            docs += splitted_strings

        source = f"[[Source: {filename}]]"
        for idx, doc in enumerate(docs):
            docs[idx] = f"{source} {doc}"

        # Collect all docs for batch processing
        all_docs += docs

    # Generate embeddings in a batch for all_docs
    pc = Pinecone_Client(api_key=os.getenv('PINECONE_API_KEY'))
    PineconeVectorStore.from_texts(all_docs, embeddings, index_name=global_config.PINECONE_INDEX, namespace=namespace)

    logger.info(f"Stored {len(all_docs)} documents in index {global_config.PINECONE_INDEX}")
    return len(all_docs)
