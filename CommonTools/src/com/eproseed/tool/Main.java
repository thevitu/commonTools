package com.eproseed.tool;

public class Main {

	public static void main(String[] args) {
		try {
			ExtractorWindow window = new ExtractorWindow();
			if (args != null && args.length >= 2) {
				window.setEnvVar(args[0])
						.setDefaultDir(args[1]);
			}
			window.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
