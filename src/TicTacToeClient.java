import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TicTacToeClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    private JFrame frame;
    private JButton[][] buttons;
    private JLabel messageLabel;
    private PrintWriter out;
    private BufferedReader in;
    private boolean myTurn = false; // Track if it's this player's turn

    public TicTacToeClient() {
        // Create GUI
        frame = new JFrame("Tic Tac Toe - Network Game");
        buttons = new JButton[3][3];
        messageLabel = new JLabel("Waiting for the server...", SwingConstants.CENTER);

        frame.setLayout(new BorderLayout());
        frame.add(messageLabel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        frame.add(boardPanel, BorderLayout.CENTER);

        // Create buttons
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JButton button = new JButton("");
                buttons[row][col] = button;
                button.setFont(new Font("Arial", Font.BOLD, 50));
                button.setEnabled(false); // Initially disable all buttons
                boardPanel.add(button);

                final int r = row;
                final int c = col;

                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (myTurn) { // Only allow move when it's this player's turn
                            handleMove(r, c);
                        } else {
                            JOptionPane.showMessageDialog(frame, "It's not your turn!", "Wait", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
            }
        }

        frame.setSize(400, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Connect to server
        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Start listening to server messages in a separate thread
            new Thread(this::listenToServer).start();
        } catch (Exception e) {
            messageLabel.setText("Error: Cannot connect to server!");
            e.printStackTrace();
        }
    }

    private void handleMove(int row, int col) {
        if (!buttons[row][col].getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Invalid move! This spot is taken.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        out.println((row + 1) + " " + (col + 1)); // Send move to server
        myTurn = false; // Disable move until the server confirms
        disableBoard(); // Prevent further clicks until the turn is updated
    }

    private void listenToServer() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("Received from server: " + response); // Debugging

                JSONObject json = new JSONObject(response);
                String type = json.getString("type");

                switch (type) {
                    case "BOARD_UPDATE":
                        updateBoard(json);
                        break;
                    case "GAME_MESSAGE":
                        String msg = json.getString("message");
                        messageLabel.setText(msg);
                        if (msg.startsWith("Your turn")) {
                            myTurn = true;  // Allow this player to move
                            enableBoard();  // Enable buttons
                        } else {
                            myTurn = false;
                            disableBoard();
                        }
                        break;
                    case "WAIT":
                        messageLabel.setText(json.getString("message"));
                        myTurn = false;
                        disableBoard();
                        break;
                    case "GAME_RESULT":
                        messageLabel.setText(json.getString("message"));
                        disableBoard();
                        break;
                    case "ERROR":
                        JOptionPane.showMessageDialog(frame, json.getString("message"), "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                }
            }
        } catch (Exception e) {
            messageLabel.setText("Connection lost!");
            e.printStackTrace();
        }
    }

    private void updateBoard(JSONObject json) {
        SwingUtilities.invokeLater(() -> {
            String[][] board = new String[3][3];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    board[i][j] = json.getJSONArray("board").getJSONArray(i).getString(j);
                    if (!board[i][j].equals(" ") && !buttons[i][j].getText().equals(board[i][j])) {
                        buttons[i][j].setText(board[i][j]);
                        buttons[i][j].setEnabled(false);
                    }
                }
            }
        });
    }

    private void disableBoard() {
        SwingUtilities.invokeLater(() -> {
            for (JButton[] row : buttons) {
                for (JButton button : row) {
                    button.setEnabled(false);
                }
            }
        });
    }

    private void enableBoard() {
        SwingUtilities.invokeLater(() -> {
            for (JButton[] row : buttons) {
                for (JButton button : row) {
                    if (button.getText().isEmpty()) {
                        button.setEnabled(true);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TicTacToeClient::new);
    }
}