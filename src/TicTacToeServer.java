import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class TicTacToeServer {
    private static final int PORT = 12345;
    private static final char[][] board = {
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

            // Send JSON message to Player X
            JSONObject msgX = new JSONObject();
            msgX.put("type", "GAME_MESSAGE");
            msgX.put("message", "You are Player X. Waiting for Player O...");
            playerX.println(msgX);
            playerX.flush();

            // Accept Player O
            Socket playerOSocket = serverSocket.accept();
            playerO = new PrintWriter(playerOSocket.getOutputStream(), true);

            // Send JSON message to both players
            playerX.println(new JSONObject().put("type", "GAME_MESSAGE").put("message", "Player O has joined!"));
            playerX.flush();
            playerO.println(new JSONObject().put("type", "GAME_MESSAGE").put("message", "You are Player O. Game is starting!"));
            playerO.flush();

            BufferedReader inputX = new BufferedReader(new InputStreamReader(playerXSocket.getInputStream()));
            BufferedReader inputO = new BufferedReader(new InputStreamReader(playerOSocket.getInputStream()));

            // Game loop
            while (true) {
                sendBoard(); // Send board update to both players

                PrintWriter currentWriter = (currentPlayer == 'X') ? playerX : playerO;
                PrintWriter waitingPlayer = (currentPlayer == 'X') ? playerO : playerX;
                BufferedReader currentInput = (currentPlayer == 'X') ? inputX : inputO;

                // Notify only the current player
                JSONObject turnMsg = new JSONObject();
                turnMsg.put("type", "GAME_MESSAGE");
                turnMsg.put("message", "Your turn (Player " + currentPlayer + ")");
                currentWriter.println(turnMsg);
                currentWriter.flush();

                // Notify the other player to wait
                JSONObject waitMsg = new JSONObject();
                waitMsg.put("type", "WAIT");
                waitMsg.put("message", "Waiting for Player " + currentPlayer + " to play...");
                waitingPlayer.println(waitMsg);
                waitingPlayer.flush();

                String move = currentInput.readLine();
                if (move == null) break; // Handle disconnection

                String[] parts = move.split(" ");
                if (parts.length != 2) {
                    currentWriter.println(new JSONObject().put("type", "ERROR").put("message", "Invalid move format. Use: row column (e.g., 2 3)"));
                    currentWriter.flush();
                    continue;
                }

                int row, col;
                try {
                    row = Integer.parseInt(parts[0]) - 1;
                    col = Integer.parseInt(parts[1]) - 1;
                } catch (NumberFormatException e) {
                    currentWriter.println(new JSONObject().put("type", "ERROR").put("message", "Invalid move format. Use numbers (e.g., 2 3)"));
                    currentWriter.flush();
                    continue;
                }

                if (row < 0 || row > 2 || col < 0 || col > 2 || board[row][col] != ' ') {
                    currentWriter.println(new JSONObject().put("type", "ERROR").put("message", "Invalid move. Try again."));
                    currentWriter.flush();
                    continue;
                }

                board[row][col] = currentPlayer;

                if (checkWin(currentPlayer)) {
                    sendBoard();
                    JSONObject winMsg = new JSONObject().put("type", "GAME_RESULT").put("message", "Player " + currentPlayer + " wins! 🎉");
                    playerX.println(winMsg);
                    playerO.println(winMsg);
                    playerX.flush();
                    playerO.flush();
                    break;
                }
                if (isDraw()) {
                    sendBoard();
                    JSONObject drawMsg = new JSONObject().put("type", "GAME_RESULT").put("message", "It's a draw! 🤝");
                    playerX.println(drawMsg);
                    playerO.println(drawMsg);
                    playerX.flush();
                    playerO.flush();
                    break;
                }

                // Switch player turn
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendBoard() {
        JSONObject boardUpdate = new JSONObject();
        boardUpdate.put("type", "BOARD_UPDATE");

        // Convert board to JSON array
        String[][] boardArray = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardArray[i][j] = String.valueOf(board[i][j]);
            }
        }
        boardUpdate.put("board", boardArray);

        // Send to players
        playerX.println(boardUpdate);
        playerO.println(boardUpdate);
        playerX.flush();
        playerO.flush();
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