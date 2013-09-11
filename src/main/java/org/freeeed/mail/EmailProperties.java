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
package org.freeeed.mail;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * Class EmailProperties.
 * 
 * Keep property values for email processing, like email file hash names, etc.
 * 
 * @author ilazarov
 *
 */
public class EmailProperties extends Properties {
    private static final long serialVersionUID = 6933991845586148451L;
    public static final String EMAIL_HASH_NAMES = "email-hash-names";

    private static final String PROPERTIES_FILE = "config/email-processing.properties";
    
    private static EmailProperties __instance;
    
    private EmailProperties() {
        try {
            load(new FileReader(PROPERTIES_FILE));
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
    
    /**
     * 
     * This class is singleton.
     * Returns the single instance of the class.
     * 
     * @return
     */
    public static synchronized EmailProperties getInstance() {
        if (__instance == null) {
            __instance = new EmailProperties();
        }
        
        return __instance;
    }
}
