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
package org.freeeed.mail;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

public interface EmailDataProvider {
    
    String getContent() throws MessagingException, IOException;
    
    List<String> getAttachmentNames();
    
    List<String> getFrom();
    
    String getSubject();
    
    List<String> getTo();
    
    List<String> getCC();
    
    Date getDate();
    
    List<String> getRecepient();
    
    List<String> getBCC();
    
    Map<String, String> getAttachmentsContent();
}
