package rip.ethereal.generator.util;

/**
 * @author antja03
 * @since 8/7/2019
 */
public class KillableThread extends Thread {

    private boolean running;

    public boolean isRunning() {
        return running;
    }

    public void kill() {
        running = false;
    }

    public void startThread() {
        running = true;
    }

}
