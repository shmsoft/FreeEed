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

/**
 *
 * @author mark
 */
public class Delimiter {
    static public final char TAB = '\t';
    static public final char ONE = '\u0001';
    static public final char PIPE = '|';
    static public final char CARET = '^';
    
    public static char getDelim(String delimName) {
        if ("TAB".equalsIgnoreCase(delimName)) {
            return TAB;
        } else if ("ONE".equalsIgnoreCase(delimName)) {
            return ONE;
        } else if ("PIPE".equalsIgnoreCase(delimName)) {
            return PIPE;
        }
        else if ("CARET".equalsIgnoreCase(delimName)) {
            return CARET;
        }
        return TAB;
    }
    public static String getSpelledDelim(String delimName) {
        if ("TAB".equalsIgnoreCase(delimName)) {
            return "\\t";
        } else if ("ONE".equalsIgnoreCase(delimName)) {
            return "\\u0001";
        } else if ("PIPE".equalsIgnoreCase(delimName)) {
            return "|";
        }
        else if ("CARRET".equalsIgnoreCase(delimName)) {
            return "^";
        }
        return "\\t";
    }    
}
