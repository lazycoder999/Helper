package helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class Glog {
	
	private static boolean	whileRunning	= true;
	
	public static String[]	logArray		= new String[1000];
	public static long[]	logArrayTime	= new long[1000];
	private static String[]	logArrayTmp		= new String[1000];
	public static long[]	logArrayTimeTmp	= new long[1000];
	private static short	logArrayI		= 0, logArrayIprinted = 0;
	public static int		logArrayItmp	= 0;
	public static String	globalVar1		= "";
	public static String	globalVar2		= "";
	
	public Glog() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				whileRunning = false;
			}
		});
		
	}
	
	public synchronized static void runPrintLogToConsole() {
		if (Gh.getThreadByName("printLogToConsoleThread") == null) {
			prnt("Ghelper: runGhelper Start");
			Thread printLogToConsoleThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (whileRunning) {
						printLogToConsole();
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			printLogToConsoleThread.setPriority(Thread.MIN_PRIORITY);
			printLogToConsoleThread.setName("printLogToConsoleThread");
			printLogToConsoleThread.start();
			
			DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
			DecimalFormatSymbols custom = new DecimalFormatSymbols();
			custom.setDecimalSeparator('.');
			format.setDecimalFormatSymbols(custom);
			
			prnt("Ghelper: runGhelper End");
		}
	};
	
	private synchronized static void printLogToConsole() {
		for (short i = logArrayIprinted; i < logArrayI; i++) {
			
			if (logArray[i] != null && logArray[i] != "" && !logArray[i].contains("[speed]") && !logArray[i].contains("[debug]")
					&& !logArray[i].contains("[prices]") && !logArray[i].contains("[tickstat]")) {
				
				String text = logArray[i].replace("[prices]", "").replace("[ok]", "");
				//text = text.substring(11);
				text = text.replace(";", " ");
				text = Gh.ft1.format(logArrayTime[i]) + text;
				
				if (logArray[i].contains("[err]")) {
					System.err.println(text);
				} else {
					System.out.println(text);
				}
			}
		}
		logArrayIprinted = logArrayI;
	}
	
	private synchronized static void writeLine(String text, String folderName, String fileName) {
		// System.out.println("writing Start");
		// System.out.println("text=" + text);
		// System.out.println("folderName=" + folderName);
		// System.out.println("fileName=" + fileName);
		
		// directWriter("text=" + text + " folderName=" + folderName +
		// " fileName=" + fileName);
		
		try {
			// System.out.println("fullFileName=" + fullFileName);
			String fullFileName = folderName + fileName;
			File file = new File(fullFileName);
			// System.out.println("fullFileName=" + fullFileName);
			
			try {
				if (!file.exists()) {
					file.getParentFile().mkdirs();
				}
			} catch (Exception e) {
				System.err.println("error chekcing directories" + e.getMessage());
			}
			
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
			} catch (Exception e) {
				System.err.println("error chekcing file" + e.getMessage());
			}
			
			Writer w = null;
			try {
				@SuppressWarnings("resource")
				FileOutputStream is = new FileOutputStream(file, true);
				OutputStreamWriter osw = new OutputStreamWriter(is, "UTF-8");
				w = new BufferedWriter(osw);
			} catch (Exception e) {
				System.err.println("filing stuff error" + e.getMessage());
			}
			
			text = text.replaceAll(",", ".");
			
			w.write(text + '\n');
			w.close();
		} catch (IOException e) {
			System.err.println("Problem writing to the file" + e.getMessage());
		}
		// prnt("writing End");
	}
	
	public synchronized static void prnt(String text) {
		
		try {
			logArrayTime[logArrayI] = System.currentTimeMillis();
			logArray[logArrayI] = ";[ok] " + text;
			logArrayI++;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (logArrayI > 800 || text.contains("[write]")) {
			logArrayItmp = logArrayI;
			logArrayI = 0;
			
			logArrayTmp = logArray.clone();
			logArrayTimeTmp = logArrayTime.clone();
			for (int i = 0; i < logArray.length; i++) {
				logArray[i] = null;
				logArrayTime[i] = 0;
			}
			
			Thread writeAllThread = new Thread(new Runnable() {
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
	
	public synchronized static void prnte(String text) {
		
		try {
			logArrayTime[logArrayI] = System.currentTimeMillis();
			logArray[logArrayI] = ";[err] " + text;
			logArrayI++;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (logArrayI > 800 || text.contains("[write]")) {
			logArrayItmp = logArrayI;
			logArrayI = 0;
			
			logArrayTmp = logArray.clone();
			logArrayTimeTmp = logArrayTime.clone();
			for (int i = 0; i < logArray.length; i++) {
				logArray[i] = null;
				logArrayTime[i] = 0;
			}
			
			Thread writeAllThread = new Thread(new Runnable() {
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
			String fs = File.separator;
			
			globalVar1 = globalVar1.replaceAll(globalVar2, "");
			globalVar1 = globalVar1.replaceAll("M1", "").replaceAll("M5", "").replaceAll("M15", "").replaceAll("M30", "");
			globalVar1 = globalVar1.replaceAll("H1", "").replaceAll("H4", "").replaceAll("D1", "").replaceAll("W1", "").replaceAll("MN", "");
			
			String folderName = "C:" + fs + "log_skudra" + fs + Gh.getPcAndUserName() + fs + globalVar1.replaceAll("[^a-zA-Z0-9.-]", "") + "_";
			
			System.out.println("writeAll folderName=" + folderName);
			for (short i = 0; i < logArrayItmp; i++) {
				
				logArrayTmp[i] = Gh.ft2.format(logArrayTimeTmp[i]) + logArrayTmp[i];
				if (logArrayTmp[i].contains("[prices]")) {
					try {
						String textTmp = logArrayTmp[i].replace("[ok] ", "");
						textTmp = textTmp.replace("[prices]", "");
						textTmp = textTmp.replace("[err] ", "");
						writeLine(textTmp, folderName, Gh.td2() + "_" + Gh.getRunningFileName() + "_[prices].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[err]")) {
					try {
						writeLine(logArrayTmp[i], folderName, Gh.getRunningFileName() + "_[err].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[speed]")) {
					try {
						writeLine(logArrayTmp[i], folderName, Gh.getRunningFileName() + "_[speed].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[op]")) {
					try {
						writeLine(logArrayTmp[i], folderName, Gh.getRunningFileName() + "_[operations].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[tickstat]")) {
					try {
						writeLine(logArrayTmp[i], folderName, Gh.getRunningFileName() + "_[tickstat].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[tradeStat]")) {
					try {
						writeLine(logArrayTmp[i], folderName, Gh.getRunningFileName() + "_[tradeStat].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
					
				} else if (logArrayTmp[i].contains(";signal;")) {
					try {
						writeLine(logArrayTmp[i], folderName, Gh.getRunningFileName() + "_[sigFills].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				}
				
				try {
					writeLine(logArrayTmp[i], folderName, Gh.td() + "_" + Gh.getRunningFileName() + "_[all].txt");
				} catch (Exception e) {
					System.err.println("writeAll writeLine 1, e=" + e.getMessage());
				}
				
			}
		} catch (Exception e) {
			prnte("writeAll whole method, e=" + e.getMessage());
		}
		System.out.println("writeAll End");
	}
	
	public synchronized void directWriter(String text) {
		try {
			
			String fs = File.separator;
			String folderName = "C:" + fs + "log_skudra" + fs + Gh.getPcAndUserName() + fs;
			File file = new File(folderName + Gh.td() + Gh.getRunningFileName() + "_[exceptions].log");
			
			try {
				if (!file.exists()) {
					file.getParentFile().mkdirs();
				}
			} catch (Exception e) {
				System.err.println("error chekcing directories" + e.getMessage());
			}
			
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
			} catch (Exception e) {
				System.err.println("error chekcing file" + e.getMessage());
			}
			
			Writer w = null;
			try {
				@SuppressWarnings("resource")
				FileOutputStream is = new FileOutputStream(file, true);
				OutputStreamWriter osw = new OutputStreamWriter(is, "UTF-8");
				w = new BufferedWriter(osw);
			} catch (Exception e) {
				System.err.println("filing stuff error" + e.getMessage());
			}
			
			w.write(Gh.ft2.format(System.currentTimeMillis()) + text + '\n');
			w.close();
		} catch (IOException e) {
			System.err.println("Problem writing to the file" + e.getMessage());
		}
	}
}
