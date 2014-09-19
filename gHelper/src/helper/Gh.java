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
import java.util.Set;

public class Gh {
	
	public static SimpleDateFormat	ft				= new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat	ft1				= new SimpleDateFormat("H:mm:ss.SSS");
	public static SimpleDateFormat	ft2				= new SimpleDateFormat("yyyy-MM-dd H:mm:ss.SSS");
	public SimpleDateFormat			ft3				= new SimpleDateFormat("H:mm:ss");
	public SimpleDateFormat			ft4				= new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
	private static SimpleDateFormat	ft5				= new SimpleDateFormat("yyyy-MM");
	public DecimalFormat			df2				= new DecimalFormat("#.##");
	
	public boolean					shuttingDown	= false;
	
	public Gh() {
		
	}
	
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
			Glog.prnte("gDecoder, error, text=" + text + ", e=" + e.getMessage());
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
		Glog.prnt("fileDateModified: fullFileName=" + fullFileName);
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
			Glog.prnte("LoadSett2: getBoolean, problem setting: " + key.toString() + "=" + object.toString() + e.getMessage());
		}
		
		return booleanValue;
	}
	
	public String getString(Object object, String key) {
		
		String string = null;
		
		try {
			string = String.valueOf((String) object);
		} catch (Exception e) {
			Glog.prnte("LoadSett2: getString, problem setting: " + key.toString() + "=" + object.toString() + " e=" + e.getMessage());
		}
		
		return string;
	}
	
	public Integer getInteger(Object object, String key) {
		
		Integer intNumber = null;
		
		try {
			intNumber = Integer.valueOf((String) object);
		} catch (Exception e) {
			Glog.prnte("LoadSett2: getInteger, problem setting: " + key.toString() + "=" + object.toString() + " e=" + e.getMessage());
		}
		
		return intNumber;
	}
	
	public Float getFloat(Object object, String key) {
		
		Float floatNumber = null;
		
		try {
			floatNumber = Float.valueOf((String) object);
			
		} catch (Exception e) {
			Glog.prnte("LoadSett2: getFloat, problem setting: " + key.toString() + "=" + object.toString() + " e=" + e.getMessage());
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
	
	public String decodeFast(String fieldName, String text) {
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
