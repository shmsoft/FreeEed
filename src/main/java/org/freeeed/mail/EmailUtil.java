package org.freeeed.mail;

import java.util.ArrayList;

/**
 *
 * @author mark
 */
public class EmailUtil {

    public static ArrayList<String> parseAddressLines(String[] addressLines) {
        ArrayList<String> fields = new ArrayList<String>();
        for (String addressLine : addressLines) {
            String[] addresses = addressLine.split(",");
            for (String address : addresses) {
                address = address.trim().replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "");
                fields.add(address);
            }
        }
        return fields;
    }
}
