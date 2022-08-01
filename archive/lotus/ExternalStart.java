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
package org.freeeed.lotus;

public class ExternalStart {

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Invalid arguments: Usage " + ExternalStart.class + " <nsf file> <output dir> <solr endpoint>");
            System.exit(-1);
        }
        
        String nsfFile = args[0];
        String output = args[1];
        
        String endpoint = null;
        if (args.length > 2) {
            endpoint = args[2];
        }
        
        LotusNotesEmailParser parser = new LotusNotesEmailParser(nsfFile, output, endpoint);
        parser.parse();
    }
}
