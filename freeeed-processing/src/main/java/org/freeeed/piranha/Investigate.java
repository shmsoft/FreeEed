package org.freeeed.piranha;

import org.freeeed.main.Version;

public class Investigate {
    public static void main(String [] argv) {
        System.out.println("Piranha investigative surgery " + Version.getVersion());
        Investigate instance = new Investigate();
        instance.investigate(argv[0]);
    }
    private void investigate(String dir) {
        System.out.println("Investigating " + dir);
    }
}
