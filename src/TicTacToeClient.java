import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TicTacToeClient {
    private static final String SERVER_ADDRESS = "localhost"; // Change to server IP if needed
    private static final int PORT = 12345;

    private JFrame frame;
    private JButton[][] buttons;
    private JLabel messageLabel;
    private PrintWriter out;
    private BufferedReader in;

    public TicTacToeClient() {
        // Create GUI components
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
                button.setFont(new Font("Arial", Font.BOLD, 40));
                boardPanel.add(button);

                final int r = row;
                final int c = col;

                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        handleMove(r, c);
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
            new Thread(() -> listenToServer()).start();
        } catch (Exception e) {
            messageLabel.setText("Error: Cannot connect to server!");
            e.printStackTrace();
        }
    }

    private void handleMove(int row, int col) {
        out.println((row + 1) + " " + (col + 1)); // Send move to server
    }

    private void listenToServer() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                if (response.contains("Your turn")) {
                    messageLabel.setText("Your turn!");
                } else if (response.contains("wins") || response.contains("draw")) {
                    messageLabel.setText(response);
                    disableBoard();
                } else if (response.startsWith("\n  ")) {
                    updateBoard(response);
                } else {
                    messageLabel.setText(response);
                }
            }
        } catch (Exception e) {
            messageLabel.setText("Connection lost!");
            e.printStackTrace();
        }
    }

    private void updateBoard(String boardState) {
        String[] rows = boardState.split("\n");
        for (int i = 1; i <= 3; i++) {
            String[] cells = rows[i].substring(2).split("\\|");
            for (int j = 0; j < 3; j++) {
                String cellText = cells[j].trim();
                if (!cellText.equals("")) {
                    buttons[i - 1][j].setText(cellText);
                    buttons[i - 1][j].setEnabled(false);
                }
            }
        }
    }

    private void disableBoard() {
        for (JButton[] row : buttons) {
            for (JButton button : row) {
                button.setEnabled(false);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TicTacToeClient::new);
    }
}
