package fi.aalto.cs.drumbeat.rest.managers;

public class LoggerUtil {
	
	/**
	 * Get the method name for a depth in call stack. <br />
	 * Utility function
	 * 
	 * @param depth
	 *            depth in the call stack (0 means current method, 1 means call
	 *            method, ...)
	 * @return method name
	 */
	public static String getMethodName(final int methodCallShift) {
		// TODO: Remove this method to drumbeat-common
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		return ste[methodCallShift + 2].getMethodName();
	}

}
