package com.p3.archon.xmlsipautomater.utilities;

/*
 * $$HeadURL$$
 * $$Id$$
 *
 * CCopyright (c) 2015, P3Solutions . All Rights Reserved.
 * This code may not be used without the express written permission
 * of the copyright holder, P3Solutions.
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class: DirectoryWalker
 *
 *
 * This class is used to Walk a Directory and Return a List of all Files in a
 * Directory.
 *
 * @author Malik
 * @version 1.0
 *
 */
public class DirectoryWalker {

	private List<String> m_fileList;

	/**
	 * walk -Walk a Directory and Return a List of all Files in a Directory.
	 *
	 * @param source
	 *            - The String version of the Source File directory to walk
	 *
	 * @return List of all Files in a Directory.
	 */
	public synchronized List<String> walk(String source) throws IOException {
		System.out.println("Walking through and getting Import files for " + source);
		m_fileList = new ArrayList<String>();
		walk(new File(source));
		return m_fileList;
	}

	/**
	 * walk - private method to Walk a Directory and Return a List of all Files in a
	 * Directory.
	 *
	 * @param source
	 *            - The File version of the Source File directory to walk
	 *
	 */
	private void walk(File source) throws IOException {
		if (source.isDirectory()) {
			File[] files = source.listFiles(), dirs = new File[files.length];
			int idirs = 0;
			for (File file : files) {
				if (file.isDirectory()) {
					dirs[idirs++] = file;
				} else if (file.isFile()) {
					System.out.println("file found  = " + file);
					m_fileList.add(file.getAbsolutePath());
				}
			}
		}
	}

	public synchronized List<String> walkDir(String source) throws IOException {
		System.out.println("Walking through and getting Import files for " + source);
		m_fileList = new ArrayList<String>();
		walkDir(new File(source));
		return m_fileList;
	}

	private void walkDir(File source) throws IOException {
		if (source.isDirectory()) {
			File[] files = source.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					m_fileList.add(file.getAbsolutePath());
				}
			}
		}
	}

}
