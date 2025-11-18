import assemblyai as aai
import threading
import os
from fastapi import FastAPI, UploadFile, Form
from starlette.responses import RedirectResponse
from pydantic import BaseModel
from store_document import do_store_document, do_store_documents
from fastapi import HTTPException
from pinecone import Pinecone as Pinecone_Client
from global_config import AI_ADVISOR_VERSION
import logging
import openai
from aiadvisor_pinecone_lch import clean_namespace
from langchain.vectorstores import Pinecone
from langchain.chains.question_answering import load_qa_chain
from dotenv import load_dotenv, find_dotenv
from pinecone import Pinecone as Pinecone_Client
from typing import Optional, List
from fastapi import FastAPI, Query
from langchain.llms import OpenAI
import shutil
from pathlib import Path
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import global_config
from logging import StreamHandler
from boto3 import Session
import time

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(global_config.LOG_FILE_NAME)

model = os.getenv('LLM_MODEL')
_ = load_dotenv(find_dotenv())  # read local .env file
openai.api_key = os.getenv('OPENAI_API_KEY')

if os.getenv('AWS_LOG_GROUP') is not None:
    class CloudWatchLogHandler(StreamHandler):
        def __init__(self, *args, **kwargs):
            super().__init__(*args, **kwargs)
            self.session = Session(region_name=os.getenv("AWS_DEFAULT_REGION"))
            self.cloudwatch_logs = self.session.client('logs')
            self.log_group = os.getenv("AWS_LOG_GROUP")
            self.log_stream = f"AIAdvisor_{int(time.time())}"

        def emit(self, record):
                log_message = self.format(record)
                try:
                    response = self.cloudwatch_logs.describe_log_streams(
                        logGroupName=self.log_group,
                        logStreamNamePrefix=self.log_stream)

                    log_streams = response.get('logStreams', [])
                    if not log_streams:
                        self.cloudwatch_logs.create_log_stream(
                            logGroupName=self.log_group,
                            logStreamName=self.log_stream
                        )
                except Exception as e:
                    if "ResourceNotFoundException" in str(e):
                        # Log stream doesn't exist, create it
                        self.cloudwatch_logs.create_log_stream(
                            logGroupName=self.log_group,
                            logStreamName=self.log_stream
                        )
                self.cloudwatch_logs.put_log_events(
                    logGroupName=self.log_group,
                    logStreamName=self.log_stream,
                    logEvents=[{'timestamp': int(record.created * 1000), 'message': log_message}]
                )
    logger.addHandler(CloudWatchLogHandler())
else:
    fh = logging.FileHandler(global_config.LOG_FILE_NAME)
    fh.setLevel(logging.DEBUG)
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    fh.setFormatter(formatter)
    logger.addHandler(fh)
    logger.info("Logger is configured to write to a file: " + global_config.LOG_FILE_NAME)


_ = load_dotenv(find_dotenv())  # read local .env file
openai.api_key = os.getenv('OPENAI_API_KEY')

app = FastAPI(
    title="AIAdvisor",
    description="AIAdvisor API on " + global_config.LLM_MODEL,
    version=AI_ADVISOR_VERSION
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)



class Question(BaseModel):
    question: str
    case_id: str


@app.on_event("startup")
def set_default_executor():
    from concurrent.futures import ThreadPoolExecutor
    import asyncio

    loop = asyncio.get_running_loop()
    loop.set_default_executor(
        ThreadPoolExecutor(max_workers=5)
    )

