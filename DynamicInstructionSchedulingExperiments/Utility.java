public class Utility {
    // convert string to integer. If conversion fails end the program with error message
	static int tryParseInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			System.out.println("Input argument <" + str + "> is not in a valid format.");
			System.exit(1); 
			return 0;
		}
	}

	// convert string to long. If conversion fails end the program with error message
	static long tryParseHexToLong(String str) {
		try {
			return Long.parseLong(str, 16);
		} catch (NumberFormatException e) {
			System.out.println("Trace input contains invalid address <" + str + ">.");
			System.exit(1); 
			return 0;
		}
    }
}