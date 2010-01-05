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
		// fill the remaining slots with green
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

		Map<Integer, Integer> streaks = countStreaks(results);

		for (Map.Entry<Integer, Integer> entry : streaks.entrySet()) {
			System.out.format("Number of occurences of length %2d: %5d\n", entry.getKey(), entry.getValue());
		}
	}

	private static Map<Integer, Integer> countStreaks(List<Color> results) {

		Map<Integer, Integer> streaks = new HashMap<Integer, Integer>(results.size());

		Color previousResult = results.get(0);
		int currentStreakLength = 1;
		for (int i = 1; i < results.size(); i++) {

			Color result = results.get(i);

			if (result == previousResult) {
				currentStreakLength++;
			}
			else {
				// streak broken
				incrementStreakCount(streaks, currentStreakLength);

				// reset
				currentStreakLength = 1;
			}

			previousResult = result;
		}

		return streaks;

	}

	private static void incrementStreakCount(Map<Integer, Integer> streaks, int streakLength) {
		final Integer key = Integer.valueOf(streakLength);
		if (!streaks.containsKey(key)) {
			streaks.put(key, 0);
		}
		streaks.put(key, streaks.get(streakLength) + 1);
	}
}