@app.get("/question_case/", summary="Ask AIAdvisor about your case")
async def ask_question(question: str, case_id: str):
    try:
        logger.info("****************ask question")
        _ = load_dotenv(find_dotenv())  # read local .env file
        openai.api_key = os.getenv('OPENAI_API_KEY')

        namespace = case_id
        logger.info("namespace=" + namespace)
        # initialize pinecone
        pc = Pinecone_Client(api_key=os.getenv('PINECONE_API_KEY'))
        logger.info("pinecone.init OK")
        from langchain.embeddings.openai import OpenAIEmbeddings
        embeddings = OpenAIEmbeddings(openai_api_key=openai.api_key)

        def get_similar_docs(query, namespace, num_sources=10, score=False):
            index = Pinecone.from_existing_index(global_config.PINECONE_INDEX, embeddings, namespace=namespace)
            if score:
                similar_docs = index.similarity_search_with_score(query, k=num_sources, namespace=namespace)
            else:
                similar_docs = index.similarity_search(query, k=num_sources, namespace=namespace)
            logger.info(str(len(similar_docs)) + " similar docs found")
            return similar_docs

        def get_answer(query, namespace):
            docs = []
            from langchain.llms import OpenAI
            llm = OpenAI(model_name=model)
            chain = load_qa_chain(llm, chain_type="stuff")
            similar_docs_list = get_similar_docs(query, namespace=namespace)
            if isinstance(similar_docs_list, tuple):
                docs.extend([t[0] for t in similar_docs_list])
            else:
                docs.extend(similar_docs_list)

            sources = []
            for element in docs:
                source = get_source(element.page_content)
                logger.info(f"Source: {source}")
                sources.append(source)

            sources = list(set(sources))
            return (chain.run(input_documents=similar_docs_list, question=query), sources)

        my_query = str(question)
        logger.info("my_query=" + my_query)
        answer = get_answer(my_query, namespace)
        print(answer)

        logger.info("A: " + str(answer))

    except Exception as e:
        logger.exception(e)
        raise HTTPException(status_code=500, detail="An error occurred while processing the request.")
    return {"question": question, "answer": answer[0], "sources": answer[1]}


@app.get("/question_cases/", summary="Ask AIAdvisor about your multiple cases")
async def ask_question(question: str, case_ids: list[str] = Query(...)):
    def get_similar_docs(query, namespace, num_sources=10, score=False):
        pc = Pinecone_Client(api_key=os.getenv('PINECONE_API_KEY'))
        index = Pinecone.from_existing_index(global_config.PINECONE_INDEX, embeddings, namespace=namespace)
        if score:
            similar_docs = index.similarity_search_with_score(query, k=num_sources, namespace=namespace)
        else:
            similar_docs = index.similarity_search(query, k=num_sources, namespace=namespace)
        logger.info(str(len(similar_docs)) + " similar docs found")
        logger.info(type(similar_docs))
        return similar_docs

    def get_answer(query, namespace):
        combined_list = []
        docs = []
        for case_id in case_ids:
            namespace = case_id
            logger.info("namespace=" + namespace)
            llm = OpenAI(model_name=model)
            chain = load_qa_chain(llm, chain_type="stuff")
            similar_docs_list = get_similar_docs(query, namespace=namespace)
            combined_list.extend(similar_docs_list)
            if isinstance(similar_docs_list, tuple):
                docs.extend([t[0] for t in similar_docs_list])
            else:
                docs.extend(similar_docs_list)
        sources = []
        for element in docs:
            source = get_source(element.page_content)
            logger.info(f"Source: {source}")
            sources.append(source)
        sources = list(set(sources))
        return (chain.run(input_documents=combined_list, question=query), sources)

    try:
        logger.info("********** ask question about cases " + str(case_ids))
        _ = load_dotenv(find_dotenv())  # read local .env file
        openai.api_key = os.getenv('OPENAI_API_KEY')
        for case_id in case_ids:
            namespace = case_id
            logger.info("namespace=" + namespace)
            # initialize pinecone
            pc = Pinecone_Client(api_key=os.getenv('PINECONE_API_KEY'))
            logger.info("pinecone.init OK")
            from langchain.embeddings.openai import OpenAIEmbeddings
            embeddings = OpenAIEmbeddings(openai_api_key=openai.api_key)
            my_query = str(question)
            logger.info("my_query=" + my_query)
            answer = get_answer(my_query, namespace)
            print(answer[0])

            logger.info("A: " + answer[0])

    except Exception as e:
        logger.exception(e)
        raise HTTPException(status_code=500, detail="An error occurred while processing the request.")
    return {"question": question, "answer": answer[0], "sources": answer[1]}


@app.post("/clean_case_index/", status_code=201, summary="Clean up an index for a case")
async def clean_case_index(case_id: str):
    logger.info("Prepare index for case_id: " + case_id)
    if (namespace_exists(case_id)):
        clean_namespace(case_id)
    return {"message": "Index prepared successfully"}


def namespace_exists(namespace):
    index_name = global_config.PINECONE_INDEX
    pc = Pinecone_Client(api_key=os.getenv('PINECONE_API_KEY'))
    index = pc.Index(index_name)
    namespaces = index.describe_index_stats()['namespaces']
    return namespace in namespaces

