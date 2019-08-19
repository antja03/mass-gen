package rip.ethereal.generator.misc;

import rip.ethereal.generator.util.KillableThread;
import rip.ethereal.generator.util.synchronize.SynchronizedArraylist;
import rip.ethereal.generator.util.synchronize.SynchronizedHashmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author antja03
 * @since 8/7/2019
 */
public class AccountWriter extends KillableThread {

    /**
     * A synchronized map which stores all queued combos and the files they should be written to
     */
    private SynchronizedHashmap<File, SynchronizedArraylist<String>> queue;

    /**
     * The final queue where entries end up
     */
    private HashMap<File, ArrayList<String>> finalQueue;

    public AccountWriter() {
        queue = new SynchronizedHashmap<>();
        finalQueue = new HashMap<>();
    }

    /**
     * Loops through all entries in @finalQueue and writes all combos to their corresponding files
     */
    @Override
    public void run() {
        startThread();

        while (isRunning()) {
            finalQueue.putAll(queue);
            queue.clear();

            for (Map.Entry<File, ArrayList<String>> entry : finalQueue.entrySet()) {

                File file = entry.getKey();
                List<String> combos = entry.getValue();

                if (file.exists()) {
                    try {
                        FileWriter fileWriter = new FileWriter(file, true);

                        for (String combo : combos) {
                            fileWriter.write(combo + System.getProperty("line.separator"));
                        }

                        fileWriter.flush();
                        fileWriter.close();
                        combos.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Adds a combo to @finalQueue with the specified file
     *
     * @param file The file that @combo should be written to
     * @param combo The username:password combination that will be written
     */
    public void addToQueue(File file, String combo) {
        List<String> combos = queue.getOrDefault(file, null);
        if (combos != null) {
            combos.add(combo);
        } else {
            queue.put(file, new SynchronizedArraylist<>());
            addToQueue(file, combo);
        }
    }

}
