package com.eproseed.tool;

import java.io.File;

public class Main {

	public static void main(String[] args) {
		try {
			XMLExtractor extractor = new XMLExtractor(new File(args[0]));
			extractor.extractDocumentTypes();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
