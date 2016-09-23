/**
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

/**
 * Facade for tasks. Behind is either SQS or local FIFO implementation.
 */
package org.freeeed.services;

/**
 *
 * @author mark
 */
public class AwsTaskQueue implements TaskQueue {
    @Override
    public Task getNext() {
       return new Task();
    }
    @Override
    public void add(Task task) {
        
    }
    @Override
    public void confirmDone(Task task) {
        
    }
    /**
     *
     * @return the number of tasks in the queue
     */
    @Override
    public int size() {
        return 0;
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
