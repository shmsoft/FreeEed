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

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class GroupComparator extends WritableComparator {

    private static final Logger logger = LoggerFactory.getLogger(GroupComparator.class);

    protected GroupComparator() {
        super(Text.class, true);
    }
    // TODO: can do much better here: do a custom comparator that stops when it
    // hits a tab character (need to state assumptions about UTF-8)

    @Override
    public int compare(WritableComparable t1, WritableComparable t2) {        
        String[] t1Split = t1.toString().split("\t");
        String[] t2Split = t2.toString().split("\t");
        return t1Split[0].compareTo(t2Split[0]);
    }
}