@app.post("/store_document/", status_code=201, summary="Store a document for a case")
async def store_document(case_id: str = Form(...), document: UploadFile = Form(...), document_id: int = Form(...)):
    try:
        logger.info("****************store document")
        if document_id is None or document_id == 0:
            source = document.filename
        else:
            source = str(document_id)

        logger.info(f"File Name: {source}")
        logger.info(f"Start: {threading.active_count()}")
        content = await document.read()  # Read the file content
        content = content.decode()  # If the file is a text file, convert bytes to string
        doc_length = len(content)
        logger.info("Store a document of length: " + str(doc_length) + " for case: " + str(case_id))
        logger.info(f"Before Store Documents: {threading.active_count()}")
        number_splits = do_store_document(content, case_id, source)
        return {"message": "Document stored successfully", "Number of splits": str(number_splits)}
    except Exception as e:
        logger.exception(e)
        raise HTTPException(status_code=500, detail="An error occurred while processing the request.")


@app.post("/store_content/", status_code=201, summary="Store document content for a case")
async def store_content(case_id: str = Form(...), content: str = Form(...), source: str = Form(...)):
    try:
        doc_length = len(content)
        logger.info("Store content of length: " + str(doc_length) + " for case: " + str(case_id))
        logger.info(f"Before Store Documents: {threading.active_count()}")
        number_splits = do_store_document(content, case_id, source)
        logger.info(f"After Store Documents: {threading.active_count()}")
        logger.info(f"Number of splits: {str(number_splits)}")
        return {"message": "Content stored successfully", "Number of splits": str(number_splits)}
        
    except Exception as e:
        logger.exception(e)
        raise HTTPException(status_code=500, detail="An error occurred while processing the request.")


@app.post("/store_contents/", status_code=201, summary="Store document content for a case")
async def store_contents(case_id: str = Form(...), contents: List[str] = Form(...), sources: List[str] = Form(...)):
    responses = []
    requests = []
    
    for document, document_id in zip(contents, sources):
        try:
            logger.info("****************store contents "+document)
            source = document.filename if document_id is None or document_id == 0 else str(document_id)
            logger.info(f"File Name: {source}")
            logger.info(f"Start: {threading.active_count()}")
            doc_length = len(document)
            logger.info("Store a document of length: " + str(doc_length) + " for case: " + str(case_id))
            logger.info(f"Before Store Documents: {threading.active_count()}")
            content_filename_map = {'content' : document, 'filename' : source}
            requests.append(content_filename_map)
        except Exception as e:
            logger.exception(e)
            raise HTTPException(status_code=500, detail=f"An error occurred while processing the request for case ID: {case_id}.")
    number_splits = do_store_documents(case_id, requests)
    responses.append({"message": "Documents stored successfully", "Number of splits": str(number_splits)})
    return responses


@app.get("/describe_index/", summary="Describe full Pinecone index (may take a long time)")
async def describe_index(index_name: Optional[str] = None):
    try:
        if index_name is None:
            index_name = global_config.PINECONE_INDEX
        logger.info("Describing index: " + index_name)
        pc = Pinecone_Client(api_key=os.getenv('PINECONE_API_KEY'))
        index = pc.Index(index_name)
        index_stats_response = index.describe_index_stats()
        one_string = str(index_stats_response)
        return {"index_stats": one_string}
    except Exception as e:
        logger.exception(e)


@app.post("/transcribe_audio/", status_code=200, summary="Transcribe an audio file")
async def transcribe(document: UploadFile = Form(...)):
    # Load API key from .env file
    _ = load_dotenv(find_dotenv())
    aai.settings.api_key = os.getenv('ASSEMBLY_AI_KEY')

    # Ensure API key is available
    if aai.settings.api_key is None:
        raise HTTPException(status_code=500, detail="API key not found")

    transcriber = aai.Transcriber()

    # Create a temporary directory to save the file
    temp_dir = Path("./temp")
    temp_dir.mkdir(exist_ok=True)
    temp_file = temp_dir / document.filename

    # Save the uploaded file to a temporary file
    with temp_file.open("wb") as buffer:
        shutil.copyfileobj(document.file, buffer)

    # Transcribe the audio file
    try:
        transcript = transcriber.transcribe(str(temp_file))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        # Cleanup: remove the temporary file
        temp_file.unlink()

    return JSONResponse(content={"transcription": transcript.text})
    # return transcript.text


@app.get("/")
async def read_root():
    logger.info("Hello! Redirecting to /doc")
    return RedirectResponse(url='/docs')


def get_source(text):
    tag = "]]"
    end_index = text.find(tag)
    if end_index != -1:
        result = text[0:end_index]
        result = result.replace('[[Source: ', '')
        return result
    return ""