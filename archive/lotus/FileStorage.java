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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileStorage {
    private static final String NSF_EXTRACTED_FILE_EXT = ".nsfe";
    
    private String outputDir;
    private int fileCounter = 1;
    private GsonBuilder gsonBuilder;
    
    public FileStorage() {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss");
    }
    
    public FileStorage(String output, String nsfFile) {
        this();
        
        File f = new File(nsfFile);
        String name = f.getName();
        
        String nsfFileName = name.substring(0, name.lastIndexOf("."));
        this.outputDir = output + File.separator + nsfFileName + File.separator;
        File outDir = new File(outputDir);
        
        System.out.println("Creating dir: " + outDir);
        System.out.println("Status: " + outDir.mkdirs());
    }
    
    public void storeEmail(LotusEmail email) {
        String fileName = outputDir + fileCounter + NSF_EXTRACTED_FILE_EXT;   
        
        Gson gson = gsonBuilder.create();
        String json = gson.toJson(email);
        
        FileWriter fw = null;
        try {
            fw = new FileWriter(fileName);
            fw.write(json);
        } catch (Exception e) {
            System.out.println("Problem saving file: " + fileName);
            e.printStackTrace();
        } finally {
            fileCounter++;
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    public LotusEmail readEmail(File file) {
        try {
            String content = Files.toString(file, Charset.defaultCharset());
            Gson gson = gsonBuilder.create();
            
            LotusEmail email = gson.fromJson(content, LotusEmail.class);
            return email;
        } catch (IOException e) {
            System.out.println("Unable to read file content: " + file);
        }
        
        return null;
    }
    
    public String getOutputDir() {
        return outputDir;
    }
}
