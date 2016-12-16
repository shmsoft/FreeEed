/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.analytics;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.palette.ColorPalette;
import java.awt.Color;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author mark
 */
public class WordCloud {

    private static final Random RANDOM = new Random();

    public static void main(String[] argv) {

        try {
            final List<WordFrequency> wordFrequencies = buildWordFrequencies().subList(0, 150);
            final Dimension dimension = new Dimension(600, 600);
            final com.kennycason.kumo.WordCloud wordCloud = new com.kennycason.kumo.WordCloud(dimension, CollisionMode.RECTANGLE);
            wordCloud.setPadding(0);
            wordCloud.setBackground(new CircleBackground(300));
            wordCloud.setColorPalette(buildRandomColorPalette(20));
            wordCloud.setFontScalar(new LinearFontScalar(10, 40));
            wordCloud.build(wordFrequencies);
            wordCloud.writeToFile("output/wordcloud_circle.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<WordFrequency> buildWordFrequencies() throws IOException {
        final List<String> pokemonNames = getPokemonNames();
        final List<WordFrequency> wordFrequencies = new ArrayList<>();
        for (String pokemon : pokemonNames) {
            wordFrequencies.add(new WordFrequency(pokemon, RANDOM.nextInt(100) + 1));
        }
        return wordFrequencies;
    }

    private static List<String> getPokemonNames() throws IOException {
        //return IOUtils.readLines(getInputStream("config/text/pokemon.txt"));
        return IOUtils.readLines(new FileInputStream("config/text/pokemon.txt"));
    }

    private static InputStream getInputStream(final String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
        private static ColorPalette buildRandomColorPalette(int n) {
        final Color[] colors = new Color[n];
        for(int i = 0; i < colors.length; i++) {
            colors[i] = new Color(RANDOM.nextInt(230) + 25, RANDOM.nextInt(230) + 25, RANDOM.nextInt(230) + 25);
        }
        return new ColorPalette(colors);
    }
}
