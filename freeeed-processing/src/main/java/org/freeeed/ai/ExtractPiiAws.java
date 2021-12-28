package org.freeeed.ai;

import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * https://docs.aws.amazon.com/comprehend/latest/dg/get-started-api-pii.html
 * <p>
 * For testing
aws comprehend detect-pii-entities \
--text "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010" \
--language-code en
 */

public class ExtractPiiAws {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractPiiAws.class);

    public List <String> extractPii(String data) {
        String q = "\"";
        String command =
                "aws comprehend detect-pii-entities" +
                        " --text " + q +
                        "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010"
                        + q  + " --language-code en";
        List<String> list = null;
        try {
            list = OsUtil.runCommand(command);
        } catch (IOException e) {
            LOGGER.error("AWS PII error", e);
        }
        return list;

    }
    public String extractPiiAsString(String data) {
        List <String> list = extractPii(data);
        StringBuffer buffer = new StringBuffer();
        for (String pii : list) {
            buffer.append(pii + "\n");
        }
        return buffer.toString();
    }
}
