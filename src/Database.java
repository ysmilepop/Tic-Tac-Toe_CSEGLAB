import java.sql.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

public class Database {

    private Connection conn;
    int moveNumber = 1;

    // Initialize the SQLite database connection
    public void initialize() {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to SQLite
            conn = DriverManager.getConnection("jdbc:sqlite:tictactoe.db");

            // Create table for storing game moves if it doesn't exist
            String createMovesTableSQL = "CREATE TABLE IF NOT EXISTS game_moves (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "game_id INTEGER, " +
                    "player TEXT NOT NULL, " +
                    "move_row INTEGER NOT NULL, " +
                    "move_col INTEGER NOT NULL, " +
                    "board_state TEXT NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            Statement stmt = conn.createStatement();
            stmt.execute(createMovesTableSQL);

            // Create table for storing game results if it doesn't exist
            String createResultsTableSQL = "CREATE TABLE IF NOT EXISTS game_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "game_state TEXT NOT NULL, " +
                    "result TEXT NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createResultsTableSQL);
            stmt.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public int storeResult(String result, String gameState) {
        int gameId = -1;
        try {
            String sql = "INSERT INTO game_results (game_state, result) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, gameState);
            pstmt.setString(2, result);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Inserting game result failed, no rows affected.");
            }

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                gameId = rs.getInt(1);
            } else {
                throw new SQLException("Inserting game result failed, no ID obtained.");
            }
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gameId;
    }

    
    public void storeMove(int gameId, String player, int row, int col, String boardState) {
        try {
            String sql = "INSERT INTO game_moves (game_id, player, move_row, move_col, board_state) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, gameId);
            pstmt.setString(2, player);
            pstmt.setInt(3, row);
            pstmt.setInt(4, col);
            pstmt.setString(5, boardState);
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

            // Query to get all game results
            String sql = "SELECT * FROM game_results";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int gameId = rs.getInt("id");
                String gameState = rs.getString("game_state");
                String result = rs.getString("result");

                // Create a panel to display the game result
                JPanel resultPanel = new JPanel();
                resultPanel.setLayout(new GridLayout(1, 1));
                JLabel resultLabel = new JLabel("Result: " + result);
                resultLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
                resultPanel.add(resultLabel);

                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.weightx = 1.0;
                gbc.weighty = 0.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                historyPanel.add(resultPanel, gbc);

                row++;

                // Query to get all moves for the current game result
                String sqlMoves = "SELECT * FROM game_moves WHERE game_id = ? ORDER BY id";
                PreparedStatement pstmt = conn.prepareStatement(sqlMoves);
                pstmt.setInt(1, gameId);
                ResultSet moves = pstmt.executeQuery();

                while (moves.next()) {
                    String boardState = moves.getString("board_state");
                    String[] moveCells = boardState.split(",");
                    JPanel boardPanel = new JPanel();
                    boardPanel.setLayout(new GridLayout(3, 3));
                    boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

                    for (int i = 0; i < 9; i++) {
                        JButton cellButton = new JButton();
                        char cellValue = moveCells[i].charAt(0);
                        cellButton.setText(cellValue == '-' ? "" : String.valueOf(cellValue));
                        cellButton.setFont(new Font("Tahoma", Font.PLAIN, 69));
                        cellButton.setEnabled(true);
                        cellButton.setBackground(cellValue == 'X' ? new Color(255, 0, 0) :
                            cellValue == 'O' ? new Color(0, 255, 0) :
                            Color.LIGHT_GRAY);
                        	boardPanel.add(cellButton);

                        // Set square size for the button
                        int buttonSize = 70; 
                        cellButton.setPreferredSize(new Dimension(buttonSize, buttonSize));

                        // Add the button to the panel
                        boardPanel.add(cellButton);
                    }

                    // Ensure that the boardPanel also maintains equal spacing for the grid
                    boardPanel.setLayout(new GridLayout(3, 3, 5, 5));


                    JLabel moveLabel = new JLabel("Move " + moveNumber + ": " + moves.getString("player") + " moved to (" +
                                                   moves.getInt("move_row") + ", " + moves.getInt("move_col") + ")");
                    moveLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));

                    gbc.gridx = 0;
                    gbc.gridy = row;
                    gbc.weightx = 0.7;
                    gbc.weighty = 0;
                    gbc.fill = GridBagConstraints.BOTH;
                    historyPanel.add(boardPanel, gbc);

                    gbc.gridx = 1;
                    gbc.gridy = row;
                    gbc.weightx = 0.3;
                    historyPanel.add(moveLabel, gbc);

                    row++;
                    moveNumber++;
                }

                moves.close();
            }

            rs.close();
            stmt.close();

            JScrollPane scrollPane = new JScrollPane(historyPanel);
            scrollPane.setPreferredSize(new java.awt.Dimension(800, 600));  // Adjust dimensions as needed

            JOptionPane.showMessageDialog(null, scrollPane, "Game History", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public void clearDatabase() {
        try {
            String sql = "DELETE FROM game_results";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
