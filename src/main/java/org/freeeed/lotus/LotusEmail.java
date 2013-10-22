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
package org.freeeed.lotus;

import java.util.Date;
import java.util.List;

public class LotusEmail {
    private List<String> bcc;
    private String subject;
    private List<String> cc;
    private List<String> to;
    private String content;
    private Date date;
    private List<String> attachments;
    private List<String> from;
    
    public List<String> getBCC() {
        return bcc;
    }
    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public List<String> getCC() {
        return cc;
    }
    public void setCc(List<String> cc) {
        this.cc = cc;
    }
    public List<String> getTo() {
        return to;
    }
    public void setTo(List<String> to) {
        this.to = to;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public List<String> getAttachmentNames() {
        return attachments;
    }
    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }
    public List<String> getFrom() {
        return from;
    }
    public void setFrom(List<String> from) {
        this.from = from;
    }
}
