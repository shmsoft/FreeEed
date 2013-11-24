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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author mark
 */
public class SHMcloudConfiguration extends PropertiesConfiguration {

    private Properties cache = new Properties();

    public SHMcloudConfiguration() {
        super();
    }

    public SHMcloudConfiguration(String fileName) throws ConfigurationException {
        super(fileName);
    }

    public void cleanup() {
        cache.clear();
        String projectFileName = (String) getProperty(ParameterProcessing.PROJECT_FILE_NAME);
        if (projectFileName != null) {
            cache.put(ParameterProcessing.PROJECT_FILE_NAME, projectFileName);
        }
        clearProperty(ParameterProcessing.PROJECT_FILE_NAME);
    }

    public void restore() {
        Enumeration keys = cache.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = cache.getProperty(key);
            setProperty(key, value);
        }        
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        Iterator iterator = getKeys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            Object obj = getProperty(key);
            str.append(key).append("=");
            if (obj instanceof String) {
                String value = (String) obj;
                str.append(value).append(",");
            } else if (obj instanceof ArrayList) {
                ArrayList values = (ArrayList) obj;
                for (Object s: values) {
                    String value = (String) s;
                    str.append(value).append(",");
                }                
            }            
        }
        return str.toString();
    }
}
