package org.freeeed.main;

/**
 *
 * @author mark
 */
public class Delim {    
    static public final char TAB = '\t';
    static public final char ONE = '\u0001';
    static public final char PIPE = '|';
    static public final char CARRET = '^';    
    
    public static char getDelim(String delimName) {
        if ("TAB".equalsIgnoreCase(delimName)) {
            return TAB;
        } else if ("ONE".equalsIgnoreCase(delimName)) {
            return ONE;
        } else if ("PIPE".equalsIgnoreCase(delimName)) {
            return PIPE;
        }
        else if ("CARRET".equalsIgnoreCase(delimName)) {
            return CARRET;
        }
        return TAB;
    }
    public static String getSpelledDelim(String delimName) {
        if ("TAB".equalsIgnoreCase(delimName)) {
            return "\\t";
        } else if ("ONE".equalsIgnoreCase(delimName)) {
            return "\\u0001";
        } else if ("PIPE".equalsIgnoreCase(delimName)) {
            return "|";
        }
        else if ("CARRET".equalsIgnoreCase(delimName)) {
            return "^";
        }
        return "\\t";
    }    
}
