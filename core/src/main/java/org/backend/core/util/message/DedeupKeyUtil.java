package org.backend.core.util.message;

public class DedeupKeyUtil {
	
	private static final String prefix = "INVOICE:";
	
	public static String generate(Long invoiceId, String channel) {
		return prefix + String.valueOf(invoiceId) + ":" + channel;
	}

}
