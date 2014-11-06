package mt4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GfileReader {
	
//	public static void main(String[] args) {
//		//"C://Users//kepe//AppData//Roaming//MetaQuotes//Terminal//299541A8EEF200DE23B8B72470C7D8FA//MQL4//Files//pipe.txt";
//		//"C://Users//tiod//AppData//Roaming//MetaQuotes//Terminal//CCD68BFB06049A8615C607C3F6AD69B7//MQL4//Files//pipe.txt";
//		FileReader nm = new FileReader();
//		String location = "C://Users//tiod//AppData//Roaming//MetaQuotes//Terminal//EB3C3B239AFB8B62B7EC3451D269EB1E";
//		String precise = "//MQL4//Files//poga.txt";
//		nm.StartClient(location + precise, 25, (byte) 1);
//	}
	
	private ReaderListener		readerListener;
	private static final Logger	LOG	= LogManager.getLogger(GfileReader.class.getName());
	
	public void setReadrListener(final ReaderListener readerListener) {
		this.readerListener = readerListener;
	}
	
	public void StartClient(final String fileName, final int refreshRate, final byte listenerId, final boolean sendOnChange, final byte lineNumb) {
		//Glog.runPrintLogToConsole();
//		log.info("started file reader. fileName=" + fileName + ", refreshRate=" + refreshRate + ", listenerId=" + listenerId + ", sendOnChange="
//				+ sendOnChange + ", lineNumb=" + lineNumb);
		String prev_line = "aaa";
		RandomAccessFile pipe = null;
		while (true) {
			
			final File myTestFile = new File(fileName);
			final boolean fileRead = myTestFile.canRead();
			final boolean fileExists = myTestFile.exists();
			final boolean fileIs = myTestFile.isFile();
			
			if (fileRead && fileExists && fileIs) {
				
				pipe = null;
				
				try {
					pipe = new RandomAccessFile(fileName, "r");
				} catch (final FileNotFoundException e) {
					LOG.error("manual checks error 1");
					e.printStackTrace();
				} catch (final SecurityException e) {
					LOG.error("manual checks error 2");
					e.printStackTrace();
				} catch (final Exception e) {
					LOG.error("manual checks error 3");
					e.printStackTrace();
				}
				
				if (pipe != null) {
					String line = null;
					byte lineNumbCnt = 0;
					try {
						while (null != (line = pipe.readLine()) && pipe != null) {
							if ((!prev_line.equals(line) || !sendOnChange) && lineNumbCnt == lineNumb) {
								//log.info("readed line=" + line);
								prev_line = line;
								
								if (listenerId == 1) {
									readerListener.incomingFileMsg1(prev_line);
								} else if (listenerId == 2) {
									readerListener.incomingFileMsg2(prev_line);
								} else if (listenerId == 3) {
									readerListener.incomingFileMsg3(prev_line);
								} else if (listenerId == 4) {
									readerListener.incomingFileMsg4(prev_line);
								} else if (listenerId == 5) {
									readerListener.incomingFileMsg5(prev_line);
								}
							}
							lineNumbCnt++;
						}
					} catch (final IOException e) {
						e.printStackTrace();
					}
					try {
						pipe.close();
					} catch (final IOException e1) {
						e1.printStackTrace();
					}
					try {
						Thread.sleep(refreshRate);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else {
				LOG.error("fileRead=" + fileRead + " fileExists=" + fileExists + " fileIs=" + fileIs);
				LOG.error("fileName=" + fileName + " listenerId=" + listenerId);
			}
		}
		
	}
}