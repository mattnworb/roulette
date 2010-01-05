package brown.roulette;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 * @author Matt Brown
 * @date Jan 4, 2010
 */
public class Roulette {

	private static final Logger log = Logger.getLogger(Roulette.class);

	private static enum Color {
		BLACK,
		GREEN,
		RED
	}

	// The wheel is implemented as an array of Colors
	private Color[] wheel;

	private Random rand = new Random();

	public Roulette(int numBlack, int numRed, int numGreen) {

		final int slots = numBlack + numRed + numGreen;

		this.wheel = new Color[slots];

		// alternate black and red
		for (int i = 0; i < (numBlack + numRed); i++) {
			if (i % 2 == 0) {
				wheel[i] = Color.BLACK;
			}
			else {
				wheel[i] = Color.RED;
			}
		}
		// fill the last slots with green
		Arrays.fill(wheel, numBlack + numRed, slots, Color.GREEN);
	}

	public void printWheel(PrintStream out) {
		for (int i = 0; i < wheel.length; i++) {
			out.format("Slot %d: %s", Integer.valueOf(i + 1), wheel[i]);
		}

	}

	public Color spin() {
		int result = rand.nextInt(wheel.length);
		return wheel[result];
	}

	private final static int NUM_BLACK = 18;

	private final static int NUM_RED = 18;

	private final static int NUM_GREEN = 2;

	public static void main(String[] args) {
		Roulette r = new Roulette(NUM_BLACK, NUM_RED, NUM_GREEN);

		int testRuns = 1000;
		if (args.length > 0) {
			try {
				testRuns = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException e) {
				System.err.println("Not a valid input for number of attempts: " + args[0]);
			}
		}

		List<Color> results = new ArrayList<Color>(testRuns);
		for (long i = 0; i < testRuns; i++) {
			results.add(r.spin());
		}

		System.out.println("Results: " + results);

		// find longest streak
		final Streak longestStreak = findLongestStreak(results);
		System.out.println("Longest streak: " + longestStreak);

		// dumpStreakElements(results, streak);

		// count all streaks less than the longest
		final int longestLength = longestStreak.getLength();

		// keep track of the streaks in a map
		Map<Integer, Integer> streaks = new HashMap<Integer, Integer>(longestLength);
		for (int i = longestLength; i > 0; i--) {
			streaks.put(i, countOccurencesOfLength(results, i));
		}

		for (Map.Entry<Integer, Integer> entry : streaks.entrySet()) {
			System.out.println("Number of occurences of length [" + entry.getKey() + "]: " + entry.getValue());
		}
	}

	private static void dumpStreakElements(List<Color> results, final Streak streak) {
		final int start = streak.getStartPosition();
		final int length = streak.getLength();
		for (int i = start; i < start + length; i++) {
			System.out.println(i + ": " + results.get(i));
		}
	}

	private static int countOccurencesOfLength(List<Color> results, int length) {
		int count = 0;

		int currentCount = 1;
		Color lastColor = null;

		for (Color result : results) {

			if (lastColor == result) {
				currentCount++;
			}
			else if (lastColor != null) {
				// streak broken - does the length match our input?
				if (currentCount == length) {
					count++;
				}

				// reset
				currentCount = 1;
			}

			lastColor = result;
		}

		return count;
	}

	private static Streak findLongestStreak(List<Color> results) {
		Streak longest = new Streak(0, 0);

		Color lastColor = null;
		int currentStreak = 1;

		for (int i = 0; i < results.size(); i++) {

			final Color result = results.get(i);

			log.debug("findLongestStreak: at index [" + i + "] result [" + result + "]");

			if (lastColor == result) {
				currentStreak++;
				log.debug("findLongestStreak: count of currentStreak at [" + currentStreak + "]");
			}
			else if (lastColor != null) {
				// streak broken
				log.debug("findLongestStreak: streak broken");

				if (currentStreak > longest.getLength()) {
					int start = i - currentStreak;
					log.debug("findLongestStreak: new max of [" + currentStreak + "] found, started at " + start);
					longest = new Streak(currentStreak, start);
				}

				// reset
				currentStreak = 1;
			}

			lastColor = result;
		}

		return longest;
	}

	private static class Streak {

		private int length;

		private int startPosition;

		public Streak(int length, int start) {
			this.length = length;
			this.startPosition = start;
		}

		public int getLength() {
			return length;
		}

		public int getStartPosition() {
			return startPosition;
		}

		@Override
		public String toString() {
			return "Length: " + length + ", startPosition: " + startPosition;
		}
	}
}
