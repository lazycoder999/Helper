package helper;

import java.awt.Window;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.CodeSource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class Gh {
	
	public DecimalFormat	df2				= new DecimalFormat("#.##");
	public boolean			shuttingDown	= false;
	
	public String gTime(final String dateTimeFormat, final LocalDateTime localDateTime) {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
		final String f = formatter.format(localDateTime);
		return f;
	}
	
	public String getJarFolder(final CodeSource codeSource) {
		
		File jarFile = null;
		
		try {
			jarFile = new File(codeSource.getLocation().toURI().getPath());
			
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		if (jarFile != null) {
			String jarDir = jarFile.getParentFile().getPath();
			jarDir = jarDir.replace("\\", "/");
			return jarDir;
		} else {
			Glog.prnt("getJarFolder = null");
			return null;
		}
	}
	
	public String gDecoder(final String text, final Byte position) {
		// prnt("decoder received: " + text);
		String[] tokens = null;
		try {
			tokens = text.split(";");
		} catch (final Exception e) {
			Glog.prnte("gDecoder, error, text=" + text + ", e=" + e.getMessage());
		}
		
		if (tokens != null) {
			// prnt("decoded: " + tokens[position]);
			return tokens[position];
		} else {
			return "error, cannot decode";
		}
	}
	
	public String fileDateModified(final String fullFileName) {
		final File file = new File(fullFileName);
		Glog.prnt("fileDateModified: fullFileName=" + fullFileName);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(file.lastModified());
	}
	
	public String getRunningFileName() {
		final String path = Gh.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = null;
		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// System.out.println("decodedPath=" + decodedPath);
		
		File f = null;
		
		if (decodedPath != null) {
			f = new File(decodedPath);
		} else {
			Glog.prnte("getRunningFileName decodedPath = null");
		}
		
		if (f != null) {
			return f.getName();
		} else {
			Glog.prnt("getRunningFileName file = null");
			return null;
		}
	}
	
	public String getPcAndUserName() {
		String userName = "error cannot get user name";
		
		try {
			userName = System.getProperty("user.name");
		} catch (final Exception e) {
			e.printStackTrace();
		}
		
		String pcName = "error cannot get pc name";
		
		try {
			pcName = InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			e.printStackTrace();
		}
		
		pcName = pcName + "#" + userName;
		
		return pcName;
	}
	
	public Integer getInteger(final String text) {
		try {
			return Integer.valueOf(text);
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public BigDecimal getBigDecimal(final String text) {
		try {
			return new BigDecimal(text);
		} catch (final Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public Boolean getBoolean(final Object object, final String key) {
		
		Boolean booleanValue = null;
		
		try {
			booleanValue = Boolean.valueOf((String) object);
		} catch (final Exception e) {
			Glog.prnte("LoadSett2: getBoolean, problem setting: " + key.toString() + "=" + object.toString() + e.getMessage());
		}
		
		return booleanValue;
	}
	
	public String getString(final Object object, final String key) {
		
		String string = null;
		
		try {
			string = String.valueOf(object);
		} catch (final Exception e) {
			Glog.prnte("LoadSett2: getString, problem setting: " + key.toString() + "=" + object.toString() + " e=" + e.getMessage());
		}
		
		return string;
	}
	
	public Integer getInteger(final Object object, final String key) {
		
		Integer intNumber = null;
		
		try {
			intNumber = Integer.valueOf((String) object);
		} catch (final Exception e) {
			Glog.prnte("LoadSett2: getInteger, problem setting: " + key.toString() + "=" + object.toString() + " e=" + e.getMessage());
		}
		
		return intNumber;
	}
	
	public Float getFloat(final Object object, final String key) {
		
		Float floatNumber = null;
		
		try {
			floatNumber = Float.valueOf((String) object);
			
		} catch (final Exception e) {
			Glog.prnte("LoadSett2: getFloat, problem setting: " + key.toString() + "=" + object.toString() + " e=" + e.getMessage());
		}
		
		return floatNumber;
	}
	
	public void runSystemCommand(final String command) {
		
		try {
			Runtime.getRuntime().exec(command);
//			BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//			String s = "";
//			// reading output stream of the command
//			while ((s = inputStream.readLine()) != null) {
//				prnt("[ping]" + s);
//			}
			
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	public Window getSelectedWindow(final Window[] windows) {
		
		Window result = null;
		for (int i = 0; i < windows.length; i++) {
			final Window window = windows[i];
			if (window.isActive()) {
				result = window;
			} else {
				final Window[] ownedWindows = window.getOwnedWindows();
				if (ownedWindows != null) {
					result = getSelectedWindow(ownedWindows);
				}
			}
		}
		return result;
	}
	
	public double roundDouble(final double value, final int places) {
		if (places < 0) {
			throw new IllegalArgumentException();
		}
		
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	public String gRound(final Double notRoundedNbr, final Integer digits) {
		if (notRoundedNbr != null) {
			final String roundedNbr = new BigDecimal(notRoundedNbr).setScale(digits, RoundingMode.HALF_UP).toString();
			return roundedNbr;
		} else {
			return null;
		}
	}
	
	public String gRound(final Float notRoundedNbr, final Integer digits) {
		if (notRoundedNbr != null) {
			final String roundedNbr = new BigDecimal(notRoundedNbr).setScale(digits, RoundingMode.HALF_UP).toString();
			return roundedNbr;
		} else {
			return null;
		}
	}
	
	public String gRound(final BigDecimal notRoundedNbr, final Integer digits) {
		if (notRoundedNbr != null) {
			final String roundedNbr = notRoundedNbr.setScale(digits, RoundingMode.HALF_UP).toString();
			return roundedNbr;
		} else {
			return null;
		}
	}
	
	public Thread getThreadByName(final String threadName) {
		Thread __tmp = null;
		
		final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		final Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
		
		for (int i = 0; i < threadArray.length; i++) {
			if (threadArray[i].getName().equals(threadName)) {
				__tmp = threadArray[i];
			}
		}
		
		return __tmp;
	}
	
	private static final int	POW10[]	= { 1, 10, 100, 1000, 10000, 100000, 1000000 };
	
	public String g2Round(double val, final int precision) {
		final StringBuilder sb = new StringBuilder();
		if (val < 0) {
			sb.append('-');
			val = -val;
		}
		final int exp = POW10[precision];
		final long lval = (long) (val * exp + 0.5);
		sb.append(lval / exp).append('.');
		final long fval = lval % exp;
		for (int p = precision - 1; p > 0 && fval < POW10[p]; p--) {
			sb.append('0');
		}
		sb.append(fval);
		return sb.toString();
	}
	
	public String decodeFast(final String fieldName, final String text) {
		int index1 = 0, index2, index3;
		String res1, res2, res3;
		//Glog.prnt("decodeFast. fieldName=" + fieldName + ", text=" + text);
//		int count = ;
//		int count2 = ;
//		if ((text.length() - text.replace("=", "").length()) != (text.length() - text.replace(";", "").length())) {
//			System.out.println("error. ; != =");
//			return "[error]";
//		}
		//System.out.println("count=" + count + " count2=" + count2);
		index1 = text.indexOf(fieldName);
		//Glog.prnt("decodeFast. = index1=" + index1);
		if (index1 != -1) {
			res1 = text.substring(index1);
			index2 = res1.indexOf(";");
			res2 = res1.substring(0, index2);
			index3 = res2.indexOf("=");
			res3 = res2.substring(index3 + 1);
			//Glog.prnt("decodeFast. return=" + res3);
			return res3;
		} else {
			Glog.prnte("index1=" + index1 + ", fieldName=" + fieldName + " not found in text=" + text);
			return "[error]";
		}
		
//		System.out.println("res1= " + res1);
//		System.out.println("res2= " + res2);
//		System.out.println("res3= " + res3);
		
//		String res2 = text.substring(text.indexOf(fieldName)).substring(0, text.indexOf(";"));
//		int index3 = res2.indexOf("=");
		
//		return res2.substring(index3 + 1);
		
	}
	
	public long getUniq() {
		//Random random = new Random();
		//int randomNumber = random.nextInt(9000 - 1000) + 1000;
		//return System.currentTimeMillis();
		
		return Long.valueOf(((long) ((Math.random() * (900000 - 100000)) + 100000)) + System.currentTimeMillis());
	}
}
