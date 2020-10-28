package nssac.utility;

import java.time.LocalTime;

public class LogUtility {
	
	public void logPrinter(LocalTime timestamp, String stmt) {
		System.out.println(">> " + timestamp + " : " + stmt);
	}
	
	public void errorPrinter(LocalTime timestamp, String stmt) {
		System.out.println("***** ERROR ***** " + timestamp + " : " + stmt);
	}
	
	public void debugPrinter(LocalTime timestamp, String stmt) {
		System.out.println(" ## DEBUG ## " + timestamp + " : " + stmt);
	}

	public void warningPrinter(LocalTime timestamp, String stmt) {
		System.out.println("*WARNING* " + timestamp + " : " + stmt);
	}

}
