package helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;

public class Glog {
	
	private static boolean			whileRunning	= true;
	
	public static String[]			logArray		= new String[10000];
	public static LocalDateTime[]	logArrayTime	= new LocalDateTime[10000];
	public static LocalDateTime[]	logArrayTimeTmp	= new LocalDateTime[10000];
	private static String[]			logArrayTmp		= new String[10000];
	private static short			logArrayI		= 0, logArrayIprinted = 0;
	public static int				logArrayItmp	= 0;
	
	public static String			globalVar1		= "";
	public static String			globalVar2		= "";
	private static Gh				gh				= new Gh();
	
	public Glog() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				whileRunning = false;
			}
		});
		
	}
	
	public synchronized static void runPrintLogToConsole() {
		if (gh.getThreadByName("printLogToConsoleThread") == null) {
			prnt("Ghelper: runGhelper Start");
			final Thread printLogToConsoleThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (whileRunning) {
						printLogToConsole();
						try {
							Thread.sleep(5);
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			printLogToConsoleThread.setPriority(Thread.MIN_PRIORITY);
			printLogToConsoleThread.setName("printLogToConsoleThread");
			printLogToConsoleThread.start();
			
			final DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
			final DecimalFormatSymbols custom = new DecimalFormatSymbols();
			custom.setDecimalSeparator('.');
			format.setDecimalFormatSymbols(custom);
			
			prnt("Ghelper: runGhelper End");
		}
	};
	
	private synchronized static void printLogToConsole() {
		for (short i = logArrayIprinted; i < logArrayI; i++) {
			
			if (logArray[i] != null /*&& logArray[i] != "" && !logArray[i].contains("[speed]")*/&& !logArray[i].contains("[debug]")
					&& !logArray[i].contains("[prices]") && !logArray[i].contains("[tickstat]")) {
				
				//String text = logArray[i].replace("[prices]", "").replace("[ok]", "");
				String text = logArray[i];
				//text = text.substring(11);
				//text = text.replace(";", " ");
				text = gh.gTime("H:mm:ss.SSS", logArrayTime[i]) + text;
				if (logArray[i].contains("[err]")) {
					System.err.println(text);
				} else {
					System.out.println(text);
				}
			}
		}
		logArrayIprinted = logArrayI;
	}
	
	public synchronized static void writeLine(String text, final String folderName, final String fileName) {
		// System.out.println("writing Start");
		// System.out.println("text=" + text);
		// System.out.println("folderName=" + folderName);
		// System.out.println("fileName=" + fileName);
		
		// directWriter("text=" + text + " folderName=" + folderName +
		// " fileName=" + fileName);
		
		try {
			// System.out.println("fullFileName=" + fullFileName);
			final String fullFileName = folderName + fileName;
			final File file = new File(fullFileName);
			// System.out.println("fullFileName=" + fullFileName);
			
			try {
				if (!file.exists()) {
					file.getParentFile().mkdirs();
				}
			} catch (final Exception e) {
				System.err.println("error chekcing directories" + e.getMessage());
			}
			
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
			} catch (final Exception e) {
				System.err.println("error chekcing file" + e.getMessage());
			}
			
			Writer w = null;
			try {
				@SuppressWarnings("resource")
				final FileOutputStream is = new FileOutputStream(file, true);
				final OutputStreamWriter osw = new OutputStreamWriter(is, "UTF-8");
				w = new BufferedWriter(osw);
			} catch (final Exception e) {
				System.err.println("filing stuff error" + e.getMessage());
			}
			
			text = text.replaceAll(",", ".");
			if (w != null) {
				w.write(text + '\n');
				w.close();
			} else {
				System.err.println("w = null");
			}
		} catch (final IOException e) {
			System.err.println("Problem writing to the file" + e.getMessage());
		}
		// prnt("writing End");
	}
	
	public synchronized static void prnt(final String text) {
		
		try {
			logArrayTime[logArrayI] = LocalDateTime.now();
			logArray[logArrayI] = ";[ok] " + text;
			logArrayI++;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		
		if (logArrayI > 9500 || text.contains("[write]")) {
			logArrayItmp = logArrayI;
			logArrayI = 0;
			
			logArrayTmp = logArray.clone();
			logArrayTimeTmp = logArrayTime.clone();
			for (int i = 0; i < logArray.length; i++) {
				logArray[i] = null;
				logArrayTime[i] = null;
			}
			
			final Thread writeAllThread = new Thread(new Runnable() {
				@Override
				public void run() {
					writeAll();
				}
			});
			writeAllThread.setPriority(Thread.MIN_PRIORITY);
			writeAllThread.setName("writeAllThread");
			writeAllThread.start();
		}
	}
	
	public synchronized static void prnte(final String text) {
		
		try {
			logArrayTime[logArrayI] = LocalDateTime.now();
			logArray[logArrayI] = ";[err] " + text;
			logArrayI++;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		
		if (logArrayI > 9500 || text.contains("[write]")) {
			logArrayItmp = logArrayI;
			logArrayI = 0;
			
			logArrayTmp = logArray.clone();
			logArrayTimeTmp = logArrayTime.clone();
			for (int i = 0; i < logArray.length; i++) {
				logArray[i] = null;
				logArrayTime[i] = null;
			}
			
			final Thread writeAllThread = new Thread(new Runnable() {
				@Override
				public void run() {
					writeAll();
				}
			});
			writeAllThread.setPriority(Thread.MIN_PRIORITY);
			writeAllThread.setName("writeAllThread");
			writeAllThread.start();
		}
	}
	
	private synchronized static void writeAll() {
		System.out.println("writeAll Start");
		try {
			final String fs = File.separator;
			
			globalVar1 = globalVar1.replaceAll(globalVar2, "");
			globalVar1 = globalVar1.replaceAll("M1", "").replaceAll("M5", "").replaceAll("M15", "").replaceAll("M30", "");
			globalVar1 = globalVar1.replaceAll("H1", "").replaceAll("H4", "").replaceAll("D1", "").replaceAll("W1", "").replaceAll("MN", "");
			
			final String folderName = "C:" + fs + "log_skudra" + fs + gh.getPcAndUserName() + fs + globalVar1.replaceAll("[^a-zA-Z0-9.-]", "") + "_";
			
			System.out.println("writeAll folderName=" + folderName);
			for (short i = 0; i < logArrayItmp; i++) {
				
				logArrayTmp[i] = gh.gTime("yyyy-MM-dd H:mm:ss.SSS", logArrayTimeTmp[i]) + logArrayTmp[i];
				if (logArrayTmp[i].contains("[prices]")) {
					try {
						String textTmp = logArrayTmp[i].replace("[ok] ", "");
						textTmp = textTmp.replace("[prices]", "");
						textTmp = textTmp.replace("[err] ", "");
						writeLine(textTmp, folderName, gh.gTime("yyyy-MM", LocalDateTime.now()) + "_" + gh.getRunningFileName() + "_[prices].txt");
					} catch (final Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[err]")) {
					try {
						writeLine(logArrayTmp[i], folderName, gh.getRunningFileName() + "_[err].txt");
					} catch (final Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[speed]")) {
					try {
						writeLine(logArrayTmp[i], folderName, gh.getRunningFileName() + "_[speed].txt");
					} catch (final Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[op]")) {
					try {
						writeLine(logArrayTmp[i], folderName, gh.getRunningFileName() + "_[operations].txt");
					} catch (final Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[tickstat]")) {
					try {
						writeLine(logArrayTmp[i], folderName, gh.getRunningFileName() + "_[tickstat].txt");
					} catch (final Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[tradeStat]")) {
					try {
						writeLine(logArrayTmp[i], folderName, gh.getRunningFileName() + "_[tradeStat].txt");
					} catch (final Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains(";signal;")) {
					try {
						writeLine(logArrayTmp[i], folderName, gh.getRunningFileName() + "_[sigFills].txt");
					} catch (final Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[filename]")) {
					try {
						writeLine(logArrayTmp[i], folderName, gh.getRunningFileName() + "_" + gh.gDecoder(logArrayTmp[i], (byte) 0) + ".txt");
					} catch (final Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
					
				}
				
				try {
					writeLine(logArrayTmp[i], folderName, gh.gTime("yyyy-MM-dd", LocalDateTime.now()) + "_" + gh.getRunningFileName() + "_[all].txt");
				} catch (final Exception e) {
					System.err.println("writeAll writeLine 1, e=" + e.getMessage());
				}
				
			}
		} catch (final Exception e) {
			prnte("writeAll whole method, e=" + e.getMessage());
		}
		System.out.println("writeAll End");
	}
	
	public synchronized void directWriter(final String text) {
		try {
			
			final String fs = File.separator;
			final String folderName = "C:" + fs + "log_skudra" + fs + gh.getPcAndUserName() + fs;
			final File file = new File(folderName + gh.gTime("yyyy-MM-dd", LocalDateTime.now()) + gh.getRunningFileName() + "_[exceptions].log");
			
			try {
				if (!file.exists()) {
					file.getParentFile().mkdirs();
				}
			} catch (final Exception e) {
				System.err.println("error chekcing directories" + e.getMessage());
			}
			
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
			} catch (final Exception e) {
				System.err.println("error chekcing file" + e.getMessage());
			}
			
			Writer w = null;
			try {
				@SuppressWarnings("resource")
				final FileOutputStream is = new FileOutputStream(file, true);
				final OutputStreamWriter osw = new OutputStreamWriter(is, "UTF-8");
				w = new BufferedWriter(osw);
			} catch (final Exception e) {
				System.err.println("filing stuff error" + e.getMessage());
			}
			
			if (w != null) {
				w.write(gh.gTime("yyyy-MM-dd H:mm:ss.SSS", LocalDateTime.now()) + text + '\n');
				w.close();
			} else {
				System.err.println("directWriter w = null");
			}
		} catch (final IOException e) {
			System.err.println("Problem writing to the file" + e.getMessage());
		}
	}
}
