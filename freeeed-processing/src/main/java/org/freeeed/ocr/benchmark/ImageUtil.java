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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
@Deprecated
public class ImageUtil {

    public static void createImage(String text, String file, String type) {
        BufferedImage img = new BufferedImage(2192, 1672,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = img.createGraphics();

        g2.setPaint(new Color(255, 255, 255));
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());

        g2.setColor(Color.black);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));

        drawString(g2, text, 100, 100, img.getWidth() - 300);

        File outputfile = new File(file);
        try {
            ImageIO.write(img, type, outputfile);
        } catch (IOException e) {
            System.out.println("Problem saving generated image: " + e.getMessage());
        }
    }

    public static void drawString(Graphics2D g, String s, int x, int y, int width) {
        // FontMetrics gives us information about the width,
        // height, etc. of the current Graphics object's Font.
        FontMetrics fm = g.getFontMetrics();

        int lineHeight = fm.getHeight();

        int curX = x;
        int curY = y;

        String[] words = s.split(" ");

        for (String word : words) {
            // Find out thw width of the word.
            int wordWidth = fm.stringWidth(word + " ");

            // If text exceeds the width, then move to next line.
            if (curX + wordWidth >= x + width) {
                curY += lineHeight;
                curX = x;
            }

            g.drawString(word, curX, curY);

            // Move over to the right for next word.
            curX += wordWidth;
        }
    }
}
