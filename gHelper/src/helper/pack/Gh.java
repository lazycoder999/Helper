package helper.pack;

import java.awt.Window;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Set;

public class Gh {
	
	public static String[]			logArray		= new String[1000];
	public static long[]			logArrayTime	= new long[1000];
	private static String[]			logArrayTmp		= new String[1000];
	public static long[]			logArrayTimeTmp	= new long[1000];
	private static short			logArrayI		= 0, logArrayIprinted = 0;
	
	public static SimpleDateFormat	ft				= new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat	ft1				= new SimpleDateFormat("H:mm:ss.SSS");
	public static SimpleDateFormat	ft2				= new SimpleDateFormat("yyyy-MM-dd H:mm:ss.SSS");
	public SimpleDateFormat			ft3				= new SimpleDateFormat("H:mm:ss");
	public SimpleDateFormat			ft4				= new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
	private static SimpleDateFormat	ft5				= new SimpleDateFormat("yyyy-MM");
	public DecimalFormat			df2				= new DecimalFormat("#.##");
	
	public static int				logArrayItmp	= 0;
	public boolean					shuttingDown	= false;
	
	public static String			globalVar1		= "";
	public static String			globalVar2		= "";
	
	private static boolean			whileRunning	= true;
	
	public Gh() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				whileRunning = false;
			}
		});
	}
	
	public static void runPrintLogToConsole() {
		if (getThreadByName("printLogToConsoleThread") == null) {
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
	
	public String t() {
		return ft1.format(System.currentTimeMillis());
	}
	
	public String tt() {
		return ft3.format(System.currentTimeMillis());
	}
	
	public static String td() {
		return ft.format(System.currentTimeMillis());
	}
	
	public static String td2() {
		return ft5.format(System.currentTimeMillis());
	}
	
// writers Start
	private synchronized static void printLogToConsole() {
		for (short i = logArrayIprinted; i < logArrayI; i++) {
			
			if (logArray[i] != null && logArray[i] != "" && !logArray[i].contains("[speed]") && !logArray[i].contains("[debug]")
					&& !logArray[i].contains("[tickstat]")) {
				
				String text = logArray[i].replace("[prices]", "").replace("[ok]", "");
				//text = text.substring(11);
				text = text.replace(";", " ");
				text = ft1.format(logArrayTime[i]) + text;
				
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
			
			String folderName = "C:" + fs + "log_skudra" + fs + getPcAndUserName() + fs + globalVar1.replaceAll("[^a-zA-Z0-9.-]", "") + "_";
			
			System.out.println("writeAll folderName=" + folderName);
			for (short i = 0; i < logArrayItmp; i++) {
				
				logArrayTmp[i] = ft2.format(logArrayTimeTmp[i]) + logArrayTmp[i];
				if (logArrayTmp[i].contains("[prices]")) {
					try {
						String textTmp = logArrayTmp[i].replace("[ok] ", "");
						textTmp = textTmp.replace("[prices]", "");
						textTmp = textTmp.replace("[err] ", "");
						writeLine(textTmp, folderName, td2() + "_" + getRunningFileName() + "_[prices].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[err]")) {
					try {
						writeLine(logArrayTmp[i], folderName, getRunningFileName() + "_[err].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[speed]")) {
					try {
						writeLine(logArrayTmp[i], folderName, getRunningFileName() + "_[speed].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[op]")) {
					try {
						writeLine(logArrayTmp[i], folderName, getRunningFileName() + "_[operations].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[tickstat]")) {
					try {
						writeLine(logArrayTmp[i], folderName, getRunningFileName() + "_[tickstat].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains(";signal;")) {
					try {
						writeLine(logArrayTmp[i], folderName, getRunningFileName() + "_[sigFills].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				}
				
				try {
					writeLine(logArrayTmp[i], folderName, td() + "_" + getRunningFileName() + "_[all].txt");
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
			String folderName = "C:" + fs + "log_skudra" + fs + getPcAndUserName() + fs;
			File file = new File(folderName + td() + getRunningFileName() + "_[exceptions].log");
			
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
			
			w.write(ft2.format(System.currentTimeMillis()) + text + '\n');
			w.close();
		} catch (IOException e) {
			System.err.println("Problem writing to the file" + e.getMessage());
		}
	}
	
// writers End
	
	public static String getJarFolder(CodeSource codeSource) {
		
		File jarFile = null;
		
		try {
			jarFile = new File(codeSource.getLocation().toURI().getPath());
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String jarDir = jarFile.getParentFile().getPath();
		jarDir = jarDir.replace("\\", "/");
		return jarDir;
	}
	
	public String gDecoder(String text, Byte position) {
		// prnt("decoder received: " + text);
		String[] tokens = null;
		try {
			tokens = text.split(";");
		} catch (Exception e) {
			prnte("gDecoder, error, text=" + text + ", e=" + e.getMessage());
		}
		
		if (tokens != null) {
			// prnt("decoded: " + tokens[position]);
			return tokens[position];
		} else {
			return "error, cannot decode";
		}
	}
	
	public String fileDateModified(String fullFileName) {
		File file = new File(fullFileName);
		prnt("fileDateModified: fullFileName=" + fullFileName);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(file.lastModified());
	}
	
	public static String getRunningFileName() {
		String path = Gh.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = null;
		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// System.out.println("decodedPath=" + decodedPath);
		File f = new File(decodedPath);
		// System.out.println(f.getName());
		
		return f.getName();
	}
	
	public static String getPcAndUserName() {
		String userName = "error cannot get user name";
		
		try {
			userName = System.getProperty("user.name");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String pcName = "error cannot get pc name";
		
		try {
			pcName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		pcName = pcName + "#" + userName;
		
		return pcName;
	}
	
	public Integer getInteger(String text) {
		try {
			return Integer.valueOf(text);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public BigDecimal getBigDecimal(String text) {
		try {
			return new BigDecimal(text);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public Boolean getBoolean(Object object, String key) {
		
		Boolean booleanValue = null;
		
		try {
			booleanValue = Boolean.valueOf((String) object);
		} catch (Exception e) {
			prnte("LoadSett2: getBoolean, problem setting: " + key.toString() + "=" + object.toString() + e.getMessage());
		}
		
		return booleanValue;
	}
	
	public String getString(Object object, String key) {
		
		String string = null;
		
		try {
			string = String.valueOf((String) object);
		} catch (Exception e) {
			prnte("LoadSett2: getString, problem setting: " + key.toString() + "=" + object.toString() + " e=" + e.getMessage());
		}
		
		return string;
	}
	
	public Integer getInteger(Object object, String key) {
		
		Integer intNumber = null;
		
		try {
			intNumber = Integer.valueOf((String) object);
		} catch (Exception e) {
			prnte("LoadSett2: getInteger, problem setting: " + key.toString() + "=" + object.toString() + " e=" + e.getMessage());
		}
		
		return intNumber;
	}
	
	public Float getFloat(Object object, String key) {
		
		Float floatNumber = null;
		
		try {
			floatNumber = Float.valueOf((String) object);
			
		} catch (Exception e) {
			prnte("LoadSett2: getFloat, problem setting: " + key.toString() + "=" + object.toString() + " e=" + e.getMessage());
		}
		
		return floatNumber;
	}
	
	public static void runSystemCommand(String command) {
		
		try {
			Runtime.getRuntime().exec(command);
//			BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//			String s = "";
//			// reading output stream of the command
//			while ((s = inputStream.readLine()) != null) {
//				prnt("[ping]" + s);
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Window getSelectedWindow(Window[] windows) {
		
		Window result = null;
		for (int i = 0; i < windows.length; i++) {
			Window window = windows[i];
			if (window.isActive()) {
				result = window;
			} else {
				Window[] ownedWindows = window.getOwnedWindows();
				if (ownedWindows != null) {
					result = getSelectedWindow(ownedWindows);
				}
			}
		}
		return result;
	}
	
	public double roundDouble(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();
		
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	public String gRound(Double notRoundedNbr, Integer digits) {
		if (notRoundedNbr != null) {
			String roundedNbr = new BigDecimal(notRoundedNbr).setScale(digits, RoundingMode.HALF_UP).toString();
			return roundedNbr;
		} else {
			return null;
		}
	}
	
	public String gRound(Float notRoundedNbr, Integer digits) {
		if (notRoundedNbr != null) {
			String roundedNbr = new BigDecimal(notRoundedNbr).setScale(digits, RoundingMode.HALF_UP).toString();
			return roundedNbr;
		} else {
			return null;
		}
	}
	
	public String gRound(BigDecimal notRoundedNbr, Integer digits) {
		if (notRoundedNbr != null) {
			String roundedNbr = notRoundedNbr.setScale(digits, RoundingMode.HALF_UP).toString();
			return roundedNbr;
		} else {
			return null;
		}
	}
	
	public static Thread getThreadByName(String threadName) {
		Thread __tmp = null;
		
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		
		for (int i = 0; i < threadArray.length; i++) {
			if (threadArray[i].getName().equals(threadName))
				__tmp = threadArray[i];
		}
		
		return __tmp;
	}
	
	private static final int	POW10[]	= { 1, 10, 100, 1000, 10000, 100000, 1000000 };
	
	public String g2Round(double val, int precision) {
		StringBuilder sb = new StringBuilder();
		if (val < 0) {
			sb.append('-');
			val = -val;
		}
		int exp = POW10[precision];
		long lval = (long) (val * exp + 0.5);
		sb.append(lval / exp).append('.');
		long fval = lval % exp;
		for (int p = precision - 1; p > 0 && fval < POW10[p]; p--) {
			sb.append('0');
		}
		sb.append(fval);
		return sb.toString();
	}
}
