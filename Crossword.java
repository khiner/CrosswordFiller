import java.util.Iterator;
import java.util.Arrays;
import java.util.Stack;
import java.util.ArrayList;

public class Crossword {
	int depth;
	boolean stop;
	boolean done;
	boolean surrounded;
	char[][] crossword;
	char[][] startState;
	ArrayList<int[]> acrossBlackSquares;
	ArrayList<int[]> downBlackSquares;
	ArrayList<WordInfo> unfinishedWords;
	ArrayList<WordInfo> finishedWords;
	ArrayList<WordInfo> blankWords;
	ArrayList<WordInfo> nextWords;
	Stack<WordInfo> lastWords;
	Stack<WordInfo> tempLastWords;
	Stack<node> nodeList;
	Dictionary dictionary;

	public Crossword(final char[][] crossword) throws Exception {
		this.dictionary = new Dictionary();
		this.stop = false;
		this.done = false;
		this.crossword = this.copyChars(crossword);
		this.startState = this.copyChars(crossword);
		this.getBlackSquares();
		this.lastWords = new Stack<WordInfo>();
		this.tempLastWords = new Stack<WordInfo>();
		this.updateWordLists();
		this.depth = this.blankWords.size() + this.finishedWords.size() + this.unfinishedWords.size();
	}

	public int size() {
		return this.crossword.length;
	}

	public int getDepth() {
		return this.depth;
	}

	public char[][] getChars() {
		return this.crossword;
	}

	public ArrayList<WordInfo> getFinished() {
		return this.finishedWords;
	}

	public ArrayList<WordInfo> getUnfinished() {
		return this.unfinishedWords;
	}

	private void getBlackSquares() {
		this.acrossBlackSquares = new ArrayList<int[]>();
		this.downBlackSquares = new ArrayList<int[]>();
		for (int x = 0; x < this.crossword.length - 1; ++x) {
			for (int y = -1; y < this.crossword.length; ++y) {
				if (y == -1 || this.crossword[x][y] == '\n') {
					this.acrossBlackSquares.add(new int[] { x, y });
				}
			}
		}
		for (int y2 = 0; y2 < this.crossword.length - 1; ++y2) {
			for (int x2 = -1; x2 < this.crossword.length; ++x2) {
				if (x2 == -1 || this.crossword[x2][y2] == '\n') {
					this.downBlackSquares.add(new int[] { x2, y2 });
				}
			}
		}
	}

	private void updateWordLists() {
		this.unfinishedWords = new ArrayList<WordInfo>();
		this.finishedWords = new ArrayList<WordInfo>();
		this.blankWords = new ArrayList<WordInfo>();
		this.nextWords = new ArrayList<WordInfo>();
		this.surrounded = false;
		for (int x = 0; x < this.acrossBlackSquares.size() - 1; ++x) {
			final int blackRow = this.acrossBlackSquares.get(x)[0];
			final int blackColumn = this.acrossBlackSquares.get(x)[1];
			final int nextBlackRow = this.acrossBlackSquares.get(x + 1)[0];
			final int nextBlackColumn = this.acrossBlackSquares.get(x + 1)[1];
			if (blackRow == nextBlackRow && blackColumn + 1 < nextBlackColumn) {
				final char[] word = Arrays.copyOfRange(this.crossword[blackRow], blackColumn + 1, nextBlackColumn);
				final WordInfo wi = new WordInfo(word, blackRow, blackColumn + 1, 'a');
				int spaceCount = 0;
				for (int j = 0; j < word.length; ++j) {
					if (word[j] == ' ') {
						++spaceCount;
					}
				}
				if (spaceCount == 0) {
					this.finishedWords.add(wi);
				} else if (spaceCount == word.length) {
					this.blankWords.add(wi);
				} else {
					this.unfinishedWords.add(wi);
				}
			}
		}
		for (int y = 0; y < this.downBlackSquares.size() - 1; ++y) {
			final int blackRow = this.downBlackSquares.get(y)[0];
			final int blackColumn = this.downBlackSquares.get(y)[1];
			final int nextBlackRow = this.downBlackSquares.get(y + 1)[0];
			final int nextBlackColumn = this.downBlackSquares.get(y + 1)[1];
			if (blackColumn == nextBlackColumn && blackRow + 1 < nextBlackRow) {
				final char[] word = new char[nextBlackRow - (blackRow + 1)];
				for (int i = 0; i < word.length; ++i) {
					word[i] = this.crossword[blackRow + 1 + i][blackColumn];
				}
				final WordInfo wi = new WordInfo(word, blackRow + 1, blackColumn, 'd');
				int spaceCount = 0;
				for (int j = 0; j < word.length; ++j) {
					if (word[j] == ' ') {
						++spaceCount;
					}
				}
				if (spaceCount == 0) {
					this.finishedWords.add(wi);
				} else if (spaceCount == word.length) {
					this.blankWords.add(wi);
				} else {
					this.unfinishedWords.add(wi);
				}
			}
		}
		this.getNextWords();
	}

