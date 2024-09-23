import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;       
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


import java.sql.Connection;

public class TicTacToe extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JPanel boardPanel;
    private char[][] board = new char[3][3];
    private JButton[][] buttons = new JButton[3][3];
    private final char AI_PLAYER = 'X';
    private final char HUMAN_PLAYER = 'O';
    private boolean isEndgame = false;
    private Connection conn;
    private Minimax minimax;
    private Database db;
    int moveNumber = 1;
    private JButton btnPlayAgain;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    TicTacToe frame = new TicTacToe();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public TicTacToe() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getRootPane().setBorder(new LineBorder(Color.BLACK, 10));
        setTitle("TIC-TAC-TOE GAME");
        setBounds(100, 100, 717, 790);
        setLocationRelativeTo(null);

        // Set layout to null for absolute positioning
        contentPane = new JPanel();
        contentPane.setBackground(new Color(0, 45, 57));
        contentPane.setLayout(null);  // Absolute layout for custom positioning
        setContentPane(contentPane);

        // Initialize the board and buttons
        initializeBoardAndButtons();

        // Initialize the database and Minimax
        db = new Database();
        db.initialize();
        minimax = new Minimax();

        // Title label
        JLabel lblTitle = new JLabel("TIC-TAC-TOE");
        lblTitle.setBounds(216, 17, 305, 89);
        lblTitle.setBackground(new Color(255, 255, 255));
        lblTitle.setForeground(new Color(128, 255, 255));
        lblTitle.setFont(new Font("STCaiyun", Font.BOLD, 45));
        contentPane.add(lblTitle);

        // History button at the bottom
        JButton btnHistory = new JButton("History");
        btnHistory.setBounds(239, 594, 216, 54); // Absolute positioning
        btnHistory.setBackground(new Color(0, 0, 0));
        btnHistory.setForeground(new Color(128, 255, 255));
        btnHistory.setFont(new Font("STCaiyun", Font.BOLD, 25));
        btnHistory.setBorder(new LineBorder(Color.CYAN, 2));
        btnHistory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	showGameHistory();
            }
        });
        contentPane.add(btnHistory);
        
        btnPlayAgain = new JButton("Play Again");
        btnPlayAgain.setBounds(239, 660, 216, 54); // Absolute positioning
        btnPlayAgain.setBackground(new Color(0, 0, 0));
        btnPlayAgain.setForeground(new Color(128, 255, 255));
        btnPlayAgain.setFont(new Font("STCaiyun", Font.BOLD, 25));
        btnPlayAgain.setBorder(new LineBorder(Color.CYAN, 2));
        btnPlayAgain.setVisible(false);  // Initially hidden
        btnPlayAgain.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });
        contentPane.add(btnPlayAgain);

        setVisible(true);  // Make the frame visible
    }

    private void initializeBoardAndButtons() {
        // Create a board panel to hold the buttons
        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3)); // Grid layout for 3x3 board
        boardPanel.setBounds(121, 116, 470, 470); // Set bounds for the board panel
        contentPane.add(boardPanel); // Add to content pane with absolute positioning

        // Initialize the buttons
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 70));
                buttons[i][j].setForeground(new Color(255, 0, 0)); // Text color
                buttons[i][j].setBackground(new Color(0, 0, 0)); // Background color
                buttons[i][j].setBorder(new LineBorder(Color.CYAN, 5)); // Button border

                // Add mouse listener for clicks
                final int row = i;
                final int col = j;
                buttons[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!isEndgame) {
                            setMove(buttons[row][col], row, col);
                        }
                    }
                });

                // Add the button to the board panel
                boardPanel.add(buttons[i][j]);
            }
        }
    }

    public void setMove(JButton button, int row, int col) {
        if (!button.getText().isEmpty() || board[row][col] != ' ') {
            return;
        }

        // Store the initial result as ongoing
        int gameId = db.storeResult("ongoing", boardToString()); // Store ongoing result only once

        // Human move
        board[row][col] = HUMAN_PLAYER;
        button.setText("O");
        button.setForeground(new Color(0, 255, 128));
		button.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 70));
        button.setBackground(new Color(0, 0, 0));

        db.storeMove(gameId, "human", row, col, boardToString()); // Log human move

        if (isWinner(board, HUMAN_PLAYER)) {
            isEndgame = true;
            db.storeResult("win", boardToString());
            JOptionPane.showMessageDialog(this, "Human wins!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            btnPlayAgain.setVisible(true);  // Show the Play Again button
            return;
        } else if (isDraw(board)) {
            isEndgame = true;
            db.storeResult("draw", boardToString());
            JOptionPane.showMessageDialog(this, "Draw", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            btnPlayAgain.setVisible(true);  // Show the Play Again button
            return;
        }


        // AI move
        int[] aiMove = minimax.bestMove(board, AI_PLAYER);
        if (aiMove != null) {
            board[aiMove[0]][aiMove[1]] = AI_PLAYER;
            buttons[aiMove[0]][aiMove[1]].setText("X");
            buttons[aiMove[0]][aiMove[1]].setForeground(new Color(255, 0, 0));
            buttons[aiMove[0]][aiMove[1]].setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 70));
            buttons[aiMove[0]][aiMove[1]].setBackground(new Color(0, 0, 0));

           db.storeMove(gameId, "AI", aiMove[0], aiMove[1], boardToString()); // Log AI move
        }

        if (isWinner(board, AI_PLAYER)) {
            isEndgame = true;
            db.storeResult("loss", boardToString());
            JOptionPane.showMessageDialog(this, "AI wins!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            btnPlayAgain.setVisible(true);  // Show the Play Again button
            return;
        } else if (isDraw(board)) {
            isEndgame = true;
            db.storeResult("draw", boardToString());
            JOptionPane.showMessageDialog(this, "Draw", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            btnPlayAgain.setVisible(true);  // Show the Play Again button
            return;
        }

    }

    public static boolean isWinner(char[][] board, char player) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) {
                return true;
            }
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) {
                return true;
            }
        }
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) {
            return true;
        }
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) {
            return true;
        }
        return false;
    }

    public static boolean isDraw(char[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void restartGame() {
        // Clear the board
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
                buttons[i][j].setText("");
            }
        }
        
        isEndgame = false;  // Reset game state
        moveNumber = 1;     // Reset move number
        btnPlayAgain.setVisible(false);  // Hide the play again button
        db.incrementGameCount();
    }
    
  
    // Convert the board array to a string
    private String boardToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sb.append(board[i][j] == ' ' ? '-' : board[i][j]);
                if (i != 2 || j != 2) sb.append(',');
            }
        }
        return sb.toString();
    }

    // Show game history from the database
    private void showGameHistory() {
    	db.moveNumber = 1;
        db.showGameHistory();
    
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
            db.clearDatabase();  // Call the method to clear the database
        }
    });
    }
}