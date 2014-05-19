package mt4;

import helper.pack.Gh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GfileReader {
	
//	public static void main(String[] args) {
//		//"C://Users//kepe//AppData//Roaming//MetaQuotes//Terminal//299541A8EEF200DE23B8B72470C7D8FA//MQL4//Files//pipe.txt";
//		//"C://Users//tiod//AppData//Roaming//MetaQuotes//Terminal//CCD68BFB06049A8615C607C3F6AD69B7//MQL4//Files//pipe.txt";
//		FileReader nm = new FileReader();
//		String location = "C://Users//tiod//AppData//Roaming//MetaQuotes//Terminal//EB3C3B239AFB8B62B7EC3451D269EB1E";
//		String precise = "//MQL4//Files//poga.txt";
//		nm.StartClient(location + precise, 25, (byte) 1);
//	}
	
	private ReaderListener	readerListener;
	
	public void setReadrListener(ReaderListener readerListener) {
		this.readerListener = readerListener;
	}
	
	public void StartClient(String fileName, int refreshRate, byte listenerId, boolean sendOnChange) {
		Gh.runPrintLogToConsole();
		
		String prev_line = "aaa";
		RandomAccessFile pipe = null;
		while (true) {
			
			File myTestFile = new File(fileName);
			boolean fileRead = myTestFile.canRead();
			boolean fileExists = myTestFile.exists();
			boolean fileIs = myTestFile.isFile();
			
			if (fileRead && fileExists && fileIs) {
				
				pipe = null;
				
				try {
					pipe = new RandomAccessFile(fileName, "r");
				} catch (FileNotFoundException e) {
					Gh.prnte("manual checks error 1");
					e.printStackTrace();
				} catch (SecurityException e) {
					Gh.prnte("manual checks error 2");
					e.printStackTrace();
				} catch (Exception e) {
					Gh.prnte("manual checks error 3");
					e.printStackTrace();
				}
				
				if (pipe != null) {
					String line = null;
					try {
						while (null != (line = pipe.readLine()) && pipe != null) {
							
							if (!prev_line.equals(line) || !sendOnChange) {
								//Gh.prnt("readed line=" + line);
								prev_line = line;
								
								if (listenerId == 1) {
									readerListener.incomingFileMsg1(prev_line);
								} else if (listenerId == 2) {
									readerListener.incomingFileMsg2(prev_line);
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						pipe.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					try {
						Thread.sleep(refreshRate);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else {
				Gh.prnte("fileRead=" + fileRead + " fileExists=" + fileExists + " fileIs=" + fileIs);
			}
		}
		
	}
}