import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // Compile Java files
            System.out.println("Compiling Java files...");
            Process compileProcess = new ProcessBuilder("javac", "-cp", "libs/json.jar", "-d", "out",
                    "src/TicTacToeServer.java", "src/TicTacToeClient.java").inheritIO().start();
            compileProcess.waitFor();

            // Check if compilation was successful
            if (compileProcess.exitValue() != 0) {
                System.out.println("Compilation failed! Fix errors and try again.");
                return;
            }

            // Start the server in a separate process
            System.out.println("Starting Tic-Tac-Toe Server...");
            new ProcessBuilder("java", "-cp", "libs/json.jar:out", "TicTacToeServer")
                    .inheritIO().start();

            // Wait 2 seconds for the server to start
            Thread.sleep(2000);

            // Start Player 1 (Client)
            System.out.println("Starting Player 1 (Client)...");
            new ProcessBuilder("java", "-cp", "libs/json.jar:out", "TicTacToeClient")
                    .inheritIO().start();

            // Start Player 2 (Client)
            System.out.println("Starting Player 2 (Client)...");
            new ProcessBuilder("java", "-cp", "libs/json.jar:out", "TicTacToeClient")
                    .inheritIO().start();

            System.out.println("All processes started successfully! 🎮");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}