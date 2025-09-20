package com.example.ooxx.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ooxx.R;

import java.util.Locale;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class GameManager {
    private Context context;
    private int boardSize = 6; // 固定为6x6棋盘
    private int[][] board;
    private ImageView[][] cellViews;
    private int currentPlayer = 1;
    private boolean isGameOver = false;
    private int score = 0;
    private boolean isMultiplayer = false;
    private boolean isBluetoothGame = false;

    private TextView tvCurrentPlayer;
    private TextView tvResult;
    private GridLayout gridBoard;

    private OnGameEventListener gameEventListener;

    private Handler handler = new Handler(Looper.getMainLooper());

    // 存储已存在的行和列模式，用于确保唯一性
    private Set<String> rowPatterns = new HashSet<>();
    private Set<String> colPatterns = new HashSet<>();

    // 悔棋功能相关
    private Stack<Move> moveHistory = new Stack<>();
    private boolean isSwitchingPiece = false;

    // 标记初始谜题中已有的棋子（不可切换）
    private boolean[][] initialPieces;

    public class Move {
        public int row;
        public int col;
        public int previousState;
        public int newState;
        public int player;

        public Move(int row, int col, int previousState, int newState, int player) {
            this.row = row;
            this.col = col;
            this.previousState = previousState;
            this.newState = newState;
            this.player = player;
        }
    }

    public interface OnGameEventListener {
        void onGameWin(String result, int score);
        void onGameDraw(String result, int score);
        void onPlayerChanged(int player);
        void onCellClicked(int row, int col, int player);
        void onUndoAvailable(boolean available);
    }

    public GameManager(Context context, GridLayout gridBoard,
                       TextView tvCurrentPlayer, TextView tvResult,
                       OnGameEventListener listener, int boardSize) {  // 添加 boardSize 参数
        this.context = context;
        this.gridBoard = gridBoard;
        this.tvCurrentPlayer = tvCurrentPlayer;
        this.tvResult = tvResult;
        this.gameEventListener = listener;
        this.boardSize = boardSize;  // 设置棋盘大小
        this.board = new int[boardSize][boardSize];
        this.cellViews = new ImageView[boardSize][boardSize];
        this.initialPieces = new boolean[boardSize][boardSize]; // 初始化初始棋子标记数组
        initBoardView();
    }

    /**
     * 设置游戏模式
     */
    public void setGameMode(String mode) {
        this.isMultiplayer = "vs".equals(mode) || "bluetooth".equals(mode) || "online".equals(mode);
        this.isBluetoothGame = "bluetooth".equals(mode);
    }

    private void initBoardView() {
        gridBoard.removeAllViews();
        gridBoard.setRowCount(boardSize);
        gridBoard.setColumnCount(boardSize);

        // 根据棋盘大小调整单元格大小
        int cellSize = calculateCellSize();

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                ImageView cell = new ImageView(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                params.setMargins(2, 2, 2, 2); // 添加边距

                cell.setLayoutParams(params);
                cell.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                cell.setImageResource(R.drawable.cell_empty);
                cell.setBackgroundResource(R.drawable.cell_bg);

                final int finalRow = row;
                final int finalCol = col;
                cell.setOnClickListener(v -> onCellClick(finalRow, finalCol));

                gridBoard.addView(cell);
                cellViews[row][col] = cell;
                board[row][col] = 0;
                initialPieces[row][col] = false; // 初始化为false
            }
        }

        updatePlayerDisplay();
        updateUndoButtonState();
    }

    private int calculateCellSize() {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        // 计算可用空间（考虑边距和顶部/底部控件）
        int horizontalPadding = 32; // 左右边距
        int verticalPadding = 200; // 上下边距（考虑顶部信息栏和底部按钮区域）

        int availableWidth = screenWidth - horizontalPadding;
        int availableHeight = screenHeight - verticalPadding;

        // 取宽度和高度的较小值，确保棋盘在屏幕上完全可见
        int boardSizePx = Math.min(availableWidth, availableHeight);

        // 计算每个单元格的大小，减去边距
        // 减小单元格大小以适应6x6棋盘
        return (boardSizePx / boardSize) - 11; // 从-4改为-6，进一步减小单元格大小
    }

    public void onCellClick(int row, int col) {
        if (isGameOver) {
            return;
        }

        // 如果点击的是初始棋子，则不允许操作
        if (initialPieces[row][col]) {
            Toast.makeText(context, "初始棋子不可修改", Toast.LENGTH_SHORT).show();
            return;
        }

        // 如果点击的是已有棋子（非初始），则切换棋子类型
        if (board[row][col] != 0) {
            switchPiece(row, col);
            return;
        }

        // 处理玩家移动
        handlePlayerMove(row, col, currentPlayer);
    }

    /**
     * 切换已有棋子的类型
     */
    private void switchPiece(int row, int col) {
        // 再次检查是否是初始棋子（安全措施）
        if (initialPieces[row][col]) {
            Toast.makeText(context, "初始棋子不可修改", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentPiece = board[row][col];
        int newPiece = (currentPiece == 1) ? 2 : 1;

        // 记录移动历史
        moveHistory.push(new Move(row, col, currentPiece, newPiece, currentPlayer));
        updateUndoButtonState();

        // 更新棋盘
        board[row][col] = newPiece;
        updateCellView(row, col, newPiece);

        // 检查游戏状态
        checkGameStatus();

        // 不切换玩家，只更新显示
        updatePlayerDisplay();
    }

    /**
     * 悔棋功能
     */
    public void undoLastMove() {
        if (moveHistory.isEmpty() || isGameOver) {
            return;
        }

        Move lastMove = moveHistory.pop();
        board[lastMove.row][lastMove.col] = lastMove.previousState;
        updateCellView(lastMove.row, lastMove.col, lastMove.previousState);;

        updateUndoButtonState();

        // 检查游戏状态
        checkGameStatus();
    }

    /**
     * 处理远程移动（蓝牙或网络）
     */
    public void onRemoteMove(int row, int col, int player) {
        handler.post(() -> {
            if (!isGameOver && board[row][col] == 0) {
                handlePlayerMove(row, col, player);
            }
        });
    }

    private void handlePlayerMove(int row, int col, int player) {
        // 先检查移动是否合法
        if (!isMoveValid(row, col, player)) {
            Toast.makeText(context, "此移动违反游戏规则！", Toast.LENGTH_SHORT).show();
            return;
        }

        // 记录移动历史
        moveHistory.push(new Move(row, col, 0, player, currentPlayer));
        updateUndoButtonState();

        board[row][col] = player;
        updateCellView(row, col, player);

        if (gameEventListener != null) {
            gameEventListener.onCellClicked(row, col, player);
        }

        checkGameStatus();

        if (!isGameOver) {
            switchPlayer();
        }
    }

    private boolean isMoveValid(int row, int col, int player) {
        // 临时放置棋子
        board[row][col] = player;

        // 检查行是否有超过两个连续的相同符号
        if (hasThreeInRow(row) || hasThreeInColumn(col)) {
            board[row][col] = 0; // 恢复
            return false;
        }

        // 检查行和列中X和O的数量是否平衡（不能超过一半）
        if (!isRowColumnBalanced(row, col)) {
            board[row][col] = 0; // 恢复
            return false;
        }

        board[row][col] = 0; // 恢复
        return true;
    }

    private boolean hasThreeInRow(int row) {
        int count = 1;
        int lastSymbol = board[row][0];

        for (int col = 1; col < boardSize; col++) {
            if (board[row][col] == lastSymbol && board[row][col] != 0) {
                count++;
                if (count >= 3) {
                    return true;
                }
            } else {
                count = 1;
                lastSymbol = board[row][col];
            }
        }
        return false;
    }

    private boolean hasThreeInColumn(int col) {
        int count = 1;
        int lastSymbol = board[0][col];

        for (int row = 1; row < boardSize; row++) {
            if (board[row][col] == lastSymbol && board[row][col] != 0) {
                count++;
                if (count >= 3) {
                    return true;
                }
            } else {
                count = 1;
                lastSymbol = board[row][col];
            }
        }
        return false;
    }

    private boolean isRowColumnBalanced(int changedRow, int changedCol) {
        // 检查改变的行
        int xCountRow = 0, oCountRow = 0;
        for (int col = 0; col < boardSize; col++) {
            if (board[changedRow][col] == 1) xCountRow++;
            else if (board[changedRow][col] == 2) oCountRow++;
        }
        if (xCountRow > boardSize / 2 || oCountRow > boardSize / 2) {
            return false;
        }

        // 检查改变的列
        int xCountCol = 0, oCountCol = 0;
        for (int row = 0; row < boardSize; row++) {
            if (board[row][changedCol] == 1) xCountCol++;
            else if (board[row][changedCol] == 2) oCountCol++;
        }
        if (xCountCol > boardSize / 2 || oCountCol > boardSize / 2) {
            return false;
        }

        return true;
    }

    private void updateCellView(int row, int col, int type) {
        int resId;
        if (type == 1) {
            resId = R.drawable.cell_x;
        } else if (type == 2) {
            resId = R.drawable.cell_o;
        } else {
            resId = R.drawable.cell_empty;
        }
        cellViews[row][col].setImageResource(resId);
    }

    private void checkGameStatus() {
        // 检查是否所有格子都已填充
        boolean isBoardFull = true;
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (board[row][col] == 0) {
                    isBoardFull = false;
                    break;
                }
            }
            if (!isBoardFull) break;
        }

        if (!isBoardFull) {
            return; // 棋盘还没填满，继续游戏
        }

        // 检查所有规则是否满足
        if (checkAllRules()) {
            isGameOver = true;
            String result = "谜题解决！";
            int bonusScore = calculateWinBonus();
            score += bonusScore;

            if (gameEventListener != null) {
                gameEventListener.onGameWin(result, score);
            }
        } else {
            isGameOver = true;
            String result = "违反规则，游戏失败";
            if (gameEventListener != null) {
                gameEventListener.onGameDraw(result, score);
            }
        }
    }

    private boolean checkAllRules() {
        // 规则1: 检查每行每列是否有超过两个连续的X或O
        for (int i = 0; i < boardSize; i++) {
            if (hasThreeInRow(i) || hasThreeInColumn(i)) {
                return false;
            }
        }

        // 规则2: 检查每行每列中X和O的数量是否相同
        for (int i = 0; i < boardSize; i++) {
            int xCountRow = 0, oCountRow = 0;
            int xCountCol = 0, oCountCol = 0;

            for (int j = 0; j < boardSize; j++) {
                // 统计行
                if (board[i][j] == 1) xCountRow++;
                else if (board[i][j] == 2) oCountRow++;

                // 统计列
                if (board[j][i] == 1) xCountCol++;
                else if (board[j][i] == 2) oCountCol++;
            }

            if (xCountRow != oCountRow || xCountCol != oCountCol) {
                return false;
            }
        }

        // 规则3: 检查每行每列是否唯一
        rowPatterns.clear();
        colPatterns.clear();

        for (int i = 0; i < boardSize; i++) {
            StringBuilder rowPattern = new StringBuilder();
            StringBuilder colPattern = new StringBuilder();

            for (int j = 0; j < boardSize; j++) {
                rowPattern.append(board[i][j]);
                colPattern.append(board[j][i]);
            }

            String rowStr = rowPattern.toString();
            String colStr = colPattern.toString();

            if (rowPatterns.contains(rowStr) || colPatterns.contains(colStr)) {
                return false;
            }

            rowPatterns.add(rowStr);
            colPatterns.add(colStr);
        }

        return true;
    }

    private int calculateWinBonus() {
        return 200; // 6x6棋盘奖励200分
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        updatePlayerDisplay();

        if (gameEventListener != null) {
            gameEventListener.onPlayerChanged(currentPlayer);
        }
    }

    private void updatePlayerDisplay() {
        if (tvCurrentPlayer != null) {
            String playerText;
            if (isMultiplayer) {
                playerText = String.format(Locale.getDefault(), "当前玩家：%s",
                        (currentPlayer == 1) ? "X" : "O");
            } else {
                playerText = (currentPlayer == 1) ? "放置 X" : "放置 O";
            }
            tvCurrentPlayer.setText(playerText);
        }
    }

    private void updateUndoButtonState() {
        if (gameEventListener != null) {
            gameEventListener.onUndoAvailable(!moveHistory.isEmpty());
        }
    }

    public void resetGame() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                cellViews[row][col].setImageResource(R.drawable.cell_empty);
                board[row][col] = 0;
                initialPieces[row][col] = false; // 重置初始棋子标记
            }
        }
        currentPlayer = 1;
        isGameOver = false;
        score = 0;
        rowPatterns.clear();
        colPatterns.clear();
        moveHistory.clear();
        updatePlayerDisplay();
        updateUndoButtonState();

        if (tvResult != null) {
            tvResult.setText("游戏进行中...");
        }
    }

    public void initBoardFromServer(String initialBoard) {
        if (initialBoard != null && initialBoard.length() == boardSize * boardSize) {
            resetGame(); // 先重置棋盘

            // 初始化 initialPieces
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    initialPieces[row][col] = false; // 先全部设为false
                }
            }

            for (int i = 0; i < initialBoard.length(); i++) {
                int row = i / boardSize;
                int col = i % boardSize;
                char cellChar = initialBoard.charAt(i);

                if (cellChar == '1' || cellChar == '2') {
                    int type = Character.getNumericValue(cellChar);
                    board[row][col] = type;
                    initialPieces[row][col] = true; // 标记为初始棋子
                    updateCellView(row, col, type);
                }
            }
            updatePlayerDisplay();
            updateUndoButtonState();
        } else {
            Toast.makeText(context, "无效的棋盘数据", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取当前棋盘状态字符串
     */
    public String getBoardState() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                sb.append(board[row][col]);
            }
        }
        return sb.toString();
    }

    /**
     * 获取可用的移动位置
     */
    public java.util.List<int[]> getAvailableMoves() {
        java.util.List<int[]> moves = new java.util.ArrayList<>();
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (board[row][col] == 0) {
                    moves.add(new int[]{row, col});
                }
            }
        }
        return moves;
    }

    // Getter 方法
    public int getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return isGameOver; }
    public int getScore() { return score; }
    public int getBoardSize() { return boardSize; }
    public int[][] getBoard() { return board; }
    public boolean canUndo() { return !moveHistory.isEmpty(); }

    // Setter 方法
    public void setScore(int score) { this.score = score; }
    public void setCurrentPlayer(int player) {
        this.currentPlayer = player;
        updatePlayerDisplay();
    }
}
