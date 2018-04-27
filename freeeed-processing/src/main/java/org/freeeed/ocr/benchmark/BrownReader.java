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
package org.freeeed.ocr.benchmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BrownReader {

    public List<String> readCorpus(String file) {
        String line;
        List<String> text = new ArrayList<>();

        // simple tokenizer: match one or more spaces
        // String delimiters = " +";

        Pattern delimiters = Pattern.compile("[ ]+");
        // Split input with the pattern

        int line_count = 0;

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));

            while ((line = in.readLine()) != null) {
                if (line.trim().length() > 0) {
                    line_count += 1;
                    String[] tokens = delimiters.split(line);

                    for (int x = 0; x < tokens.length; x++) { // iterate over
                        // tokens with
                        // their
                        // corresponding POS
                        tokens[x] = tokens[x].replaceAll("[\\n\\t]+", "");

                        // for cases in Brown corpus like "//in" :(
                        if (tokens[x].startsWith("//")) {
                            String t = tokens[x].replace("//", "per/");
                            tokens[x] = t;
                        }

                        // and that was not all, further for cases like:
                        // "before/in /l//nn and/cc AAb//nn or/cc /r//nn ./. "
                        // (text j in NLTK distribution)
                        if (tokens[x].startsWith("/", 0)) {
                            String t = tokens[x].substring(1);
                            tokens[x] = t;
                        }
                        // for cases like : "AAb//nn" (s. above)
                        if (tokens[x].contains("//")) {
                            int j = tokens[x].indexOf("//");

                            String t = tokens[x].substring(0, j)
                                    + tokens[x].substring(j + 1);
                            tokens[x] = t;
                        }

                        // for cases in brown like: "lb/day/nn" (text 'J',
                        // sentence N. 8940)
                        int first = tokens[x].indexOf("/");
                        int last = tokens[x].lastIndexOf("/");
                        if (first != last) {
                            String[] zw = tokens[x].split("/");
                            StringBuilder t = new StringBuilder();
                            for (int w = 0; w < zw.length - 1; w++) {

                                t.append(zw[w]);
                            }

                            t.append("/");
                            t.append(zw[zw.length - 1]);
                            tokens[x] = t.toString();
                        }

                        String[] t = tokens[x].split("/");
                        text.add(t[0]);
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }

        return text;
    }
}
