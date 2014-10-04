package helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringDecoders {

	/*	public static void main(String[] args) {

			StringDecoders dec = new StringDecoders();

			String ip = dec.getSrvIp("127.0.0.1:4432");
			System.out.println("decoded ip = " + ip);

			Integer port = dec.getSrvPort("127.0.0.1:4432");
			System.out.println("decoded port = " + port);

		}*/

	public String getSrvIp(String ipAndPort) {
		Pattern p = Pattern.compile("^\\s*(.*?):(\\d+)\\s*$");
		Matcher m = p.matcher(ipAndPort);
		if (m.matches()) {
			return m.group(1); // host
		} else {
			return "";
		}
	}

	public Integer getSrvPort(String ipAndPort) {
		Pattern p = Pattern.compile("^\\s*(.*?):(\\d+)\\s*$");
		Matcher m = p.matcher(ipAndPort);
		if (m.matches()) {
			return Integer.parseInt(m.group(2));
		} else {
			return 0;
		}
	}
}
