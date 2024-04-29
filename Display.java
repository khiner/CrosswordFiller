import java.awt.event.MouseEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.event.MouseListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import javax.swing.SwingWorker;
import java.util.Iterator;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeEvent;
import java.util.Random;
import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.LayoutManager;
import java.util.concurrent.Executors;
import javax.swing.JTextField;
import java.awt.GridLayout;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Color;
import java.util.concurrent.Executor;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;

public class Display implements ChangeListener, ActionListener {
	private final Executor executor;
	static final Color highlightColor;
	JFrame mainWindow;
	JPanel board;
	JPanel mainPanel;
	JPanel sizePanel;
	JPanel densityPanel;
	JSlider densitySlider;
	JSlider sizeSlider;
	JButton stopGo;
	JButton reset;
	JTextArea display;
	GridLayout gl;
	static Square[][] grid;
	static JTextField currentSquare;
	static int size;
	static int density;
	static int halfway;
	static double startTime;
	static double time;
	Crossword crossword;
	boolean across;
	String startInfo;
	String endInfo;

	static {
		highlightColor = Color.cyan;
	}

	public Display() {
		this.executor = Executors.newCachedThreadPool();
		this.startInfo = "Click on the board to type in words.\nUse arrows to navigate the board.\nDouble-clicking alternates between across & down.";
		this.endInfo = "Click on the board or press 'Reset' to clear the board";
		Display.size = 8;
		Display.density = 30;
		this.across = true;
		Display.halfway = (int) Math.ceil(Display.size / 2.0);
		(this.mainWindow = new JFrame()).setLayout(new GridLayout(0, 2));
		this.board = new JPanel();
		(this.stopGo = new JButton("FILL")).setFont(new Font("Dialog", 1, 40));
		this.stopGo.setBackground(Color.ORANGE);
		this.stopGo.setFocusPainted(false);
		this.stopGo.addActionListener(this);
		(this.reset = new JButton("RESET")).setFont(new Font("Dialog", 1, 40));
		this.reset.setBackground(Color.ORANGE);
		this.reset.setFocusPainted(false);
		this.reset.addActionListener(this);
		(this.densitySlider = new JSlider(0, 0, 45, Display.density)).setMajorTickSpacing(5);
		this.densitySlider.setPaintTicks(true);
		this.densitySlider.setPaintLabels(true);
		this.densitySlider.setSnapToTicks(true);
		this.densitySlider.setBackground(Color.YELLOW);
		this.densitySlider.addChangeListener(this);
		this.densityPanel = new JPanel(new GridLayout(2, 0));
		JLabel label = new JLabel();
		label.setFont(new Font("Dialog", 1, 20));
		label.setText("% of Black Squares");
		this.densityPanel.add(label);
		this.densityPanel.add(this.densitySlider);
		this.densityPanel.setBackground(Color.YELLOW);
		this.densityPanel
				.setToolTipText("This is roughly the percentage of black squares that will appear on the board.");
		(this.sizeSlider = new JSlider(0, 2, 14, Display.size)).setMajorTickSpacing(1);
		this.sizeSlider.setPaintLabels(true);
		this.sizeSlider.setSnapToTicks(true);
		this.sizeSlider.setBackground(Color.YELLOW);
		this.sizeSlider.addChangeListener(this);
		this.sizePanel = new JPanel(new GridLayout(2, 0));
		label = new JLabel();
		label.setFont(new Font("Dialog", 1, 20));
		label.setText("Size of Crossword");
		this.sizePanel.add(label);
		this.sizePanel.add(this.sizeSlider);
		this.sizePanel.setBackground(Color.YELLOW);
		this.sizePanel.setToolTipText("This is the number of tiles on a side of the crossword square");
		(this.display = new JTextArea()).setFont(new Font("Dialog", 1, 18));
		this.display.setOpaque(true);
		this.display.setBackground(Color.YELLOW);
		this.display.setText(this.startInfo);
		(this.mainPanel = new JPanel(new GridLayout(5, 0))).add(this.densityPanel);
		this.mainPanel.add(this.sizePanel);
		this.mainPanel.add(this.stopGo);
		this.mainPanel.add(this.reset);
		this.mainPanel.add(this.display);
		this.mainWindow.getContentPane().add(this.mainPanel);
		this.mainWindow.getContentPane().add(this.board);
		this.mainWindow.pack();
		this.mainWindow.setDefaultCloseOperation(3);
		this.mainWindow.setExtendedState(6);
		this.populateBoard();
	}

