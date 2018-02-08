package org.freeeed.main;

import java.util.Objects;
import java.util.Properties;

/**
 * Created by nehaojha on 08/02/18.
 */
public class OCRTestProperties {

    public static boolean ocrEnabled;
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(FreeEedMainTest.class.getResourceAsStream("/test.properties"));
            String ocr = properties.getProperty("ocr");
            if (Objects.nonNull(ocrEnabled) && "true".equals(ocr)) {
                ocrEnabled = true;
            }
        } catch (Exception ex) {
            ocrEnabled = false;
        }
    }
}
