public class WordInfo {
	public char[] word;
	int x;
	int y;
	char direction;

	WordInfo() {
		this.word = new char[0];
		this.x = 0;
		this.y = 0;
		this.direction = ' ';
	}

	WordInfo(final char[] word, final int x, final int y, final char direction) {
		this.word = word.clone();
		this.x = x;
		this.y = y;
		this.direction = direction;
	}
}
