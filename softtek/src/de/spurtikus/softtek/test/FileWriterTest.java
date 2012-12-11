package de.spurtikus.softtek.test;

import org.junit.Test;

import softtek.FileWriter;

public class FileWriterTest {

	String basePath = "/tmp";
	String fileName = "test.tek";

	@Test
	public void testFileCreation() {
		FileWriter writer = new FileWriter(basePath+"/"+fileName);
		
		byte[] buffer = new byte[100];
		
		for (int i=0;i<100;i++) {
			buffer[i] = '.';
			if ((i%10)==0)
				buffer[i]='*';
		}
		writer.open();
		writer.write(buffer);
		writer.close();
	}
}
