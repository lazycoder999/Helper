package tray;

import helper.Gh;
import helper.Glog;

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

public class GtrayIcon {

	public static void main(String[] args) {
		GtrayIcon gtray = new GtrayIcon();
		gtray.createTry("1.jpg");
	}

	public URL getImageUrl(String trayIconName) {
		return getClass().getResource("resources/" + trayIconName);
	}

	public void createTry(String trayIconName) {
		Glog.prnt("filename=" + Gh.getRunningFileName());
		TrayIcon trayIcon = null;
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			Image image = null;

			Glog.prnt("getClass().getResource=" + getClass().getResource("resources/" + trayIconName));
			image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("resources/" + trayIconName));

			PopupMenu popMenu = new PopupMenu();
			MenuItem item1 = new MenuItem("Exit");
			popMenu.add(item1);

			item1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});

			int trayIconWidth = new TrayIcon(image).getSize().width;
			image = image.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH);
			trayIcon = new TrayIcon(image, Gh.getRunningFileName(), popMenu);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("error when setting tay icon, e=" + e);
			}

		} else {
			Glog.prnt("tray icon not supoorted");
		}
	}

}
