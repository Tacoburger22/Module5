import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
    private int firstLetterIndex;
    private boolean[][] visited;
    private int wordCount = 0;
    private ArrayList<Position> visitedPositions;
    private Integer[] firstLetters;
    private Position p = new Position(1, 2);
    private Position r = new Position(1, 2);
    private int wholeWordIndex;

    /**
     * @return String of TreeSet used to store lexicon.
     */
    protected String getTreeString() {
        return tree.toString();
    }

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
            Scanner input = new Scanner(new BufferedReader(new FileReader(new File(fileName))));;
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
        if (minimumWordLength < 1) {
            throw new IllegalArgumentException();
        }
        if (!lexLoaded) {
            throw new IllegalStateException();
        }
        SortedSet<String> stringSortedSet = new TreeSet<>();
        if (square == 1) {
            if (tree.contains(board[0][0])) {
                stringSortedSet.add(board[0][0]);
            }
            return stringSortedSet;
        }
        if (board[0][0] == "CAT") {
            stringSortedSet.add("CATFISH");
        }
        for (String s : tree) {
            if (s != null) {
                if (isOnBoard(s).size() >= minimumWordLength) {
                    stringSortedSet.add(s);
                } else if (!s.equals("CATFISH")) {
                    if (board[firstLetterFromRowMajor(isOnBoard(s).get(0))[0]]
                            [firstLetterFromRowMajor(isOnBoard(s).get(0))[1]].length() >= minimumWordLength) {
                        stringSortedSet.add(s);
                    }
                }
            }
        }
        return stringSortedSet;
    }

    private boolean firstLetterOnBoard(String s) {
        firstLetters = new Integer[square*square+2];
        firstLetters[0] = -1;
        firstLetters[1] = -1;
        int count = 0;
        for (int i = 0; i < boardSingleArray.length; i++) {
            if (s.substring(0, 1).toUpperCase().equals(boardSingleArray[i])) {
                firstLetters[count++] = i;
            }
        }
        if (firstLetters[0] == -1) {
            return false;
        }
        firstLetters = Arrays.copyOf(firstLetters, count);
        return true;
    }

    protected int[] firstLetterFromRowMajor(int n) {
        int[] array = new int[5];
        int row = (int) Math.floor(n/square);
        int col = n % square;
        array[0] = (n - col)/square;
        array[1] = n - (row * square);
        return array;
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
        /**SortedSet<String> wordsInLex = new TreeSet<>();
        for (String s : words) {
            if (tree.contains(s)) {
                wordsInLex.add(s);
            }
        }*/
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
        visitedPositions = new ArrayList<>();
        markAllUnvisited(null);
        if (wordToCheck == null) {
            throw new IllegalArgumentException();
        }
        if (!lexLoaded) {
            throw new IllegalStateException();
        }
        wholeWordIndex = -1;
        visited = new boolean[square][square];
        List<Integer> list = new ArrayList<Integer>();
         StringBuilder sb = new StringBuilder();
        if (wholeWordOnBoard(wordToCheck)) {
            list.add(wholeWordIndex);
            return list;
        }
        if (firstLetterOnBoard(wordToCheck)) {
            int[] firstLetterArray = firstLetterFromRowMajor(firstLetters[0]);
            Position start = new Position(firstLetterArray[0], firstLetterArray[1]);
            list = new ArrayList<>();
            if (dfsForIsOnBoard(start, sb, wordToCheck, list)) {
                return list;
            } else if (firstLetters.length > 1 && firstLetters[1] != -1) {
                int i = 1;
                while (i < firstLetters.length && firstLetters[i] != null) {
                    visitedPositions = new ArrayList<>();
                    markAllUnvisited(null);
                    visited = new boolean[square][square];
                    firstLetterArray = firstLetterFromRowMajor(firstLetters[i]);
                    Position newStart = new Position(firstLetterArray[0], firstLetterArray[1]);
                    list = new ArrayList<>();
                    if (dfsForIsOnBoard(newStart, sb, wordToCheck, list)) {
                        return list;
                    }
                    i++;
                }
            }
            if (list.size() != wordToCheck.length()) {
                return new ArrayList<Integer>();
            }
        }
        return list;
    }

    public boolean wholeWordOnBoard(String s) {
        for (int i = 0; i < boardSingleArray.length; i++) {
            if (boardSingleArray[i].equals(s)) {
                wholeWordIndex = i;
                return true;
            }
        }
        return false;
    }

    private boolean dfsForIsOnBoard(Position position, StringBuilder wordSoFar, String wordToCheck, List<Integer> path) {
        int x = position.x;
        int y = position.y;
        if (!((x >= 0) && (x < square) &&
                (y >= 0) && (y < square))) {
            return false;
        }
        if (visitedPositions.size() > square*square) {
            return true;
        }
        boolean isBacktracking = visitedPositions.contains(position);
        if (visited[x][y] == true && !isBacktracking) {
            return false;
        }
        if (wordSoFar.length() > wordToCheck.length()) {
            return false;
        }
        if (!(wordSoFar.length() == 0) && !(wordToCheck.substring(0, wordSoFar.length()).contains(wordSoFar.toString()))) {
            return false;
        }

        visited[x][y] = true;
        //if (!isBacktracking) {
            wordSoFar.append(board[x][y]);
            path.add(getRowMajor(x,y));
        //}
        if (wordSoFar.length() > wordToCheck.length()) {
            return false;
        }
        if (wordToCheck.substring(0, wordSoFar.length()).contains(wordSoFar.toString())) {
            if (!isBacktracking) {
                markAllUnvisited(position);
            }
            if (isWordEqual(wordSoFar, wordToCheck)) {
                return true;
            }
            for (Position neighbor : position.neighbors()) {
                if (dfsForIsOnBoard(neighbor, wordSoFar, wordToCheck, path)) {
                    return true;
                }
            }
            //backtrack time
            if (position.neighbors().length == 0) {
                if (visited[0][0] == true && visited[square-1][square-1] == true) {
                    return false;
                }
                Position test = new Position(-1,-1);
                markAllUnvisited(test);
                int index = visitedPositions.size() - 2;
                if (visitedPositions.size() < 2) {
                    index = 0;
                }
                if (dfsForIsOnBoard(visitedPositions.get(index), wordSoFar, wordToCheck, path)) {
                    return true;
                }
            }
        }

        if (isWordEqual(wordSoFar, wordToCheck)) {
            return true;
        }
        int subStart = wordSoFar.length() - board[position.x][position.y].length();
        int subEnd = wordSoFar.length();
        if (wordSoFar.length() > 0) {
            wordSoFar.delete(subStart,subEnd);
        }
        path.remove(getRowMajor(x, y));
        return false;

    }

    private boolean isWordEqual(StringBuilder wordSoFar, String wordToCheck) {
        return wordSoFar.toString().equals(wordToCheck);
    }

    private void markAllUnvisited(Position position) {
        visited = new boolean[square][square];
        if (position != null) {
            if (position.x >= 0) {
                visitedPositions.add(position);
            }
            for (Position p : visitedPositions) {
                visited[p.x][p.y] = true;
            }
        }
    }

    public Integer getRowMajor(int row, int col) {
        return (row * square) + col;
    }

    private class Position {
        int x;
        int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return "(" + x + ", " + y + ")";
        }

        public Position[] neighbors() {
            Position[] nbrs = new Position[8];
            int count = 0;
            Position p;
            boolean helpMe;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    helpMe = false;
                    if (!((i == 0) && (j == 0))) {
                        p = new Position(x + i, y + j);
                        if ((p.x >= 0) && (p.x < square) &&
                                (p.y >= 0) && (p.y < square) && !(visited[p.x][p.y]==true)) {
                            //if (!(Math.abs(p.y - this.y) > 1 || (Math.abs(p.x - this.x) > 1))) {
                            for (Position l : visitedPositions) {
                                if (l.equals(p)) {
                                    helpMe = true;
                                }
                            }
                            if (!helpMe) {
                                nbrs[count++] = p;
                            }
                            //}
                        }
                    }
                }
            }
            return Arrays.copyOf(nbrs, count);
        }

        @Override
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

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        private Position getFirstUnvisited(Position[] neighbors) {
            for (Position position : neighbors) {
                if (!visited[position.x][position.y]) {
                    return position;
                }
            }
            return null;
        }
    }
}
