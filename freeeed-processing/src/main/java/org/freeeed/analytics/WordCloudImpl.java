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
package org.freeeed.analytics;

import com.google.common.io.ByteStreams;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.palette.ColorPalette;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.freeeed.services.Project;

/**
 *
 * @author mark
 */
public class WordCloudImpl {

    private static final Random RANDOM = new Random();

    public static void main(String[] argv) {
        try {
            WordCloudImpl instance = new WordCloudImpl();
            instance.generateWordCloud("output/wordcloud_circle.png", 600, 600, 150);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void generateWordCloud(String outputFile,
            int width, int height, int topNTerms) throws Exception {
        final List<WordFrequency> wordFrequencies = readWordFrequencies().subList(0, topNTerms);
        final Dimension dimension = new Dimension(width, height);
        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
        wordCloud.setPadding(0);
        wordCloud.setBackground(new CircleBackground(300));
        wordCloud.setColorPalette(buildRandomColorPalette(20));
        wordCloud.setFontScalar(new LinearFontScalar(10, 40));
        wordCloud.build(wordFrequencies);
        wordCloud.writeToFile(outputFile);
    }

    private List<WordFrequency> readWordFrequencies() throws IOException {
        List<WordFrequency> freqs = new ArrayList<>();
        Project project = Project.getCurrentProject();
        String nativeFilePath = project.getResultsDir() + File.separator + "native.zip";
        TFile zipFile = new TFile(nativeFilePath);
        TFile[] files = zipFile.listFiles();
        for (TFile file : files) {
            if ("text".equalsIgnoreCase(file.getName())) {
                Map<String, Integer> wordMap = new HashMap<>();
                TFile[] textFiles = file.listFiles();
                for (TFile textFile : textFiles) {
                    TFileInputStream fileInputStream = new TFileInputStream(textFile);
                    String fileText = new String(ByteStreams.toByteArray(fileInputStream));
                    String[] tokens = getTokens(fileText);
                    for (String token : tokens) {
                        String sanitizedToken = token; // maybe some more sanitization later
                        if (wordMap.containsKey(sanitizedToken)) {
                            wordMap.put(sanitizedToken, 1 + wordMap.get(sanitizedToken));
                        } else {
                            wordMap.put(sanitizedToken, 1);
                        }
                    }
                    fileInputStream.close();
                }
                for (Map.Entry<String, Integer> map : wordMap.entrySet()) {
                    freqs.add(new WordFrequency(map.getKey(), map.getValue()));
                }
            }
        }
        return freqs;
    }

    private static ColorPalette buildRandomColorPalette(int n) {
        final Color[] colors = new Color[n];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = new Color(RANDOM.nextInt(230) + 25, RANDOM.nextInt(230) + 25, RANDOM.nextInt(230) + 25);
        }
        return new ColorPalette(colors);
    }

    private String[] getTokens(String text) {
        // for now, return words
        return text.split("[^a-zA-Z]+");
        // but in the future, take out stop words, and later, even an arbitrary given list of words
    }
}
