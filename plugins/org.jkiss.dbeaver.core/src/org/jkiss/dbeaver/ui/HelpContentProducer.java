package org.jkiss.dbeaver.ui;

import java.io.InputStream;
import java.util.Locale;

import org.eclipse.help.IHelpContentProducer;

public class HelpContentProducer implements IHelpContentProducer {

	public InputStream getInputStream(String pluginID, String href, Locale locale) {
        if (href.equals(IHelpContextIds.CTX_DRIVER_EDITOR)) {

        } else if (href.startsWith(IHelpContextIds.CTX_DRIVER_EDITOR)) {
            final String driverId = href.substring(IHelpContextIds.CTX_DRIVER_EDITOR.length() + 1);
            System.out.println(driverId);
        }
		return null;
	}

}
