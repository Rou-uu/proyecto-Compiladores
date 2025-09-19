package com.compiler.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Utility class for reading files.
 * Provides convenient methods for reading text files line by line.
 *
 * @author Rubén Alfaro
 * @version 1.0
 */
public class FileReader {

    /**
     * Reads all lines from a text file and returns them as a list of strings.
     * Each line in the file becomes a separate string in the returned list.
     * The file is automatically closed after reading, even if an exception occurs.
     *
     * @param filePath the path to the file to read
     * @return an ArrayList containing all lines from the file
     * @throws RuntimeException if an I/O error occurs while reading the file
     */
    public static ArrayList<String> readLines(String filePath) {
        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }

        return lines;
    }
}