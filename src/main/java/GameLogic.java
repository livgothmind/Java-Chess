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
        // CHECK ALL THE CONDITIONS
        Piece fromPiece = this.board.getPieceAt(from);
        Piece toPiece = this.board.getPieceAt(to);

        // Check if there's no piece at the source position
        if (fromPiece == null) {
            return false;
        }

        // Check if there's a piece at the destination and it's the same color
        if (toPiece != null && fromPiece.getColor() == toPiece.getColor()) {
            return false;
        }

        // Check if 'to' is in the valid positions of 'from'
        if (!fromPiece.getValidPositions().contains(to)) {
            return false;
        }

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

    public boolean move(Position from, Position to) {
        if (this.isMoveValid(from, to)) {
            updateState(from, to);
            return true;
        }
        return false;
    }
}