	private void getNextWords() {
		this.tempLastWords = (Stack) this.lastWords.clone();
		while (!this.tempLastWords.isEmpty() && this.nextWords.isEmpty()) {
			for (final WordInfo uf : this.unfinishedWords) {
				if (this.intersects(uf, this.tempLastWords.peek())) {
					this.nextWords.add(uf);
				}
			}
			if (this.nextWords.isEmpty()) {
				this.tempLastWords.pop();
				this.surrounded = true;
			}
		}
	}

	private WordInfo getNextWord() {
		WordInfo nextWord = new WordInfo();
		int leastMatches = Integer.MAX_VALUE;
		if (!this.nextWords.isEmpty()) {
			for (final WordInfo wi : this.nextWords) {
				final int matches = this.dictionary.howManyMatches(wi.word);
				if (matches < leastMatches) {
					leastMatches = matches;
					nextWord = wi;
				}
			}
		} else if (!this.unfinishedWords.isEmpty()) {
			for (final WordInfo wi : this.unfinishedWords) {
				if (wi.word.length > nextWord.word.length) {
					nextWord = wi;
				}
			}
		} else {
			for (final WordInfo wi : this.blankWords) {
				final int matches = this.dictionary.howManyWords(wi.word.length);
				if (matches < leastMatches) {
					leastMatches = matches;
					nextWord = wi;
				}
			}
		}
		return nextWord;
	}

	private boolean intersects(final WordInfo l, final WordInfo r) {
		return l.direction != r.direction && ((l.direction == 'a' && l.x >= r.x && l.x <= r.x + r.word.length - 1
				&& l.y <= r.y && r.y <= l.y + l.word.length - 1)
				|| (l.direction == 'd' && l.y >= r.y && l.y <= r.y + r.word.length - 1 && l.x <= r.x
						&& r.x <= l.x + l.word.length - 1));
	}

	private void enterWord(final WordInfo wi) {
		if (wi.direction == 'a') {
			for (int y = wi.y; y < wi.y + wi.word.length; ++y) {
				this.crossword[wi.x][y] = wi.word[y - wi.y];
			}
		} else if (wi.direction == 'd') {
			for (int x = wi.x; x < wi.x + wi.word.length; ++x) {
				this.crossword[x][wi.y] = wi.word[x - wi.x];
			}
		}
		this.updateWordLists();
	}

	void reset() {
		this.crossword = this.copyChars(this.startState);
		this.updateWordLists();
	}

	private char[][] copyChars(final char[][] r) {
		final char[][] temp = new char[r.length][r.length];
		for (int x = 0; x < r.length; ++x) {
			for (int y = 0; y < r.length; ++y) {
				temp[x][y] = r[x][y];
			}
		}
		return temp;
	}

	private boolean compareFinished(final char[] l, final char[] r) {
		if (l.length != r.length) {
			return false;
		}
		for (int i = 0; i < l.length; ++i) {
			if (l[i] != r[i]) {
				return false;
			}
		}
		return true;
	}

