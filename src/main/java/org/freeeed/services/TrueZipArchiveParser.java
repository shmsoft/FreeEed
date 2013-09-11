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
package org.freeeed.services;

import de.schlichtherle.truezip.file.TFile;

/**
 *
 * @author mark
 *
 * Test code for trying out TrueZip
 *
 */
public class TrueZipArchiveParser {

    // TODO fix paths if you want to use this
    public static void main(String argv[]) {
        TrueZipArchiveParser instance = new TrueZipArchiveParser();
        String archivePath = "/home/mark/projects/FreeEedData/edrm-enron-v2_bailey-s_pst.zip";
        instance.listFiles(new TFile(archivePath));
        archivePath = "/home/mark/NetBeansProjects/FreeEed/freeeed-output/staging/input00001.zip";
        instance.listFiles(new TFile(archivePath));
    }

    public void listFiles(TFile archivePath) {
        System.out.println("Zip entry: " + archivePath);
        TFile[] entries = new TFile(archivePath).listFiles();
        for (TFile tfile : entries) {
            System.out.println("getAbsolutePath: " + tfile.getAbsolutePath());
            System.out.println("getEnclEntryName: " + tfile.getEnclEntryName());
            System.out.println("getInnerEntryName: " + tfile.getInnerEntryName());
            System.out.println("getName: " + tfile.getName());
            if (tfile.isFile()) {
                System.out.println("It's a file!");
            } else {
                listFiles(tfile);
            }
        }
    }
}
