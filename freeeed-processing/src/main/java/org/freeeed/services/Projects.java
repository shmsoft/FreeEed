/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author mark
 */
public class Projects extends HashMap <Integer, Project> {
    public int getActiveProjectCount() {
        int count = 0;
        Set <Integer> keys = keySet();
        Iterator <Integer> iter = keys.iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            Project project = get(key);
            if (!project.isDeleted()) ++count;
        }
        return count;
    }
}
