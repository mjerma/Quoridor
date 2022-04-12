package hr.algebra.threads;

import hr.algebra.controller.GameScreenController;
import javafx.application.Platform;

public class ClockThread extends Thread {

    private final GameScreenController controller;

    public ClockThread(GameScreenController controller) {
        this.controller = controller;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            sendTime();
            try {
                Thread.sleep(1000);
                if (controller.hasGameEnded()){
                    stopMe();
                }
            } catch (InterruptedException ex) {
                return;
            }
        }
    }

    private void sendTime() {
        Platform.runLater(()-> controller.updateTime());        
    }

    public void stopMe() throws InterruptedException {
        while (isAlive()) {
            // interrupt thread
            interrupt();
            // join this thread to caller thread
            join();
        }
    }
}
