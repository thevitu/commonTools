package com.eproseed.tool;

import java.io.File;

public class Util {

	public static void echo(String value) {
		System.out.println(value);
	}
	
	public static void listDirFiles(File dir) {
		echo("Files:");
		try {
			listDirFiles(dir, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void listDirFiles(File dir, int lvl) throws Exception {
		StringBuilder tab = new StringBuilder();
		for (int x = 0; x < lvl; x++) {
			tab.append("\t");
		}
		for (File file : dir.listFiles()) {
			StringBuilder sb = new StringBuilder(tab);
			if (file.isFile()) {
				sb.append(file.getName());
			} else if (file.isDirectory()) {
				sb.append("[").append(file.getName()).append("] ");
			}
			echo(sb.toString());
			if (file.isDirectory()) {
				listDirFiles(file, lvl + 1);
			}
		}
	}
	
}
