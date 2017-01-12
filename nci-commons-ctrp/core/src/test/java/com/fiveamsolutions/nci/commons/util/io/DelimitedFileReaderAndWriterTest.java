package com.fiveamsolutions.nci.commons.util.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class DelimitedFileReaderAndWriterTest {
    
	private static final String LS = System.getProperty("line.separator");
    private static final String TEST_INPUT_FILE_NAME = "test.in.txt";
    private static final String TEST_OUTPUT_FILE_NAME = "test.out.txt";
    private static final List<String> EXPECTED_FIRST_LINE = getFirstLine();
    private static final List<String> EXPECTED_SECOND_LINE = getSecondLine();
	private static final String TAB_DELIMITED_FILE_CONTENTS = "this\tis\ta\ttest"
			+ LS + "foobar" + LS;
	private static final String COMMA_DELIMITED_FILE_CONTENTS = "this,is,a,test"
			+ LS + "foobar" + LS;

    @Test
    public void testTabDelimitedReadingAndWriting() throws Exception {
        File inputFile = new File(TEST_INPUT_FILE_NAME);
        FileUtils.deleteQuietly(inputFile);
        writeToFile(TAB_DELIMITED_FILE_CONTENTS, inputFile);

        File outputFile = new File(TEST_OUTPUT_FILE_NAME);
        FileUtils.deleteQuietly(outputFile);
        
        DelimitedFileReader  tabDelimitedReader = null;
        DelimitedWriter tabDelimitedWriter = null;
        try {
            // test reader
            Injector injector = Guice.createInjector(new DelimitedFilesModule());
            DelimitedFileReaderFactory readerFactory = injector.getInstance(DelimitedFileReaderFactory.class);
            tabDelimitedReader = readerFactory.createTabDelimitedFileReader(inputFile);
            
            DelimitedFileWriterFactory writerFactory = injector.getInstance(DelimitedFileWriterFactory.class);
            tabDelimitedWriter = writerFactory.createTabDelimitedWriter(outputFile);
            
            assertTrue(tabDelimitedReader.hasNextLine());
            assertEquals(0, tabDelimitedReader.getCurrentLineNumber());
            List<String> peekLine = tabDelimitedReader.peek();
            testListEquivilency(EXPECTED_FIRST_LINE, peekLine);
            assertEquals(0, tabDelimitedReader.getCurrentLineNumber());
            List<String> firstLine = tabDelimitedReader.nextLine();
            assertEquals(1, tabDelimitedReader.getCurrentLineNumber());
            testListEquivilency(EXPECTED_FIRST_LINE, firstLine);
            List<String> secondLine = tabDelimitedReader.nextLine();
            testListEquivilency(EXPECTED_SECOND_LINE, secondLine);
            assertFalse(tabDelimitedReader.hasNextLine());
            tabDelimitedReader.reset();
            assertTrue(tabDelimitedReader.hasNextLine());
            assertEquals(0, tabDelimitedReader.getCurrentLineNumber());
            List<String> resetLine = tabDelimitedReader.nextLine();
            testListEquivilency(EXPECTED_FIRST_LINE, resetLine);
            
            //test writer
            tabDelimitedWriter.writeLine(firstLine);
            tabDelimitedWriter.writeLine(secondLine);
            tabDelimitedWriter.close();
            String actualOutput = getFileContents(outputFile);
            assertEquals(TAB_DELIMITED_FILE_CONTENTS, actualOutput);
        } finally {
            if (null != tabDelimitedReader) {
                tabDelimitedReader.close();
            }
            FileUtils.deleteQuietly(inputFile);
            FileUtils.deleteQuietly(outputFile);
        }
    }

    @Test
    public void testCommaDelimitedReadingAndWriting() throws Exception {
        File inputFile = new File(TEST_INPUT_FILE_NAME);
        FileUtils.deleteQuietly(inputFile);
        writeToFile(COMMA_DELIMITED_FILE_CONTENTS, inputFile);

        File outputFile = new File(TEST_OUTPUT_FILE_NAME);
        FileUtils.deleteQuietly(outputFile);
        
        DelimitedFileReader  commaDelimitedReader = null;
        DelimitedWriter commaDelimitedWriter = null;
        try {
            // test reader
            Injector injector = Guice.createInjector(new DelimitedFilesModule());
            DelimitedFileReaderFactory readerFactory = injector.getInstance(DelimitedFileReaderFactory.class);
            commaDelimitedReader = readerFactory.createCommaDelimitedFileReader(inputFile);
            
            DelimitedFileWriterFactory writerFactory = injector.getInstance(DelimitedFileWriterFactory.class);
            commaDelimitedWriter = writerFactory.createTabDelimitedWriter(outputFile);
            
            assertTrue(commaDelimitedReader.hasNextLine());
            assertEquals(0, commaDelimitedReader.getCurrentLineNumber());
            List<String> peekLine = commaDelimitedReader.peek();
            testListEquivilency(EXPECTED_FIRST_LINE, peekLine);
            assertEquals(0, commaDelimitedReader.getCurrentLineNumber());
            List<String> firstLine = commaDelimitedReader.nextLine();
            assertEquals(1, commaDelimitedReader.getCurrentLineNumber());
            testListEquivilency(EXPECTED_FIRST_LINE, firstLine);
            List<String> secondLine = commaDelimitedReader.nextLine();
            testListEquivilency(EXPECTED_SECOND_LINE, secondLine);
            assertFalse(commaDelimitedReader.hasNextLine());
            commaDelimitedReader.reset();
            assertTrue(commaDelimitedReader.hasNextLine());
            assertEquals(0, commaDelimitedReader.getCurrentLineNumber());
            List<String> resetLine = commaDelimitedReader.nextLine();
            testListEquivilency(EXPECTED_FIRST_LINE, resetLine);
            
            //test writer
            commaDelimitedWriter.writeLine(firstLine);
            commaDelimitedWriter.writeLine(secondLine);
            commaDelimitedWriter.close();
            String actualOutput = getFileContents(outputFile);
            assertEquals(TAB_DELIMITED_FILE_CONTENTS, actualOutput);
        } finally {
            if (null != commaDelimitedReader) {
                commaDelimitedReader.close();
            }
            FileUtils.deleteQuietly(inputFile);
            FileUtils.deleteQuietly(outputFile);
        }
    }
    
    private void writeToFile(final String contentsOfFile, final File file) throws IOException {
        FileUtils.writeStringToFile(file, contentsOfFile);
    }
    
    private String getFileContents(final File file) throws IOException {
        return FileUtils.readFileToString(file);
    }
    
    private void testListEquivilency(final List<String> firstList, final List<String> secondList) {
        assertEquals(firstList.size(), secondList.size());
        for(int i = 0; i < firstList.size(); i++) {
            assertEquals(firstList.get(i), secondList.get(i));
        }
    }
    
    private static List<String> getFirstLine() {
        List<String> line = new ArrayList<String>();
        line.add("this");
        line.add("is");
        line.add("a");
        line.add("test");
        return line;
    }
    
    private static List<String> getSecondLine() {
        List<String> line = new ArrayList<String>();
        line.add("foobar");
        return line;
    }
    
}