package tray;

import helper.Gh;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import org.apache.log4j.Logger;

public class GtrayIcon {
	
//	public static void main(String[] args) {
//		GtrayIcon gtray = new GtrayIcon();
//		gtray.createTry("1.jpg");
//	}
	
	private final Gh			gh	= new Gh();
	
	private static final Logger	LOG	= Logger.getLogger(GtrayIcon.class.getName());
	
	public URL getImageUrl(final String trayIconName) {
		return getClass().getResource("resources/" + trayIconName);
	}
	
	public void createTry(final String trayIconName) {
		LOG.info("filename=" + gh.getRunningFileName());
		TrayIcon trayIcon = null;
		if (SystemTray.isSupported()) {
			final SystemTray tray = SystemTray.getSystemTray();
			
			Image image = null;
			
			LOG.info("getClass().getResource=" + getClass().getResource("resources/" + trayIconName));
			image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("resources/" + trayIconName));
			
			final PopupMenu popMenu = new PopupMenu();
			final MenuItem item1 = new MenuItem("Exit");
			popMenu.add(item1);
			
			item1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					System.exit(0);
				}
			});
			
			final int trayIconWidth = new TrayIcon(image).getSize().width;
			image = image.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH);
			trayIcon = new TrayIcon(image, gh.getRunningFileName(), popMenu);
			
			try {
				tray.add(trayIcon);
			} catch (final AWTException e) {
				System.err.println("error when setting tay icon, e=" + e);
			}
			
		} else {
			LOG.info("tray icon not supoorted");
		}
	}
	
}
