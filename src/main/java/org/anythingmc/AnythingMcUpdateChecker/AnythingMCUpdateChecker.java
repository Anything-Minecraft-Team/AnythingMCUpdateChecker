package org.anythingmc.AnythingMcUpdateChecker;

import java.io.File;

public class AnythingMCUpdateChecker {

    public static void main(String[] args) {

        System.out.println("online");

        File theDir = new File("./AnythingMCUpdateChecker");
        if (!theDir.exists()){
            theDir.mkdirs();
        }

        new CheckForUpdates();
        /**Timer timer = new Timer ();
        TimerTask t = new TimerTask() {
            @Override
            public void run () {
                // some code
            }
        };

        timer.schedule (t, 0L, 1000*60*60*24);**/
    }
}