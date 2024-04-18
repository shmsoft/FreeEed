#!/bin/bash

curl -v -T "$SHMSOFT_HOME/FreeEed/freeeed-processing/test-data/07-ocr/aluminum.pdf" \
    http://localhost:9998/tika \
    -H 'Content-type: application/pdf' \
    -H 'X-Parsed-By: org.apache.tika.parser.DefaultParser' \
    -H 'Accept: text/plain' \
    -H 'X-Tika-PDFOcrStrategy: ocr_and_text_extraction' \
    --output response.csv