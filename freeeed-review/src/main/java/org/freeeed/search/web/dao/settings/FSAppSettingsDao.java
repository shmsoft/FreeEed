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
package org.freeeed.search.web.dao.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;
import org.freeeed.search.web.model.AppSettings;

/**
 * 
 * Class FSAppSettingsDao.
 * 
 * @author ilazarov
 *
 */
public class FSAppSettingsDao implements AppSettingsDao {
    private static final String SETTINGS_FILE = "work/a.dat";
    private static final Logger log = Logger.getLogger(FSAppSettingsDao.class);
    private AppSettings appSettings;
            
    public void init() {
        log.info("Init FS App Settings DAO...");
        this.appSettings = loadFSSettings();
    }
    
    @Override
    public synchronized void storeSettings(AppSettings appSettings) {
        this.appSettings = appSettings;
        storeFSSettings(appSettings);
    }

    @Override
    public AppSettings loadSettings() {
        if (appSettings == null) {
            return null;
        }
        
        AppSettings result = new AppSettings();
        
        result.setResultsPerPage(appSettings.getResultsPerPage());
        result.setSolrEndpoint(appSettings.getSolrEndpoint());
        
        return result;
    }

    private void storeFSSettings(AppSettings appSettings) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        
        try {
            File dir = new File(SETTINGS_FILE);
            File parent = dir.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            
            fos = new FileOutputStream(dir);
            oos = new ObjectOutputStream(fos);
            
            oos.writeObject(appSettings);
            
            oos.close();
            fos.close();
        } catch (Exception e) {
            log.error("Problem storing app settings from file system!", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("Problem closing", e);
                }
            }
            
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    log.error("Problem closing", e);
                }
            }
        }
    }
    
    private AppSettings loadFSSettings() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(SETTINGS_FILE);
            ois = new ObjectInputStream(fis);
            
            AppSettings data = (AppSettings) ois.readObject();
            return data;
        } catch (Exception e) {
            log.error("Problem loading app settings from file system!");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("Problem closing", e);
                }
            }
            
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    log.error("Problem closing", e);
                }
            }
        }
        
        return null;
    }
}
