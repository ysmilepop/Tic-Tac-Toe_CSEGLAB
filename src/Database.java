import java.sql.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

public class Database {

    private Connection conn;
    int moveNumber = 1;
    int gameCount = 1;
    
    public void incrementGameCount() {
        gameCount++;
    }


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

            // Check if the game_label column exists, if not, alter the table to add it
            try {
                String checkColumnSQL = "PRAGMA table_info(game_results)";
                ResultSet rs = stmt.executeQuery(checkColumnSQL);
                boolean columnExists = false;
                while (rs.next()) {
                    String columnName = rs.getString("name");
                    if (columnName.equals("game_label")) {
                        columnExists = true;
                        break;
                    }
                }
                rs.close();

                if (!columnExists) {
                    String alterTableSQL = "ALTER TABLE game_results ADD COLUMN game_label TEXT";
                    stmt.execute(alterTableSQL);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            stmt.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public int storeResult(String result, String gameState) {
        int gameId = -1;
        try {
            String gameLabel = "game" + gameCount;  // Store as game1, game2, etc.
            String sql = "INSERT INTO game_results (game_state, result, game_label) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, gameState);
            pstmt.setString(2, result);
            pstmt.setString(3, gameLabel);  // Store the game label
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
            historyPanel.setBackground(new Color(0, 45, 57));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new java.awt.Insets(5, 5, 5, 5);
            int row = 0;

            // Query to get all game results including the game_label
            String sql = "SELECT * FROM game_results";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            String lastGameLabel = ""; // Track the last displayed game label

            while (rs.next()) {
                int gameId = rs.getInt("id");
                String gameLabel = rs.getString("game_label");  
                String gameState = rs.getString("game_state");
                String result = rs.getString("result");

                // Capitalize the first letter of the game label
                if (gameLabel != null && !gameLabel.isEmpty()) {
                    gameLabel = Character.toUpperCase(gameLabel.charAt(0)) + gameLabel.substring(1);

                    // Check if the current game label is different from the last displayed one
                    if (!gameLabel.equals(lastGameLabel)) {
                        // Add a vertical space between games
                        if (row > 0) {
                            JPanel spacer = new JPanel();
                            spacer.setBackground(new Color(0, 45, 57));
                            gbc.gridx = 0;
                            gbc.gridy = row;
                            gbc.weightx = 1.0;
                            gbc.weighty = 0.0;
                            gbc.fill = GridBagConstraints.HORIZONTAL;
                            historyPanel.add(spacer, gbc);
                            row++;
                        }

                        JPanel labelPanel = new JPanel();
                        labelPanel.setLayout(new GridLayout(1, 1));
                        JLabel gameLabelLabel = new JLabel(gameLabel);  
                        gameLabelLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
                        gameLabelLabel.setForeground(new Color(255, 255, 255));
                        labelPanel.setBackground(new Color(0, 45, 57));
                        labelPanel.add(gameLabelLabel);

                        gbc.gridx = 0;
                        gbc.gridy = row;
                        gbc.weightx = 1.0;
                        gbc.weighty = 0.0;
                        gbc.fill = GridBagConstraints.HORIZONTAL;
                        historyPanel.add(labelPanel, gbc);
                        row++;

                        lastGameLabel = gameLabel; // Update the last displayed game label
                    }

                    // Create a panel to display the game result
                    JPanel resultPanel = new JPanel();
                    resultPanel.setLayout(new GridLayout(1, 1));
                    JLabel resultLabel = new JLabel("Result: " + result);
                    resultLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
                    resultLabel.setForeground(new Color(255, 255, 255));
                    resultPanel.setBackground(new Color(0, 45, 57));
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

                    int moveNumber = 1; // Reset move number for each game
                    while (moves.next()) {
                        String boardState = moves.getString("board_state");
                        String[] moveCells = boardState.split(",");
                        JPanel boardPanel = new JPanel();
                        boardPanel.setLayout(new GridLayout(3, 3));
                        boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                        boardPanel.setBackground(new Color(0, 45, 57));

                        for (int i = 0; i < 9; i++) {
                            JButton cellButton = new JButton();
                            char cellValue = moveCells[i].charAt(0);
                            cellButton.setText(cellValue == '-' ? "" : String.valueOf(cellValue));
                            cellButton.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 70));
                            cellButton.setForeground(new Color(255, 0, 0));
                            cellButton.setBackground(new Color(0, 0, 0));
                            cellButton.setBorder(new LineBorder(Color.CYAN, 5));
                            cellButton.setForeground(cellValue == 'X' ? new Color(255, 0, 0) :
                                                     cellValue == 'O' ? new Color(0, 255, 128) : Color.BLACK);
                            cellButton.setEnabled(true);

                            boardPanel.add(cellButton);

                            // Set square size for the button
                            int buttonSize = 70; 
                            cellButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
                        }

                        // Ensure that the boardPanel also maintains equal spacing for the grid
                        boardPanel.setLayout(new GridLayout(3, 3, 5, 5));

                        JLabel moveLabel = new JLabel("Move " + moveNumber + ": " + moves.getString("player") + " moved to (" +
                                                       moves.getInt("move_row") + ", " + moves.getInt("move_col") + ")");
                        moveLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
                        moveLabel.setForeground(Color.white);

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
            }

            rs.close();
            stmt.close();

            JScrollPane scrollPane = new JScrollPane(historyPanel);
            scrollPane.setPreferredSize(new java.awt.Dimension(800, 600));  

            JFrame frame = new JFrame("GAME HISTORY");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(850, 650);
            frame.add(scrollPane);
            frame.setVisible(true);

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