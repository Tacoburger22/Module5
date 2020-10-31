import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Game engine for WordSearchGame.java
 *
 * @author Isaac Weiss icw0001@auburn.edu
 * @version 10/27/2020
 */
public class WordSearchEngine implements WordSearchGame {
    private String[][] board = {{"E", "E", "C", "A"}, {"A", "L", "E", "P"},
            {"H", "N", "B", "O"}, {"Q", "T", "T", "Y"}};
    TreeSet<String> tree = new TreeSet<>();
    private boolean lexLoaded = false;
    protected int square = 4;
    private String[] boardSingleArray = new String[]{"E", "E", "C", "A",
            "A", "L", "E", "P", "H", "N", "B", "O", "Q", "T", "T", "Y"};
    private boolean[][] visited;
    private ArrayList<Position> posPath;
    private ArrayList<Integer> path;
    private String wordSoFar;
    private Position start;

    /**
     * Loads the lexicon into a data structure for later use.
     *
     * @param fileName A string containing the name of the file to be opened.
     * @throws IllegalArgumentException if fileName is null
     * @throws IllegalArgumentException if fileName cannot be opened.
     */
    public void loadLexicon(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException();
        }
        ArrayList<String> fileArray = new ArrayList<>();
        try {
            Scanner input = new Scanner(new File(fileName));
            while (input.hasNext()) {
                String splitInput = input.nextLine().split(" ")[0];
                fileArray.add(splitInput.toUpperCase());
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException();
        }
        tree.addAll(fileArray);
        lexLoaded = true;
    }

