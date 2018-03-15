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
package org.freeeed.main;

public interface DocumentMetadataKeys {
    String DOCUMENT_ORIGINAL_PATH = "document_original_path";
    String DOCUMENT_TEXT = "text";
    String PROCESSING_EXCEPTION = "processing_exception";
    String MASTER_DUPLICATE = "master_duplicate";
    String ATTACHMENT_PARENT = "attachment_parent";
    String CUSTODIAN = "Custodian";
    String LINK_NATIVE = "native_link";
    //public static final String LINK_TEXT = "text_link";
    String LINK_EXCEPTION = "exception_link";
}
