package src.main.java;

public class GameLogic {
    private int moveNumber;
    private ChessColor turn;
    private Board board;

    public GameLogic(int moveNumber, ChessColor turn, Board board) {
        this.moveNumber = moveNumber;
        this.turn = turn;
        this.board = board;
    }

    public GameLogic() {
        this.moveNumber = 0;
        this.turn = ChessColor.WHITE;
        this.board = new Board();
    }

    public ChessColor getTurn() {
        return turn;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isMoveValid(Position from, Position to) {
        // return false; // don't do anything
        return true;
    }

    public void updateState(Position from, Position to) {
        moveNumber++;
        if (turn == ChessColor.WHITE)
            turn = ChessColor.BLACK;
        else
            turn = ChessColor.WHITE;
        this.board.move(from, to);
    }
}
