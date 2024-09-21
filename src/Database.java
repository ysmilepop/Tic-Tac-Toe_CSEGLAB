import java.sql.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

public class Database {

    private Connection conn;

    // Initialize the SQLite database connection
    public void initialize() {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to SQLite
            conn = DriverManager.getConnection("jdbc:sqlite:tictactoe.db");

            // Create table if it doesn't exist
            String sql = "CREATE TABLE IF NOT EXISTS game_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "game_state TEXT NOT NULL, " +
                    "result TEXT NOT NULL)";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Store the result of the game in the database
    public void storeResult(String result, String gameState) {
        try {
            String sql = "INSERT INTO game_results (game_state, result) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, gameState);
            pstmt.setString(2, result);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Show game history from the database
    public void showGameHistory() {
        try {
            JPanel historyPanel = new JPanel();
            historyPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new java.awt.Insets(5, 5, 5, 5);
            int row = 0;

            String sql = "SELECT * FROM game_results";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String gameState = rs.getString("game_state");
                String[] cells = gameState.split(",");
                JPanel boardPanel = new JPanel();
                boardPanel.setLayout(new GridLayout(3, 3));
                boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

                for (int i = 0; i < 9; i++) {
                    JButton cellButton = new JButton();
                    char cellValue = cells[i].charAt(0);
                    cellButton.setText(cellValue == '-' ? "" : String.valueOf(cellValue));
                    cellButton.setFont(new Font("Tahoma", Font.PLAIN, 30));
                    cellButton.setEnabled(false);
                    cellButton.setBackground(cellValue == 'X' ? new Color(255, 0, 0) : cellValue == 'O' ? new Color(0, 255, 0) : Color.LIGHT_GRAY);
                    boardPanel.add(cellButton);
                }

                JLabel resultLabel = new JLabel("Result: " + rs.getString("result"));
                resultLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));

                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.weightx = 0.7;
                gbc.weighty = 0;
                gbc.fill = GridBagConstraints.BOTH;
                historyPanel.add(boardPanel, gbc);

                gbc.gridx = 1;
                gbc.weightx = 0.3;
                gbc.weighty = 0;
                gbc.fill = GridBagConstraints.VERTICAL;
                historyPanel.add(resultLabel, gbc);

                row++;
            }

            rs.close();
            stmt.close();

            JScrollPane scrollPane = new JScrollPane(historyPanel);
            scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));

            JOptionPane.showMessageDialog(null, scrollPane, "Game History", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
