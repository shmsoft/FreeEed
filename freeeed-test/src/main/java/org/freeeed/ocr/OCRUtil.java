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
package org.freeeed.ocr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * Class OCRUtil.
 * 
 * @author ilazarov
 *
 */
public class OCRUtil {
	private static AtomicLong incrementor = new AtomicLong(1);
	
	public static String createUniqueFileName(String out) {
		return out + System.currentTimeMillis() + "-" + incrementor.getAndIncrement();
	}
	
	public static String readFileContent(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}

		return stringBuilder.toString();
	}
	
	public static double compareText(String text, String source) {
		Map<String, Integer> sourceMap = parseTextToWords(source);
		Map<String, Integer> textMap = parseTextToWords(text);
		
		int total = 0;
		int notFound = 0;
		for (Map.Entry<String, Integer> textEntry : textMap.entrySet()) {
			String word = textEntry.getKey();
			int count = textEntry.getValue();
			
			Integer sourceCount = sourceMap.get(word);
			int sCount = sourceCount != null ? sourceCount.intValue() : 0;
			
			total += count;
			notFound += (count > sCount) ? count - sCount : 0;
		}
		
		return (double) (total - notFound) / (double) total;
	}
	
	private static Map<String, Integer> parseTextToWords(String source) {
		Map<String, Integer> words = new HashMap<String, Integer>();
		String[] sourceLines = source.split("\n");
		for (String souceLine : sourceLines) {
			String[] sourcePhrases = souceLine.split(" ");
			
			for (String sourcePhrase : sourcePhrases) {
				String[] sourceWords = sourcePhrase.split(",");
				
				for (String sourceWord : sourceWords) {
					sourceWord = sourceWord.trim();
					
					Integer value = words.get(sourceWord);
					if (value == null) {
						value = 0;
					}
					++value;
					words.put(sourceWord, value);
				}
			}
		}
		
		return words;
	}
}