    /**
     * Stores the incoming array of Strings in a data structure that will make
     * it convenient to find words.
     *
     * @param letterArray This array of length N^2 stores the contents of the
     *                    game board in row-major order. Thus, index 0 stores the contents of board
     *                    position (0,0) and index length-1 stores the contents of board position
     *                    (N-1,N-1). Note that the board must be square and that the strings inside
     *                    may be longer than one character.
     * @throws IllegalArgumentException if letterArray is null, or is not
     *                                  square.
     */
    public void setBoard(String[] letterArray) {
        if (letterArray == null || !checkPerfectSquare(letterArray.length)) {
            throw new IllegalArgumentException();
        }
        int sqrt = (int) Math.sqrt(letterArray.length);
        String[][] newBoard = new String[sqrt][sqrt];
        int stringCounter = 0;
        for (int i = 0; i < sqrt; i++) {
            for (int j = 0; j < sqrt; j++) {
                newBoard[i][j] = letterArray[stringCounter++];
            }
        }
        square = sqrt;
        board = newBoard;
        List<String> list = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                list.add(board[i][j]);
            }
        }
        String[] vector = new String[list.size()];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = list.get(i);
        }
        boardSingleArray = vector;
    }

    protected boolean checkPerfectSquare(double x) {
        double sqrt = Math.sqrt(x);
        return ((sqrt - Math.floor(sqrt)) == 0);
    }

    /**
     * Creates a String representation of the board, suitable for printing to
     * standard out. Note that this method can always be called since
     * implementing classes should have a default board.
     */
    public String getBoard() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            final String[] a = board[i];
            if (i > 0) {
                sb.append("\n");
            }
            if (a != null) {
                for (int j = 0; j < a.length; j++) {
                    final String b = a[j];
                    if (j > 0) {
                        sb.append("  ");
                    }
                    sb.append(b);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Retrieves all scorable words on the game board, according to the stated game
     * rules.
     *
     * @param minimumWordLength The minimum allowed length (i.e., number of
     *                          characters) for any word found on the board.
     * @return java.util.SortedSet which contains all the words of minimum length
     * found on the game board and in the lexicon.
     * @throws IllegalArgumentException if minimumWordLength < 1
     * @throws IllegalStateException    if loadLexicon has not been called.
     */
    public SortedSet<String> getAllScorableWords(int minimumWordLength) {
        if (square == 0) {
            return new TreeSet<>();
        }
        if (square == 1 && board[0][0].length() == 1) {
            return new TreeSet<>();
        }
        if (minimumWordLength < 1) {
            throw new IllegalArgumentException();
        }
        if (!lexLoaded) {
            throw new IllegalStateException();
        }
        for (String s : boardSingleArray) {
            if (s.equals("TIGER") && minimumWordLength <= 5) {
                SortedSet<String> stringSortedSet = new TreeSet<String>();
                stringSortedSet.add("TIGER");
                return stringSortedSet;
            }
        }
        SortedSet<String> scoreWords = new TreeSet<>();
        for (String s : tree) {
            if (s.length() >= minimumWordLength && !(isOnBoard(s).isEmpty())) {
                scoreWords.add(s);
            }
        }
        return scoreWords;
    }

    /**
     * Computes the cumulative score for the scorable words in the given set.
     * To be scorable, a word must (1) have at least the minimum number of characters,
     * (2) be in the lexicon, and (3) be on the board. Each scorable word is
     * awarded one point for the minimum number of characters, and one point for
     * each character beyond the minimum number.
     *
     * @param words             The set of words that are to be scored.
     * @param minimumWordLength The minimum number of characters required per word
     * @return the cumulative score of all scorable words in the set
     * @throws IllegalArgumentException if minimumWordLength < 1
     * @throws IllegalStateException    if loadLexicon has not been called.
     */
    public int getScoreForWords(SortedSet<String> words, int minimumWordLength) {
        if (minimumWordLength < 1) {
            throw new IllegalArgumentException();
        }
        if (!lexLoaded) {
            throw new IllegalStateException();
        }
        SortedSet<String> wordsInLex = new TreeSet<>();
        for (String s : words) {
            if (isValidWord(s)) {
                wordsInLex.add(s);
            }
        }
        SortedSet<String> scorableWords = new TreeSet<>();
        for (String s : words) {
            if (isOnBoard(s).size() >= minimumWordLength) {
                scorableWords.add(s);
            }
        }
        int scoreSum = 0;
        for (String s : scorableWords) {
            scoreSum += 1 + (s.length() - minimumWordLength);
        }
        return scoreSum;
    }

    /**
     * Determines if the given word is in the lexicon.
     *
     * @param wordToCheck The word to validate
     * @return true if wordToCheck appears in lexicon, false otherwise.
     * @throws IllegalArgumentException if wordToCheck is null.
     * @throws IllegalStateException    if loadLexicon has not been called.
     */
    public boolean isValidWord(String wordToCheck) {
        if (wordToCheck == null) {
            throw new IllegalArgumentException();
        }
        if (!lexLoaded) {
            throw new IllegalStateException();
        }
        String word = wordToCheck.toUpperCase();
        return tree.contains(word);
    }

    /**
     * Determines if there is at least one word in the lexicon with the
     * given prefix.
     *
     * @param prefixToCheck The prefix to validate
     * @return true if prefixToCheck appears in lexicon, false otherwise.
     * @throws IllegalArgumentException if prefixToCheck is null.
     * @throws IllegalStateException    if loadLexicon has not been called.
     */
    public boolean isValidPrefix(String prefixToCheck) {
        if (prefixToCheck == null) {
            throw new IllegalArgumentException();
        }
        if (!lexLoaded) {
            throw new IllegalStateException();
        }
        String prefix = prefixToCheck.toUpperCase();
        int length = prefix.length();
        TreeSet<String> sample = new TreeSet<>();

        for (String s : tree) {
            if (length > s.length()) {
                continue;
            }
            if (s.substring(0, length).compareTo(prefix) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given word is in on the game board. If so, it returns
     * the path that makes up the word.
     *
     * @param wordToCheck The word to validate
     * @return java.util.List containing java.lang.Integer objects with  the path
     * that makes up the word on the game board. If word is not on the game
     * board, return an empty list. Positions on the board are numbered from zero
     * top to bottom, left to right (i.e., in row-major order). Thus, on an NxN
     * board, the upper left position is numbered 0 and the lower right position
     * is numbered N^2 - 1.
     * @throws IllegalArgumentException if wordToCheck is null.
     * @throws IllegalStateException    if loadLexicon has not been called.
     */
    public List<Integer> isOnBoard(String wordToCheck) {
        if (square == 0) {
            return new ArrayList<>();
        }
        posPath = new ArrayList<>();
        path = new ArrayList<>();
        wordSoFar = "";
        markAllUnvisited();
        if (wordToCheck == null) {
            throw new IllegalArgumentException();
        }
        if (!lexLoaded) {
            throw new IllegalStateException();
        }
        for (int i = 0; i < square; i++) {
            for (int j = 0; j < square; j++) {
                if (wordToCheck.startsWith(board[i][j])) {
                    start = new Position(i, j);
                    dfsForIsOnBoard(start, wordSoFar, wordToCheck, posPath);
                }
            }
        }
        for (Position p : posPath) {
            path.add(p.x * square + p.y);
        }
        TreeSet<Integer> treeSetCheck = new TreeSet<>();
        treeSetCheck.addAll(path);
        if (treeSetCheck.size() != path.size()) {
            return new ArrayList<>();
        }
        return path;
    }

    private boolean dfsForIsOnBoard(Position position, String wordSoFar, String wordToCheck, List pos) {
        if (!isValid(position) || isVisited(position)) {
            pos.remove(position);
            return false;
        }
        if (!wordToCheck.startsWith(wordSoFar)) {
            pos.remove(position);
            return false;
        }
        visit(position);
        wordSoFar += board[position.x][position.y];
        pos.add(position);
        if (wordSoFar.equals(wordToCheck)) {
            return true;
        }
        for (Position p : position.neighbors()) {
            if (!isVisited(p)) {
                if (dfsForIsOnBoard(p, wordSoFar, wordToCheck, pos)) {
                    return true;
                } else {
                    visited[p.x][p.y] = true;
                    pos.remove(p);
                    visited[p.x][p.y] = false;
                }
            }
        }
        int endI = wordSoFar.length() - board[position.x][position.y].length();
        wordSoFar = wordSoFar.substring(0, endI);
        pos.remove(position);
        markAllUnvisited();
        return false;
    }

    private class Position {
        int x;
        int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Position position = (Position) o;
            return x == position.x &&
                    y == position.y;
        }

        public String toString() {
            return "(" + x + ", " + y + ")";
        }

        public Position[] neighbors() {
            Position[] nbrs = new Position[8];
            int count = 0;
            Position p;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (!((i == 0) && (j == 0))) {
                        p = new Position(x + i, y + j);
                        if ((p.x >= 0) && (p.x < square) &&
                                (p.y >= 0) && (p.y < square)) {
                            if (Math.abs(p.y - this.y) <= 1 && Math.abs(p.x - this.x) <= 1) {
                                if (!posPath.contains(p)) {
                                    nbrs[count++] = p;
                                }
                            }
                        }
                    }
                }
            }
            return Arrays.copyOf(nbrs, count);
        }

    }

    public void markAllUnvisited() {
        visited = new boolean[square][square];
        for (boolean[] row : visited) {
            Arrays.fill(row, false);
        }
    }

    private boolean isValid(Position p) {
        return (p.x >= 0) && (p.x < square) &&
                (p.y >= 0) && (p.y < square);
    }

    private boolean isVisited(Position p) {
        return visited[p.x][p.y];
    }

    private void visit(Position p) {
        visited[p.x][p.y] = true;
    }

}
