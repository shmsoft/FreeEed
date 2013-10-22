/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
/*    
    Copyright 2012 Mark Kerzner

*/ 
package org.freeeed.main;

public interface DocumentMetadataKeys {
    public static final String DOCUMENT_ORIGINAL_PATH = "document_original_path";
    public static final String DOCUMENT_TEXT = "text";
    public static final String PROCESSING_EXCEPTION = "processing_exception";
    public static final String MASTER_DUPLICATE = "master_duplicate";
    public static final String CUSTODIAN = "Custodian";
    public static final String LINK_NATIVE = "native_link";
    public static final String LINK_TEXT = "text_link";
    public static final String LINK_EXCEPTION = "exception_link";
    
    public static final String SUBJECT = "subject";
    public static final String MESSAGE_FROM = "Message-From";
    public static final String MESSAGE_DATE = "Creation-Date";
    public static final String MESSAGE_TO = "Message-To";
    public static final String MESSAGE_CC = "Message-Cc";
    
    public static final String DATE = "date";
    public static final String DATE_RECEIVED = "Date Received";
    public static final String TIME_RECEIVED = "Time Received";
    
    public static final String DATE_SENT = "Date Sent";
    public static final String TIME_SENT = "Time Sent";
    
}
