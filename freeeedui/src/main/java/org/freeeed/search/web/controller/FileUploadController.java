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
package org.freeeed.search.web.controller;

import java.io.File;

import org.apache.log4j.Logger;
import org.freeeed.search.files.CaseFileService;
import org.freeeed.search.web.WebConstants;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Class FileUploadController.
 * 
 * @author ilazarov.
 *
 */
public class FileUploadController extends SecureController {
    private static final Logger log = Logger.getLogger(FileUploadController.class);
    
    private CaseFileService caseFileService;
    
    @Override
    public ModelAndView execute() {
        if (!(request instanceof MultipartHttpServletRequest)) {
            valueStack.put("status", "error");
            
            return null;
        }

        log.debug("Uploading file...");
        
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        
        String dest = caseFileService.uploadFile(file);
        if (dest != null) {
            valueStack.put("fileName", dest.replace("\\", "\\\\"));
            valueStack.put(
                    "fileNameShort",
                    dest.lastIndexOf(File.separator) == -1 ? dest : dest
                            .substring(dest.lastIndexOf(File.separator) + 1));
            valueStack.put("status", "success");
        } else {
            valueStack.put("status", "error");
        }

        return new ModelAndView(WebConstants.UPLOAD_FILE);
    }

    public void setCaseFileService(CaseFileService caseFileService) {
        this.caseFileService = caseFileService;
    }
}
