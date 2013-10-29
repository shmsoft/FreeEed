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
package org.freeeed.data.index;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Class ComponentManager.
 * 
 * Keeps singleton instances of registered components. Maintain a basic
 * component lifecycle defined by ComponentLifecycle interface.
 * 
 * @author ilazarov.
 * 
 */
public class ComponentManager {
    private static ComponentManager __instance;

    // define the list with registered components to manage
    private ComponentLifecycle[] registeredComponents = {};
    private Map<String, ComponentLifecycle> componentsMap;

    private ComponentManager() {
        initComponents();

        // application shutdown hook for components destroy
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                for (ComponentLifecycle component : registeredComponents) {
                    component.destroy();
                }
            }
        });
    }

    private void initComponents() {
        componentsMap = new HashMap<String, ComponentLifecycle>();
        
        for (ComponentLifecycle component : registeredComponents) {
            component.init();
            componentsMap.put(component.getClass().getName(), component);
        }
    }

    public synchronized static ComponentManager getInstance() {
        if (__instance == null) {
            __instance = new ComponentManager();
        }
        
        return __instance;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends ComponentLifecycle> T getComponent(String name) {
        return (T) componentsMap.get(name);
    }
}
