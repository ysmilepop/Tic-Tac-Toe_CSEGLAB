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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
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
        setBounds(100, 100, 600, 400); // Adjust size if necessary
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout());

        // Initialize the board and buttons
        initializeBoardAndButtons();

        // Initialize the database and Minimax
        db = new Database();
        db.initialize();
        minimax = new Minimax();

        // Add History button at the bottom
        JButton btnHistory = new JButton("History");
        btnHistory.setFont(new Font("Tahoma", Font.PLAIN, 16));
        btnHistory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showGameHistory();
            }
        });
        contentPane.add(btnHistory, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void initializeBoardAndButtons() {
        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3));
        contentPane.add(boardPanel, BorderLayout.CENTER);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(new Font("Tahoma", Font.PLAIN, 69));
                final int row = i;
                final int col = j;
                buttons[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (isEndgame) {
                            return;
                        } else {
                            setMove(buttons[row][col], row, col);
                        }
                    }
                });
                boardPanel.add(buttons[i][j]);
            }
        }
    }

    public void setMove(JButton button, int row, int col) {
        if (!button.getText().isEmpty() || board[row][col] != ' ') {
            return;
        }

        // Human move
        board[row][col] = HUMAN_PLAYER;
        button.setText("O");
        button.setBackground(new Color(0, 255, 0));

        if (isWinner(board, HUMAN_PLAYER)) {
            System.out.println("Human wins!");
            isEndgame = true;
            db.storeResult("win", boardToString());
            JOptionPane.showMessageDialog(this, "Human wins!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            return;
        } else if (isDraw(board)) {
            System.out.println("It's a draw!");
            isEndgame = true;
            db.storeResult("draw", boardToString());
            return;
        }

        // AI move
        int[] aiMove = minimax.bestMove(board, AI_PLAYER);
        if (aiMove != null) {
            board[aiMove[0]][aiMove[1]] = AI_PLAYER;
            buttons[aiMove[0]][aiMove[1]].setText("X");
            buttons[aiMove[0]][aiMove[1]].setBackground(new Color(255, 0, 0));
        }

        if (isWinner(board, AI_PLAYER)) {
            System.out.println("AI wins!");
            isEndgame = true;
            db.storeResult("loss", boardToString());
            JOptionPane.showMessageDialog(this, "AI wins!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else if (isDraw(board)) {
            System.out.println("It's a draw!");
            isEndgame = true;
            db.storeResult("draw", boardToString());
            JOptionPane.showMessageDialog(this, "Draw!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
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
        db.showGameHistory();
    }
}
