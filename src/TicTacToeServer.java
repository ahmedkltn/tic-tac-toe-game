import java.io.*;
import java.net.*;

public class TicTacToeServer {
    private static final int PORT = 12345;
    private static char[][] board = {
            {' ', ' ', ' '},
            {' ', ' ', ' '},
            {' ', ' ', ' '}
    };
    private static PrintWriter playerX, playerO;
    private static char currentPlayer = 'X';

    public static void main(String[] args) {
        System.out.println("Tic-Tac-Toe Server is running...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Accept Player X
            Socket playerXSocket = serverSocket.accept();
            playerX = new PrintWriter(playerXSocket.getOutputStream(), true);
            playerX.println("You are Player X. Waiting for Player O...");

            // Accept Player O
            Socket playerOSocket = serverSocket.accept();
            playerO = new PrintWriter(playerOSocket.getOutputStream(), true);
            playerX.println("Player O has joined!");
            playerO.println("You are Player O. Game is starting!");

            BufferedReader inputX = new BufferedReader(new InputStreamReader(playerXSocket.getInputStream()));
            BufferedReader inputO = new BufferedReader(new InputStreamReader(playerOSocket.getInputStream()));

            // Game loop
            while (true) {
                sendBoard();
                PrintWriter currentWriter = (currentPlayer == 'X') ? playerX : playerO;
                BufferedReader currentInput = (currentPlayer == 'X') ? inputX : inputO;

                currentWriter.println("Your turn. Enter row and column (1-3 1-3): ");
                String move = currentInput.readLine();

                if (move == null) break; // Handle disconnection

                String[] parts = move.split(" ");
                int row = Integer.parseInt(parts[0]) - 1;
                int col = Integer.parseInt(parts[1]) - 1;

                if (board[row][col] == ' ') {
                    board[row][col] = currentPlayer;
                    if (checkWin(currentPlayer)) {
                        sendBoard();
                        playerX.println("Player " + currentPlayer + " wins! 🎉");
                        playerO.println("Player " + currentPlayer + " wins! 🎉");
                        break;
                    }
                    if (isDraw()) {
                        sendBoard();
                        playerX.println("It's a draw! 🤝");
                        playerO.println("It's a draw! 🤝");
                        break;
                    }
                    currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                } else {
                    currentWriter.println("Invalid move. Try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendBoard() {
        StringBuilder sb = new StringBuilder("\n  1 2 3\n");
        for (int i = 0; i < 3; i++) {
            sb.append((i + 1) + " ");
            for (int j = 0; j < 3; j++) {
                sb.append(board[i][j] + (j < 2 ? "|" : ""));
            }
            sb.append("\n");
            if (i < 2) sb.append("  -----\n");
        }
        playerX.println(sb.toString());
        playerO.println(sb.toString());
    }

    private static boolean checkWin(char player) {
        for (int i = 0; i < 3; i++) {
            if ((board[i][0] == player && board[i][1] == player && board[i][2] == player) || // Row
                    (board[0][i] == player && board[1][i] == player && board[2][i] == player)) { // Column
                return true;
            }
        }
        return (board[0][0] == player && board[1][1] == player && board[2][2] == player) || // Diagonal \
                (board[0][2] == player && board[1][1] == player && board[2][0] == player);   // Diagonal /
    }

    private static boolean isDraw() {
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == ' ') {
                    return false;
                }
            }
        }
        return true;
    }
}
