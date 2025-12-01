import os
import time
from dotenv import load_dotenv, find_dotenv
from pinecone import Pinecone, ServerlessSpec

def create_index():
    # Load environment variables
    _ = load_dotenv(find_dotenv())
    
    api_key = os.getenv('PINECONE_API_KEY')
    index_name = os.getenv('PINECONE_INDEX_NAME')
    
    if not api_key:
        print("Error: PINECONE_API_KEY not found in environment variables.")
        return
    
    if not index_name:
        print("Error: PINECONE_INDEX_NAME not found in environment variables.")
        return

    print(f"Initializing Pinecone with index name: {index_name}")
    pc = Pinecone(api_key=api_key)

    existing_indexes = [index.name for index in pc.list_indexes()]
    
    if index_name in existing_indexes:
        print(f"Index '{index_name}' already exists.")
    else:
        print(f"Creating index '{index_name}'...")
        try:
            pc.create_index(
                name=index_name,
                dimension=1536, # OpenAI embeddings dimension
                metric='cosine',
                spec=ServerlessSpec(
                    cloud='aws',
                    region='us-east-1'
                )
            )
            print(f"Index '{index_name}' created successfully.")
            
            # Wait for index to be ready
            while not pc.describe_index(index_name).status['ready']:
                time.sleep(1)
            print(f"Index '{index_name}' is ready.")
            
        except Exception as e:
            print(f"Failed to create index: {e}")

if __name__ == "__main__":
    create_index()
