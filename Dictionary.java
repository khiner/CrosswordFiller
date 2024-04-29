import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.TreeMap;

public class Dictionary {
	private final String dictFile = "/FinalDict";
	final String bufFile = "/BufMap";
	TreeMap<String, Double> freqMap;
	HashMap<Integer, Integer> bufferMap;
	InputStream fis;
	FileWriter fos;
	Scanner scanner;
	FileReader fileReader;
	File file;
	BufferedWriter out;
	byte[] buf;
	int currBufPos;
	int currWordSize;
	int markedBufPos;
	int markedWordSize;
	int bufLen;

	public Dictionary() throws Exception {
		this.currBufPos = 0;
		this.currWordSize = 0;
		this.markedBufPos = 0;
		this.markedWordSize = 0;
		this.setupBuffer();
	}

	private void setupFreqChart() {
		(this.freqMap = new TreeMap<String, Double>()).put("a", 0.08167);
		this.freqMap.put("b", 0.01492);
		this.freqMap.put("c", 0.02782);
		this.freqMap.put("d", 0.04253);
		this.freqMap.put("e", 0.12702);
		this.freqMap.put("f", 0.02228);
		this.freqMap.put("g", 0.02015);
		this.freqMap.put("h", 0.06094);
		this.freqMap.put("i", 0.06966);
		this.freqMap.put("j", 0.00153);
		this.freqMap.put("k", 0.00772);
		this.freqMap.put("l", 0.04025);
		this.freqMap.put("m", 0.02406);
		this.freqMap.put("n", 0.06749);
		this.freqMap.put("o", 0.07507);
		this.freqMap.put("p", 0.01929);
		this.freqMap.put("q", 9.5E-4);
		this.freqMap.put("r", 0.05987);
		this.freqMap.put("s", 0.06327);
		this.freqMap.put("t", 0.09056);
		this.freqMap.put("u", 0.02758);
		this.freqMap.put("v", 0.00987);
		this.freqMap.put("w", 0.0236);
		this.freqMap.put("x", 0.0015);
		this.freqMap.put("y", 0.01974);
		this.freqMap.put("z", 7.4E-4);
	}

	private double getFreqValue(final char[] s) {
		double value = 0.0;
		for (int i = 0; i < s.length; ++i) {
			value += this.freqMap.get(new StringBuilder().append(Character.toLowerCase(s[i])).toString());
		}
		value /= s.length;
		return value;
	}

	private void setBufMap() throws Exception {
		this.bufferMap = new HashMap<Integer, Integer>();
		this.bufLen = (int) new File("/FinalDict").length();
		this.fis = this.getClass().getResourceAsStream("/FinalDict");
		this.buf = new byte[this.bufLen];
		this.fis.read(this.buf);
		this.fis.close();
		this.currBufPos = 0;
		this.bufferMap.put(1, 0);
		int currLength = 1;
		while (this.hasNext()) {
			final int bufferMark = this.currBufPos;
			final char[] string = this.next();
			if (string.length > currLength) {
				currLength = string.length;
				this.bufferMap.put(currLength, bufferMark);
			}
		}
		this.fos = new FileWriter("/BufMap");
		for (int i = 0; i <= 20; ++i) {
			this.fos.write(i + " " + this.bufferMap.get(i) + "\n");
		}
		this.fos.close();
	}

	private void setupBuffer() throws Exception {
		this.scanner = new Scanner(this.getClass().getResourceAsStream("/BufMap"));
		this.bufferMap = new HashMap<Integer, Integer>();
		while (this.scanner.hasNextInt()) {
			final int length = this.scanner.nextInt();
			final int buffMark = this.scanner.nextInt();
			this.bufferMap.put(length, buffMark);
		}
		this.bufLen = 369088;
		this.fis = this.getClass().getResourceAsStream("/FinalDict");
		this.buf = new byte[this.bufLen];
		this.fis.read(this.buf);
		this.fis.close();
	}

	public void setWordLength(final int size) {
		this.currWordSize = size;
		this.currBufPos = this.bufferMap.get(size);
	}

	public boolean hasNext() {
		return this.currBufPos < this.bufferMap.get(this.currWordSize + 1);
	}

	public char[] next() {
		final char[] string = new char[this.currWordSize];
		int index = 0;
		while (this.buf[this.currBufPos] != 10) {
			string[index] = (char) this.buf[this.currBufPos];
			++this.currBufPos;
			++index;
		}
		++this.currBufPos;
		return string;
	}

	public boolean contains(final char[] word) {
		this.setWordLength(word.length);
		while (this.hasNext()) {
			if (this.compareChars(word, this.next())) {
				return true;
			}
		}
		return false;
	}

	public boolean containsIncomplete(final char[] word) {
		this.setWordLength(word.length);
		while (this.hasNext()) {
			if (this.compareUnfinished(word, this.next())) {
				return true;
			}
		}
		return false;
	}

	public int howManyMatches(final char[] word) {
		this.setWordLength(word.length);
		int matches = 0;
		while (this.hasNext()) {
			if (this.compareUnfinished(word, this.next())) {
				++matches;
			}
		}
		return matches;
	}

	public int howManyWords(final int length) {
		return (this.bufferMap.get(length + 1) - this.bufferMap.get(length)) / (length + 1);
	}

	boolean compareChars(final char[] l, final char[] r) {
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

	boolean compareUnfinished(final char[] uf, final char[] f) {
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

	public void mark() {
		if (this.currBufPos != 0 && this.currWordSize != 0) {
			this.markedBufPos = this.currBufPos;
			this.markedWordSize = this.currWordSize;
			this.currBufPos = this.bufferMap.get(this.currWordSize);
		}
	}

	public void goBack() {
		this.currBufPos = this.markedBufPos;
		this.currWordSize = this.markedWordSize;
	}
}
