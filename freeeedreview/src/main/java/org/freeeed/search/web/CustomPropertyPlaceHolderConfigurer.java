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
package org.freeeed.search.web;

import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * 
 * Utility class for substituting spring property place holders
 * with their real values.
 * 
 * @author ilazarov
 *
 */
public class CustomPropertyPlaceHolderConfigurer extends PropertyPlaceholderConfigurer {
	
	protected String resolvePlaceholder(String placeholder, Properties props) {
		return props.getProperty(placeholder);
   	}

}
