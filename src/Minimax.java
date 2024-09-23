public class Minimax {

    public int minimax(char[][] board, boolean isMaximizing, char currentPlayer) {
        char opponent = (currentPlayer == 'X') ? 'O' : 'X';
        if (TicTacToe.isWinner(board, currentPlayer)) {
            return isMaximizing ? 1 : -1;
        }
        if (TicTacToe.isDraw(board)) {
            return 0;
        }
        
        //taena

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == ' ') {
                        board[i][j] = currentPlayer;
                        int score = minimax(board, false, opponent);
                        board[i][j] = ' ';
                        bestScore = Math.max(score, bestScore);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == ' ') {
                        board[i][j] = currentPlayer;
                        int score = minimax(board, true, opponent);
                        board[i][j] = ' ';
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
            return bestScore;
        }
    }

    public int[] bestMove(char[][] board, char aiPlayer) {
        int bestScore = Integer.MIN_VALUE;
        int[] move = new int[2];
        char opponent = (aiPlayer == 'X') ? 'O' : 'X';

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    board[i][j] = aiPlayer;
                    int score = minimax(board, false, opponent);
                    board[i][j] = ' ';
                    if (score > bestScore) {
                        bestScore = score;
                        move[0] = i;
                        move[1] = j;
                    }
                }
            }
        }

        return bestScore == Integer.MIN_VALUE ? null : move;
    }
}
