import asyncio
import os
from unittest.mock import MagicMock, patch
from main import describe_index
from fastapi import HTTPException

# Mock environment variables to avoid actual network calls or missing keys issues
os.environ['PINECONE_API_KEY'] = 'fake_key'
os.environ['PINECONE_INDEX_NAME'] = 'fake_index'

async def run_test():
    print("Running reproduction test...")
    
    # Mock Pinecone_Client to raise an exception
    with patch('main.Pinecone_Client') as mock_pinecone:
        mock_pinecone.side_effect = Exception("Simulated Pinecone Error")
        
        # Test fix behavior
        try:
            await describe_index(index_name="test_index")
            print("FAILURE: Exception was not raised.")
        except HTTPException as e:
            print(f"Caught expected exception: {e}")
            if e.status_code == 500 and "Simulated Pinecone Error" in e.detail:
                print("SUCCESS: Verified fix (HTTPException raised with correct details).")
            else:
                print(f"FAILURE: Unexpected exception details: {e}")
        except Exception as e:
            print(f"FAILURE: Caught unexpected exception type: {type(e)}")

if __name__ == "__main__":
    asyncio.run(run_test())
