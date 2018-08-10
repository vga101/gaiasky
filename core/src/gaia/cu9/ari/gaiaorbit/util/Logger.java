package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;

public class Logger {

	public enum LoggerLevel {
		ERROR(0), WARN(1), INFO(2), DEBUG(3);

		public int val;

		LoggerLevel(int val) {
			this.val = val;
		}

		public int getVal() {
			return val;
		}

	}

	public static LoggerLevel level = LoggerLevel.INFO;

	public static void error(Throwable t, String tag) {
		if (inLevel(LoggerLevel.ERROR))
			if (EventManager.instance.hasSubscriptors(Events.JAVA_EXCEPTION)) {
				EventManager.instance.post(Events.JAVA_EXCEPTION, t, tag);
			} else {
				System.err.println(tag);
				t.printStackTrace(System.err);
			}
	}

	public static void error(Throwable t) {
		if (inLevel(LoggerLevel.ERROR))
			if (EventManager.instance.hasSubscriptors(Events.JAVA_EXCEPTION)) {
				EventManager.instance.post(Events.JAVA_EXCEPTION, t);
			} else {
				t.printStackTrace(System.err);
			}
	}

	public static void error(String... messages) {
		if (inLevel(LoggerLevel.ERROR))
			if (EventManager.instance.hasSubscriptors(Events.JAVA_EXCEPTION)) {
				EventManager.instance.post(Events.POST_NOTIFICATION, (Object[]) messages);
			}
	}

	public static void warn(Object... messages) {
		if (inLevel(LoggerLevel.WARN))
			EventManager.instance.post(Events.POST_NOTIFICATION, messages);
	}

	public static void warnparse(String msg, Object... args) {
		if (inLevel(LoggerLevel.WARN)) {
			msg = parse(msg, args);
			EventManager.instance.post(Events.POST_NOTIFICATION, new Object[] { msg });
		}

	}

	public static void info(Object... messages) {
		if (inLevel(LoggerLevel.INFO))
			EventManager.instance.post(Events.POST_NOTIFICATION, messages);
	}

	public static void infoparse(String msg, Object... args) {
		if (inLevel(LoggerLevel.INFO)) {
			msg = parse(msg, args);
			EventManager.instance.post(Events.POST_NOTIFICATION, new Object[] { msg });
		}

	}

	private static String parse(String msg, Object... args) {
		int n = args.length;
		for (int i = 0; i < n; i++) {
			String arg = args[i] != null ? args[i].toString() : "null";
			msg = msg.replaceFirst("\\{\\}", arg);
		}
		return msg;

	}

	public static void debug(Object... messages) {
		if (inLevel(LoggerLevel.DEBUG))
			EventManager.instance.post(Events.POST_NOTIFICATION, messages);
	}

	private static boolean inLevel(LoggerLevel l) {
		return l.getVal() <= level.getVal();
	}

}