	private void grayAll() {
		for (int x = 0; x < Display.grid.length; ++x) {
			for (int y = 0; y < Display.grid.length; ++y) {
				if (Display.grid[x][y].getText().isEmpty() && !Display.grid[x][y].isLocked()) {
					Display.grid[x][y].setBackground(Color.lightGray);
				}
			}
		}
	}

	private void redHighlight(final WordInfo word) {
		if (word.direction == 'a') {
			for (int i = 0; i < word.word.length; ++i) {
				Display.grid[word.x][word.y + i].setBackground(Color.pink);
			}
		} else if (word.direction == 'd') {
			for (int i = 0; i < word.word.length; ++i) {
				Display.grid[word.x + i][word.y].setBackground(Color.pink);
			}
		}
	}

	private void lockAll() {
		for (int x = 0; x < Display.size; ++x) {
			for (int y = 0; y < Display.size; ++y) {
				Display.grid[x][y].setLock(true);
			}
		}
	}

	private String getAlpha(final String key) {
		if (key.length() != 1) {
			return "";
		}
		try {
			Integer.parseInt(key);
		} catch (final NumberFormatException e) {
			return key;
		}
		return "";
	}

	public void populateBoard() {
		Display.grid = new Square[Display.size][Display.size];
		this.gl = new GridLayout(Display.size, Display.size);
		this.board.removeAll();
		this.board.setLayout(this.gl);
		final Random rand = new Random();
		final int[][] seeds = new int[Display.size][Display.halfway];
		for (int x = 0; x < Display.size; ++x) {
			for (int y = 0; y < Display.halfway; ++y) {
				seeds[x][y] = rand.nextInt(100);
			}
		}
		int x2 = Display.size;
		for (int x3 = 0; x3 < Display.size; ++x3) {
			--x2;
			int y2 = (Display.size % 2 == 0) ? Display.halfway : (Display.halfway - 1);
			for (int y3 = 0; y3 < Display.size; ++y3) {
				Display.grid[x3][y3] = new Square(x3, y3);
				boolean setBlack;
				if (y3 < Display.halfway) {
					setBlack = (seeds[x3][y3] < Display.density);
				} else {
					--y2;
					setBlack = (seeds[x2][y2] < Display.density);
				}
				if (setBlack) {
					Display.grid[x3][y3].setBackground(Color.black);
					Display.grid[x3][y3].setLock(true);
					Display.grid[x3][y3].setFocusable(false);
				} else {
					Display.grid[x3][y3].setBackground(Color.lightGray);
				}
				this.board.add(Display.grid[x3][y3]);
			}
		}
		this.board.repaint();
		this.mainWindow.setVisible(true);
	}

	public char[][] getChars() {
		final char[][] crossword = new char[Display.size + 1][Display.size + 1];
		for (int x = 0; x < Display.size; ++x) {
			for (int y = 0; y < Display.size; ++y) {
				if (Display.grid[x][y].getBackground() == Color.black) {
					crossword[x][y] = '\n';
				} else if (!Display.grid[x][y].getText().isEmpty()) {
					crossword[x][y] = Display.grid[x][y].getText().charAt(0);
				} else {
					crossword[x][y] = ' ';
				}
			}
		}
		for (int x = 0; x < Display.size; ++x) {
			crossword[x][Display.size] = '\n';
		}
		for (int y2 = 0; y2 < Display.size; ++y2) {
			crossword[Display.size][y2] = '\n';
		}
		return crossword;
	}

