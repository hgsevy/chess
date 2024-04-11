package chess;

//import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public record ChessPosition(int row, int col){
    @Override
    public String toString() {
        return convertColtoLetter()+row;
    }

    private String convertColtoLetter(){
        if (col == 1){
            return "A";
        } else if (col == 2){
            return "B";
        } else if (col == 3){
            return "C";
        } else if (col == 4){
            return "D";
        } else if (col == 5){
            return "E";
        } else if (col == 6){
            return "F";
        } else if (col == 7){
            return "G";
        } else{
            return "H";
        }
    }
}
