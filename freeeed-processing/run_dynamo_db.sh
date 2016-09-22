#!/bin/sh

java -Djava.library.path=lib/DynamoDBLocal_lib -jar lib/DynamoDBLocal.jar -sharedDb