	static void displayWords(final char[][] crossword) {
		for (int x = 0; x < crossword.length - 1; ++x) {
			for (int y = 0; y < crossword.length - 1; ++y) {
				if (crossword[x][y] != '\n') {
					Display.grid[x][y].setText(new StringBuilder().append(crossword[x][y]).toString());
				}
			}
		}
	}

	private void ungray() {
		for (int x = 0; x < Display.size; ++x) {
			for (int y = 0; y < Display.size; ++y) {
				Display.grid[x][y].setLock(true);
				if (Display.grid[x][y].getText().isEmpty() && Display.grid[x][y].getBackground() != Color.black) {
					Display.grid[x][y].setBackground(Color.white);
				}
			}
		}
	}

	private void reset() {
		for (int x = 0; x < Display.size; ++x) {
			for (int y = 0; y < Display.size; ++y) {
				if (Display.grid[x][y].getBackground() != Color.black) {
					Display.grid[x][y].setText("");
					Display.grid[x][y].setBackground(Color.lightGray);
					Display.grid[x][y].setLock(false);
				}
			}
		}
		this.display.setText(this.startInfo);
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		this.stop();
		final JSlider source = (JSlider) e.getSource();
		if (source.equals(this.sizeSlider) && source.getValue() != Display.size) {
			Display.size = source.getValue();
			Display.halfway = (int) Math.ceil(Display.size / 2.0);
			this.populateBoard();
		} else if (source.equals(this.densitySlider)
				&& (source.getValue() <= Display.density - 5 || source.getValue() >= Display.density + 5)) {
			Display.density = source.getValue();
			this.populateBoard();
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final JButton source = (JButton) e.getSource();
		if (source.equals(this.stopGo) && this.stopGo.getText() == "FILL") {
			this.start();
		} else if (source.equals(this.stopGo) && this.stopGo.getText() == "STOP") {
			this.stop();
		} else if (source.equals(this.reset)) {
			this.stop();
			this.reset();
		}
	}

	private void start() {
		try {
			this.crossword = new Crossword(this.getChars());
			final ArrayList<WordInfo> invalidWords = this.crossword.getInvalidWords();
			if (invalidWords.isEmpty()) {
				this.ungray();
				this.stopGo.setText("STOP");
				this.executor.execute(new crosswordWorker());
				this.executor.execute(new timeWorker());
			} else {
				this.lockAll();
				final StringBuffer sb = new StringBuffer();
				boolean incomplete = false;
				sb.append("Unfortunate news:\n");
				for (final WordInfo word : invalidWords) {
					for (int i = 0; i < word.word.length; ++i) {
						if (word.word[i] == ' ') {
							word.word[i] = '_';
							incomplete = true;
						}
					}
					if (incomplete) {
						sb.append("None of my " + word.word.length + " letter words complete ");
						sb.append(word.word);
						sb.append(".\n");
					} else {
						sb.append(word.word);
						sb.append(" is not in my dictionary.\n");
					}
					this.redHighlight(word);
					this.display.setText(String.valueOf(sb.toString()) + this.endInfo);
				}
			}
		} catch (final Exception e1) {
			this.display.setText(e1.toString());
		}
	}

	private void stop() {
		if (this.crossword != null) {
			this.crossword.stop();
			this.stopGo.setText("FILL");
		}
	}

	public static void main(final String[] args) throws Exception {
		new Display();
	}

	private class crosswordWorker extends SwingWorker<Void, Void> {
		public Void doInBackground() throws Exception {
			try {
				Display.this.crossword.fill();
				Display.this.stopGo.setText("FILL");
			} catch (final Exception e1) {
				e1.printStackTrace();
			}
			return ((SwingWorker<Void, Void>) this).get();
		}
	}

	private class timeWorker extends SwingWorker<Void, Void> {
		public Void doInBackground() throws Exception {
			final DecimalFormat format = new DecimalFormat("#.###");
			Display.startTime = (double) System.currentTimeMillis();
			while (!Display.this.crossword.isDone()) {
				Display.time = (System.currentTimeMillis() - Display.startTime) / 1000.0;
				final String message = (Display.time >= 60.0)
						? ((int) (Display.time / 60.0) + " minutes " + format.format(Display.time % 60.0) + " seconds")
						: (format.format(Display.time) + " seconds");
				Display.this.display.setText(message);
			}
			return ((SwingWorker<Void, Void>) this).get();
		}
	}

	class Square extends JTextField implements KeyListener, FocusListener, MouseListener {
		int x;
		int y;
		int count;
		boolean locked;
		boolean clicked;

		public Square(final int x, final int y) {
			this.x = x;
			this.y = y;
			this.locked = false;
			this.clicked = false;
			this.setFont(new Font("Dialog", 0, (int) (500.0 / Display.size)));
			this.setForeground(Color.black);
			this.setDisabledTextColor(Color.black);
			this.setHorizontalAlignment(0);
			this.setEnabled(true);
			this.setBorder(new LineBorder(Color.black, 1));
			this.addKeyListener(this);
			this.addFocusListener(this);
			this.addMouseListener(this);
			this.setEditable(false);
		}

		public void setLock(final boolean bool) {
			this.locked = bool;
		}

		public boolean isLocked() {
			return this.locked;
		}

		private void highlightAcross() {
			Display.this.across = true;
			Display.this.grayAll();
			for (int count = 0; this.y - count >= 0 && Display.grid[this.x][this.y - count].isFocusable(); ++count) {
				Display.grid[this.x][this.y - count].setBackground(Display.highlightColor);
			}
			for (int count = 0; this.y + count < Display.size
					&& Display.grid[this.x][this.y + count].isFocusable(); ++count) {
				Display.grid[this.x][this.y + count].setBackground(Display.highlightColor);
			}
		}

		private void highlightDown() {
			Display.this.across = false;
			Display.this.grayAll();
			for (int count = 0; this.x - count >= 0 && Display.grid[this.x - count][this.y].isFocusable(); ++count) {
				Display.grid[this.x - count][this.y].setBackground(Display.highlightColor);
			}
			for (int count = 0; this.x + count < Display.size
					&& Display.grid[this.x + count][this.y].isFocusable(); ++count) {
				Display.grid[this.x + count][this.y].setBackground(Display.highlightColor);
			}
		}

		private boolean isAcrossSquare() {
			return ((this.squareToLeft() != null && !this.squareToLeft().isBlack())
					|| (this.squareToRight() != null && !this.squareToRight().isBlack()) || this.squareBelow() == null
					|| this.squareBelow().isBlack() || this.squareAbove() == null || this.squareAbove().isBlack())
					&& (this.squareAbove() == null || this.squareAbove().isBlack()
							|| (this.squareBelow() != null && !this.squareBelow().isBlack()))
					&& ((this.squareAbove() != null && !this.squareAbove().isBlack()) || this.squareBelow() == null
							|| this.squareBelow().isBlack());
		}

		public boolean isBlack() {
			return this.getBackground() == Color.black;
		}

		private Square squareToLeft() {
			if (this.y - 1 >= 0) {
				return Display.grid[this.x][this.y - 1];
			}
			return null;
		}

		private Square squareToRight() {
			if (this.y + 1 < Display.size) {
				return Display.grid[this.x][this.y + 1];
			}
			return null;
		}

		private Square squareAbove() {
			if (this.x - 1 >= 0) {
				return Display.grid[this.x - 1][this.y];
			}
			return null;
		}

		private Square squareBelow() {
			if (this.x + 1 < Display.size) {
				return Display.grid[this.x + 1][this.y];
			}
			return null;
		}

		@Override
		public void keyPressed(final KeyEvent e) {
			if (this.locked) {
				return;
			}
			switch (e.getKeyCode()) {
			case 40: {
				Display.this.across = false;
				this.count = 1;
				while (this.x + this.count < Display.size) {
					if (Display.grid[this.x + this.count][this.y].isFocusable()) {
						Display.grid[this.x + this.count][this.y].requestFocus();
						break;
					}
					++this.count;
				}
				break;
			}
			case 39: {
				Display.this.across = true;
				this.count = 1;
				while (this.y + this.count < Display.size) {
					if (Display.grid[this.x][this.y + this.count].isFocusable()) {
						Display.grid[this.x][this.y + this.count].requestFocus();
						break;
					}
					++this.count;
				}
				break;
			}
			case 38: {
				this.count = 1;
				Display.this.across = false;
				this.highlightDown();
				while (this.x - this.count < Display.size) {
					if (Display.grid[this.x - this.count][this.y].isFocusable()) {
						Display.grid[this.x - this.count][this.y].requestFocus();
						break;
					}
					++this.count;
				}
				break;
			}
			case 37: {
				Display.this.across = true;
				this.highlightAcross();
				this.count = 1;
				while (this.y - this.count < Display.size) {
					if (Display.grid[this.x][this.y - this.count].isFocusable()) {
						Display.grid[this.x][this.y - this.count].requestFocus();
						break;
					}
					++this.count;
				}
				break;
			}
			case 8: {
				this.count = 1;
				if (!this.getText().isEmpty()) {
					this.setText("");
					break;
				}
				if (Display.this.across) {
					while (this.y - this.count < Display.size) {
						if (Display.grid[this.x][this.y - this.count].isFocusable()) {
							Display.grid[this.x][this.y - this.count].requestFocus();
							Display.grid[this.x][this.y - this.count].setText("");
							break;
						}
						++this.count;
					}
					break;
				}
				while (this.x - this.count < Display.size) {
					if (Display.grid[this.x - this.count][this.y].isFocusable()) {
						Display.grid[this.x - this.count][this.y].requestFocus();
						Display.grid[this.x - this.count][this.y].setText("");
						break;
					}
					++this.count;
				}
				break;
			}
			case 10: {
				Display.this.start();
				break;
			}
			default: {
				final String key = Display.this.getAlpha(KeyEvent.getKeyText(e.getKeyCode()));
				if (key == "") {
					break;
				}
				this.setText(String.valueOf(key));
				this.count = 1;
				if (Display.this.across) {
					while (this.y + this.count < Display.size) {
						if (Display.grid[this.x][this.y + this.count].isFocusable()) {
							Display.grid[this.x][this.y + this.count].requestFocus();
							break;
						}
						++this.count;
					}
					break;
				}
				while (this.x + this.count < Display.size) {
					if (Display.grid[this.x + this.count][this.y].isFocusable()) {
						Display.grid[this.x + this.count][this.y].requestFocus();
						break;
					}
					++this.count;
				}
				break;
			}
			}
		}

		@Override
		public void keyReleased(final KeyEvent e) {
		}

		@Override
		public void keyTyped(final KeyEvent e) {
		}

		@Override
		public void focusGained(final FocusEvent e) {
			if (!this.locked) {
				this.getCaret().setVisible(true);
				if (Display.this.across) {
					this.highlightAcross();
				} else if (!Display.this.across) {
					this.highlightDown();
				}
			}
		}

		@Override
		public void focusLost(final FocusEvent e) {
			this.getCaret().setVisible(false);
			this.clicked = false;
		}

		@Override
		public void mouseClicked(final MouseEvent arg0) {
			if (this.locked) {
				if (Display.this.stopGo.getText() == "STOP") {
					Display.this.stop();
				} else if (Display.this.stopGo.getText() == "FILL") {
					Display.this.reset();
				}
			} else if (this.clicked) {
				if (Display.this.across) {
					Display.this.across = false;
					this.highlightDown();
				} else {
					Display.this.across = true;
					this.highlightAcross();
				}
			} else {
				this.clicked = true;
			}
		}

		@Override
		public void mouseEntered(final MouseEvent arg0) {
			if (this.locked) {
				return;
			}
			if (this.isAcrossSquare()) {
				Display.this.across = true;
				this.highlightAcross();
			} else {
				Display.this.across = false;
				this.highlightDown();
			}
		}

		@Override
		public void mouseExited(final MouseEvent arg0) {
			Display.this.grayAll();
		}

		@Override
		public void mousePressed(final MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(final MouseEvent arg0) {
		}
	}
}