	private boolean compareUnfinished(final char[] uf, final char[] f) {
		if (uf.length != f.length) {
			return false;
		}
		for (int i = 0; i < uf.length; ++i) {
			if (uf[i] != ' ' && uf[i] != f[i]) {
				return false;
			}
		}
		return true;
	}

	private boolean isOnBoard(final char[] s) {
		for (final WordInfo wi : this.finishedWords) {
			if (this.compareFinished(s, wi.word)) {
				return true;
			}
		}
		return false;
	}

	public boolean isValid() {
		int finishedCount = 0;
		int unfinishedCount = 0;
		this.dictionary.mark();
		for (int i = 0; i < this.finishedWords.size(); ++i) {
			if (this.dictionary.contains(this.finishedWords.get(i).word)) {
				++finishedCount;
			}
		}
		if (finishedCount < this.finishedWords.size()) {
			this.dictionary.goBack();
			return false;
		}
		for (int i = 0; i < this.unfinishedWords.size(); ++i) {
			if (this.dictionary.containsIncomplete(this.unfinishedWords.get(i).word)) {
				++unfinishedCount;
			}
		}
		if (unfinishedCount < this.unfinishedWords.size()) {
			this.dictionary.goBack();
			return false;
		}
		this.dictionary.goBack();
		return true;
	}

	public ArrayList<WordInfo> getInvalidWords() {
		final ArrayList<WordInfo> invalidWords = new ArrayList<WordInfo>();
		for (int i = 0; i < this.finishedWords.size(); ++i) {
			if (!this.dictionary.contains(this.finishedWords.get(i).word)) {
				invalidWords.add(this.finishedWords.get(i));
			}
		}
		for (int i = 0; i < this.unfinishedWords.size(); ++i) {
			if (!this.dictionary.containsIncomplete(this.unfinishedWords.get(i).word)) {
				invalidWords.add(this.unfinishedWords.get(i));
			}
		}
		return invalidWords;
	}

	private ArrayList<node> getMatches(final WordInfo nextWord, final int c) throws Exception {
		final ArrayList<node> matches = new ArrayList<node>();
		int count = 0;
		this.dictionary.setWordLength(nextWord.word.length);
		while (this.dictionary.hasNext() && count < c) {
			final char[] s = this.dictionary.next();
			if (this.compareUnfinished(nextWord.word, s) && !this.isOnBoard(s)) {
				final WordInfo wi = new WordInfo(s.clone(), nextWord.x, nextWord.y, nextWord.direction);
				final char[][] tempCrossword = this.copyChars(this.crossword);
				this.lastWords.push(wi);
				this.enterWord(wi);
				if (this.isValid()) {
					matches.add(new node(this.copyChars(this.crossword), (Stack<WordInfo>) this.lastWords.clone()));
					if (this.surrounded) {
						return matches;
					}
					if (++count == c) {
						continue;
					}
					this.lastWords.pop();
				} else {
					this.crossword = this.copyChars(tempCrossword);
					this.lastWords.pop();
					this.updateWordLists();
				}
			}
		}
		return matches;
	}

	public void itBroad(int c) throws Exception {
		this.nodeList = new Stack<node>();
		this.lastWords = new Stack<WordInfo>();
		while (this.finishedWords.size() < this.depth && !this.stop) {
			for (final node match : this.getMatches(this.getNextWord(), c)) {
				this.nodeList.push(match);
			}
			if (this.nodeList.isEmpty()) {
				this.reset();
				this.itBroad(++c);
			} else {
				this.crossword = this.copyChars(this.nodeList.peek().crossword);
				this.lastWords = (Stack) this.nodeList.pop().path.clone();
				this.updateWordLists();
				Display.displayWords(this.crossword);
			}
		}
		this.done = true;
	}

	public void stop() {
		this.stop = true;
	}

	public boolean isDone() {
		return this.done;
	}

	public void fill() throws Exception {
		this.itBroad(2);
	}

	private class node {
		char[][] crossword;
		Stack<WordInfo> path;

		public node(final char[][] crossword, final Stack<WordInfo> path) {
			this.crossword = Crossword.this.copyChars(crossword);
			this.path = path;
		}
	}
}
