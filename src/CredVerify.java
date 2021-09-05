import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CredVerify extends Thread {
	/**
	 * Check if credit cards are valid.
	 * @author Jessie
	 */
	
	private static final class CompanyInfo {
		/**
		 * Credit card company info.
		 */
		String name;
		String[] prefixes;
		int[] cardLengths;
	}
	
	private static final CompanyInfo[] getCompanies() {
		/**
		 * Store info about credit card companies.
		 */
		CompanyInfo[] companies = new CompanyInfo[4];
		
		CompanyInfo amex = new CompanyInfo();
		amex.name = "American Express";
		amex.prefixes = new String[]{"34", "37"};
		amex.cardLengths = new int[]{15};
		companies[0] = amex;
		
		CompanyInfo discover = new CompanyInfo();
		discover.name = "Discover Card";
		discover.prefixes = new String[]{"6"};
		discover.cardLengths = new int[]{16, 17, 18, 19};
		companies[1] = discover;
		
		CompanyInfo master = new CompanyInfo();
		master.name = "Mastercard";
		master.prefixes = new String[]{"51", "52", "53", "54", "55"};
		master.cardLengths = new int[]{16};
		companies[2] = master;
		
		CompanyInfo visa = new CompanyInfo();
		visa.name = "Visa";
		visa.prefixes = new String[]{"4"};
		visa.cardLengths = new int[]{13, 16};
		companies[3] = visa;
		
		return companies;
	}
	
	// Credit card companies
	private static final CompanyInfo[] COMPANIES = getCompanies();
	
	// Directory to log processing details for all cards
	private static final LocalDateTime TIME_NOW = LocalDateTime.now();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(
    		"yyyy-MM-dd--HH-mm-ss-SSS");
	private static final String DIRNAME = "cred" + TIME_NOW.format(FORMATTER);
	private static final String DIRPATH = "C:\\Users\\Jessie\\workspace"
			+ "\\OSproj\\credLogs\\" + DIRNAME;
	
	// Print verification results to console
	private static volatile String[] outputArray;
	
	String card;
	int inputNum; // Subscript of this input in args list
	int len; // Length of card
	CompanyInfo myCompany; // Credit card company which issued this card
	
	// File to log processing details for this card
	String filePath;
	File myFile;
	FileWriter fw;
	
	// Vars used to check if card is valid
	private volatile boolean valid = false;
	private volatile int evenPlacesSum = 0;
	private volatile int oddPlacesSum = 0;
	
	public CredVerify(String card, int i) {
		this.card = card;
		this.inputNum = i;
		this.filePath = DIRPATH + "\\input" + inputNum + ".txt";
		this.len = card.length();
	}
	
	protected static void makeLogDir() {
		/**
		 * Make directory to log processing details for all cards.
		 */
		try {
			File dir = new File(DIRPATH);
			if (dir.exists()) {
				System.out.printf("\nERROR: Log directory "
						+ "already exists: %s\n", DIRPATH);
				System.exit(1);
			} else {
				boolean success = dir.mkdir();
				if (success) {
					System.out.printf("\nWriting logs to %s\n", DIRPATH);
				} else {
					System.out.printf("\nERROR: Could not make "
							+ "log directory %s\n", DIRPATH);
			    	System.exit(1);
				}
			}
	    } catch(Exception e) {
	    	System.out.printf("\nERROR: Could not make "
					+ "log directory %s\n", DIRPATH);
	    	e.printStackTrace();
	    	System.exit(1);
	    }
	}
	
	protected void createFile() {
		/**
		 * Create file to log processing details for this card.
		 */
		try {
			myFile = new File(filePath);
			myFile.createNewFile();
			fw = new FileWriter(myFile);
	    } catch (IOException e) {
	      System.out.printf("ERROR creating log file %s\n", filePath);
	      e.printStackTrace();
	      System.exit(1);
	    }
	}
	
	protected void logging(String message) {
		/**
		 * Write processing details to file for this card.
		 */
		try {
			fw.write(message);
		} catch (IOException e) {
			System.out.printf("ERROR writing to log file %s\n", filePath);
		    e.printStackTrace();
		    System.exit(1);
		}
	}
	
	protected void printOutput() {
		/**
		 * Verification output for a card is written to its file 
		 * and also to console.
		 */
		String message = "Input Number: " + inputNum + " Card: " + card;
		if (!valid) {
			message += " is invalid.\n";
		} else {
			message += " is valid. It is issued by " + myCompany.name + ".\n";
		}
		
		this.logging(message);
		outputArray[inputNum] = message;
	}
	
	protected boolean onlyDigits() {
		/**
		 * Card should contain only digits.
		 */
		return card.matches("^[0-9]+$");
	}
	
	protected boolean checkPrefix() {
		/**
		 * Check if card has a valid prefix.
		 */
		for (CompanyInfo c : COMPANIES) {
			for (String p : c.prefixes) {
				if (card.startsWith(p)) {
					
					// Assuming two different companies 
					// will never have same prefixes.
					myCompany = c;
					
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean checkCompanyLength() {
		/**
		 * Check if card has a valid length according to 
		 * the company which issues that prefix.
		 */
		for (int cardLength : myCompany.cardLengths) {
			if (len == cardLength) {
				return true;
			}
		}
		return false;
	}
	
	protected void checkEvenPlaces() {
		/**
		 * Double every second digit from right to left.
		 * If doubling results in a two digit number, add the two digits.
		 * Add all single-digit numbers.
		 */
		String message = "Processing even places = ";
		for (int i = len-2; i >= 0; i = i-2) {
			char ch = card.charAt(i);
			int num = Character.getNumericValue(ch);
			int twiceNum = 2 * num;
			
			if (num > 4) {
				// twiceNum is two digits
				int a = twiceNum / 10;
				int b = twiceNum % 10;
				evenPlacesSum += a + b;
				
				message += "(" + a + " + " + b + ") + ";
				
			} else {
				// twiceNum is a single digit
				evenPlacesSum += twiceNum;
				
				message += twiceNum + " + ";
			}
		}
		
		// remove the trailing " + "
		message = message.substring(0, message.length() - 3);
		message += "\nSum for even places = " + evenPlacesSum + "\n";
		this.logging(message);
	}
	
	protected void checkOddPlaces() {
		/**
		 * Add all digits in the odd places of the card from right to left.
		 */
		String message = "Processing odd places = ";
		for (int i = len-1; i >= 0; i = i-2) {
			char ch = card.charAt(i);
			int num = Character.getNumericValue(ch);
			oddPlacesSum += num;
			
			message += num + " + ";
		}
		
		// remove the trailing " + "
		message = message.substring(0, message.length() - 3);
		message += "\nSum for odd places = " + oddPlacesSum + "\n";
		this.logging(message);
	}
	
	protected void checkTotal() {
		/**
		 * Sum the results from processing even and odd places.
		 * If total is divisible by 10, the card is valid.
		 */
		int total = evenPlacesSum + oddPlacesSum;
		String message = "Total = " + evenPlacesSum + " + " 
							+ oddPlacesSum + " = " + total + "\n";
		this.logging(message);
		
		if (total % 10 == 0) {
			this.logging("Total is divisible by 10.\n");
			// valid card
			valid = true;
		} else {
			this.logging("Total is NOT divisible by 10.\n");
			// invalid card
		}
	}
	
	protected void verifyCreditCard() {
		/**
		 * Check if input is a valid credit card.
		 */
		String message = "\nEvaluating " + card + "\n";
		message += "Length = " + len + "\n";		
		this.logging(message);
		
		if (len < 13 || len > 19) {
			this.logging("Incorrect length.\n");
		} else if (!this.onlyDigits()) {
			this.logging("Non-digit.\n");
		} else if (!checkPrefix()) {
			this.logging("Incorrect prefix.\n");
		} else if (!checkCompanyLength()) {
			this.logging("Incorrect length for company with that prefix.\n");
		} else {
			// object to use inside threads in order to call functions
			CredVerify c = this;
			
			// thread to process even places of the card
			Thread evenThread = new Thread() {
				@Override
				public void run() {
					c.checkEvenPlaces();
				}
			};
			
			// thread to process odd places of the card
			Thread oddThread = new Thread() {
				@Override
				public void run() {
					c.checkOddPlaces();
				}
			};
			
			evenThread.start();
			oddThread.start();
			try {
				evenThread.join();
			} catch (InterruptedException e) {
				System.out.printf("InterruptedException during "
						+ "card %s evenThread.join(): %s\n", c.card, e);
			}
			try {
				oddThread.join();
			} catch (InterruptedException e) {
				System.out.printf("InterruptedException during "
						+ "card %s oddThread.join(): %s\n", c.card, e);
			}
			
			// thread to calculate total and check if it's divisible by 10
			Thread totalThread = new Thread() {
				@Override
				public void run() {
					c.checkTotal();
				}
			};
			
			totalThread.start();
			try {
				totalThread.join();
			} catch (InterruptedException e) {
				System.out.printf("InterruptedException during "
						+ "card %s totalThread.join(): %s\n", c.card, e);
			}
		}
		// print card verification output
		this.printOutput();
		
		// close log file for this card
		try {
			fw.close();
		} catch (IOException e) {
			System.out.printf("ERROR closing log file %s\n", filePath);
		    e.printStackTrace();
		    System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		/**
		 * Verify credit cards.
		 */
		
		// Make directory to log processing details for all cards
		makeLogDir();
		
		outputArray = new String[args.length];
		List<Thread> threadList = new ArrayList<Thread>();
		for (int i = 0; i < args.length; i++) {
			CredVerify c = new CredVerify(args[i], i);
			c.createFile();
			
			// Make a thread to verify each credit card
			Thread verifyCCThread = new Thread(){
				@Override
				public void run() {
					c.verifyCreditCard();
				}
			};
			
			verifyCCThread.start();
			threadList.add(verifyCCThread);
		}
		
		// Join threads for all cards
		for (Thread t : threadList) {
			try {
				t.join();
			} catch (InterruptedException e) {
				System.out.printf("InterruptedException for "
						+ "%s join(): %s\n", t.getName(), e);
			}
		}
		
		// Print verification results for all cards to console
		System.out.println("\nVERIFICATION RESULTS:");
		for (String o : outputArray) {
			System.out.print(o);
		}
		System.out.println();
	}
}
