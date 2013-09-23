/*    
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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.freeeed.mail.EmailDataProvider;


public class NSFXDataParser implements EmailDataProvider {
    private LotusEmail lotusEmail;
    
    public NSFXDataParser(File nsfxFile) {
        FileStorage fs = new FileStorage();
        lotusEmail = fs.readEmail(nsfxFile);
    }
    
    @Override
    public String getContent() throws MessagingException, IOException {
        return lotusEmail.getContent();
    }

    @Override
    public List<String> getAttachmentNames() {
        return lotusEmail.getAttachmentNames();
    }

    @Override
    public List<String> getFrom() {
        return lotusEmail.getFrom();
    }

    @Override
    public String getSubject() {
        return lotusEmail.getSubject();
    }

    @Override
    public List<String> getTo() {
        return lotusEmail.getTo();
    }

    @Override
    public List<String> getCC() {
        return lotusEmail.getCC();
    }

    @Override
    public Date getDate() {
        return lotusEmail.getDate();
    }

    @Override
    public List<String> getRecepient() {
        return lotusEmail.getTo();
    }

    @Override
    public List<String> getBCC() {
        return lotusEmail.getBCC();
    }

}
