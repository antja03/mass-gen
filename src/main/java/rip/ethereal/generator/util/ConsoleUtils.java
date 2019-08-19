package rip.ethereal.generator.util;

import java.io.IOException;

public class ConsoleUtils {

    public static void voltzIsBraindead() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        System.out.println();
        System.out.println();
        System.out.println("██████╗ ███████╗███████╗███████╗███████╗    ██████╗ ██████╗\n" +
                "██╔══██╗██╔════╝██╔════╝██╔════╝██╔════╝   ██╔════╝██╔════╝\n" +
                "██████╔╝█████╗  █████╗  █████╗  █████╗     ██║     ██║     \n" +
                "██╔══██╗██╔══╝  ██╔══╝  ██╔══╝  ██╔══╝     ██║     ██║     \n" +
                "██║  ██║███████╗███████╗███████╗███████╗██╗╚██████╗╚██████╗\n" +
                "╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝╚══════╝╚═╝ ╚═════╝ ╚═════╝\n" +
                "\n");
        System.out.println("----------------------------------------");
        System.out.println();
    }

}
