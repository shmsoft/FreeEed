package org.freeeed.services;

import org.freeeed.ui.ProcessProgressUI;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class StatsTest {

    @Test
    public void increaseItemCountWorksWithoutProgressUi() {
        assertNull(ProcessProgressUI.getInstance());

        Stats.getInstance().increaseItemCount();
    }
}
