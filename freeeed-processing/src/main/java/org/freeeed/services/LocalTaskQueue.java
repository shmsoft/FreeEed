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
 * Facade for tasks. Behind is either SQS or local LIFO implementation.
 */
package org.freeeed.services;

import java.util.Stack;

/**
 *
 * @author mark
 */
public class LocalTaskQueue implements TaskQueue {
    private final Stack <Task> lifoQueue;

    public LocalTaskQueue() {
        this.lifoQueue = new Stack <>();
    }
    
    @Override
    public Task getNext() {
        return lifoQueue.pop();
    }

    @Override
    public void add(Task task) {
         lifoQueue.push(task);
    }

    @Override
    public void confirmDone(Task task) {
        // TODO 
        // we do nothing at the moment, no provision for fault tolerance when
        // running local. That's OK but it means we cannot test.
    }

    /**
     * @return the number of tasks in the queue
     */
    @Override
    public int size() {
        return lifoQueue.size();
    }

    @Override
    public void reset() {

    }
}
