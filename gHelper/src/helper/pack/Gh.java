package helper.pack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;

public class Gh {

	public static String[] logArray = new String[1000];
	private static String[] logArrayTmp = new String[1000];
	private static short logArrayI = 0, logArrayIprinted = 0;

	public static SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
	public SimpleDateFormat ft1 = new SimpleDateFormat("H:mm:ss.SSS");
	public static SimpleDateFormat ft2 = new SimpleDateFormat("yyyy-MM-dd H:mm:ss.SSS");
	public static SimpleDateFormat ft3 = new SimpleDateFormat("H:mm:ss");
	public static SimpleDateFormat ft4 = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
	private static SimpleDateFormat ft5 = new SimpleDateFormat("yyyy-MM");
	public DecimalFormat df2 = new DecimalFormat("#.##");

	public static int logArrayItmp = 0;
	public boolean shuttingDown = false;

	// i added

	public static void runPrintLogToConsole() {
		prnt("Ghelper: runGhelper Start");
		Thread printLogToConsoleThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					printLogToConsole();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		printLogToConsoleThread.setPriority(Thread.MIN_PRIORITY);
		printLogToConsoleThread.start();

		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
		DecimalFormatSymbols custom = new DecimalFormatSymbols();
		custom.setDecimalSeparator('.');
		format.setDecimalFormatSymbols(custom);

		prnt("Ghelper: runGhelper End");
	};

	public String t() {
		return ft1.format(System.currentTimeMillis());
	}

	public String tt() {
		return ft3.format(System.currentTimeMillis());
	}

	public String tl() {
		return ft2.format(System.currentTimeMillis());
	}

	public static String td() {
		return ft.format(System.currentTimeMillis());
	}

	public static String td2() {
		return ft5.format(System.currentTimeMillis());
	}

	public String f2(float value) {
		String result = "77";

		try {
			result = String.format("%.2f", value);
		} catch (Exception e) {
			prnte("f2, e=" + e.getMessage());
		}

		return result;
	}

	public String f2(double value) {
		String result = "77";

		try {
			result = String.format("%.2f", value);
		} catch (Exception e) {
			prnte("f2, e=" + e.getMessage());
		}

		return result;
	}

	public String f5(float value) {
		String result = "77";

		try {
			result = String.format("%.5f", value);
		} catch (Exception e) {
			prnte("f5, e=" + e.getMessage());
		}

		return result;
	}

	public String f5(double value) {
		String result = "77";

		try {
			result = String.format("%.5f", value);
		} catch (Exception e) {
			prnte("f5, e=" + e.getMessage());
		}

		return result;
	}

// writers Start
	private synchronized static void printLogToConsole() {
		for (short i = logArrayIprinted; i < logArrayI; i++) {

			if (logArray[i] != null && logArray[i] != "" && !logArray[i].contains("[speed]") && !logArray[i].contains("[debug]")) {

				String text = logArray[i].replace("[prices]", "");
				text = text.substring(11);
				text = text.replace(";", " ");

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

		String text2 = null;
		try {
			text2 = ft2.format(System.currentTimeMillis()) + ";[ok] " + text;
			logArray[logArrayI] = text2;
			logArrayI++;
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (logArrayI > 800 || text.contains("[write]")) {
			logArrayItmp = logArrayI;
			logArrayI = 0;

			logArrayTmp = logArray.clone();
			for (int i = 0; i < logArray.length; i++) {
				logArray[i] = null;
			}

			Thread writeAllThread = new Thread(new Runnable() {
				@Override
				public void run() {
					writeAll();
				}
			});
			writeAllThread.setPriority(Thread.MIN_PRIORITY);
			writeAllThread.start();
		}
	}

	public synchronized static void prnte(String text) {
		String text2 = null;
		try {
			text2 = ft2.format(System.currentTimeMillis()) + ";[err]" + text;
			logArray[logArrayI] = text2;
			logArrayI++;
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (logArrayI > 800 || text.contains("[write]")) {
			logArrayItmp = logArrayI;
			logArrayI = 0;

			logArrayTmp = logArray.clone();
			for (int i = 0; i < logArray.length; i++) {
				logArray[i] = null;
			}

			Thread writeAllThread = new Thread(new Runnable() {
				@Override
				public void run() {
					writeAll();
				}
			});
			writeAllThread.setPriority(Thread.MIN_PRIORITY);
			writeAllThread.start();
		}
	}

	private synchronized static void writeAll() {
		System.out.println("writeAll Start");
		try {
			String fs = File.separator;
			String folderName = "C:" + fs + "log_skudra" + fs + getPcAndUserName() + fs;

			System.out.println("writeAll folderName=" + folderName);
			for (short i = 0; i < logArrayItmp; i++) {

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
						writeLine(logArrayTmp[i], folderName, td() + "_" + getRunningFileName() + "_[err].txt");
					} catch (Exception e) {
						System.err.println("writeAll writeLine 2, e=" + e.getMessage());
					}
				} else if (logArrayTmp[i].contains("[speed]")) {
					try {
						writeLine(logArrayTmp[i], folderName, td() + "_" + getRunningFileName() + "_[speed].txt");
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

}
