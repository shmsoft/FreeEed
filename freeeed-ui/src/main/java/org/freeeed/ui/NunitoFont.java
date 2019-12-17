package org.freeeed.ui;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class NunitoFont {


    public static Font getFont(float size) {
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, NunitoFont.class.getClassLoader().getResource("fonts/Nunito-SemiBold.ttf").openStream()).deriveFont(size);
        } catch (Exception e) {
            font = new Font("Calibri", Font.PLAIN, 5);
        }
        return font;
    }


}
