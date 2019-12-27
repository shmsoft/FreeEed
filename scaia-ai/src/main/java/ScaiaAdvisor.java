package ai.scaia.advisor;



import javax.swing.*;

public class ScaiaAdvisor implements Runnable {
    @Override
    public void run() {

    }
    /*
    private FreeEedUIHelper mainPanel;

    private static ScaiaAdvisor mInstance;

    private ScaiaAdvisor() {
    }

    public static ScaiaAdvisor getInstance() {
        if (mInstance == null) {
            mInstance = new ScaiaAdvisor();
        }
        return mInstance;
    }

    public void setMainPanel(FreeEedUIHelper mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void run() {


        while (true) {


            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mainPanel.setScaiaStatus(false);
                }
            });

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }
    */
}
