import java.util.Scanner;

class entrance {
	public static void main(String str[]) {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please input a number:");
		System.out.println("1 convert to grayscale image");
		System.out.println("2 colorization using optimization");
		System.out.println("3 convert to grayscale movie");

		switch (scanner.nextLine()) {
		case "1": {
			grayscale.main(null);
			break;
		}
		case "2": {
			colorization.main(null);
			break;
		}
		case "3": {
			grayscale_gif.main(null);
			break;
		}
		default: {
			System.out.println("Input number error.");
			break;
		}
		}
	}
}