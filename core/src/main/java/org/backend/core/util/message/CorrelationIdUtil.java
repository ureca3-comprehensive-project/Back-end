package org.backend.core.util.message;

public class CorrelationIdUtil {
	
	private static final String prefix = "MSG-";
	

	public static String generate(Long invoiceId, String billingMonth) {
		
		return prefix + billingMonth + invoiceId;
	}

}
