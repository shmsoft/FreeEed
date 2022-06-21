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
package org.freeeed.search.web.configuration;

import org.freeeed.search.web.dao.settings.AppSettingsDao;
import org.freeeed.search.web.model.AppSettings;

/**
 * 
 * Class Configuration.
 * 
 * Implements the lifecycle of the product configuration - saving and loading configuration.
 * 
 * @author ilazarov
 *
 */
public class Configuration {
    private AppSettingsDao appSettingsDao;
    
    public String getSolrEndpoint() {
        AppSettings appSettings = appSettingsDao.loadSettings();
        if (appSettings != null) {
            return appSettings.getSolrEndpoint();
        }
        
        return "http://localhost:8983";
    }

    public int getNumberOfRows() {
        AppSettings appSettings = appSettingsDao.loadSettings();
        if (appSettings != null) {
            return appSettings.getResultsPerPage();
        }
        
        return 10;
    }
    
    public void setAppSettingsDao(AppSettingsDao appSettingsDao) {
        this.appSettingsDao = appSettingsDao;
    }
}
