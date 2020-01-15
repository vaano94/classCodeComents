package edu.unibe.kravchenko.extractor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CommentAmountChecker {

    public static void main(String[] args) throws IOException {
        CSVParser parser = new CSVParser(new FileReader("C:\\Users\\vaano\\IdeaProjects\\ClassCodeComments\\src\\main\\resources\\hadoop.csv"), CSVFormat.DEFAULT);


        List<CSVRecord> records = parser.getRecords();

        System.out.println("Size" + records.size());

        long recordCount = records.size();
        long emptyCount = 0;

        long commentLength = 0L;

        for (CSVRecord r: records) {
            if (r.get(4) == null || r.get(4).isEmpty()) {
                System.out.println("Class comment is empty");
                emptyCount ++;
                System.out.println(r.toString());
            }

            String replacedString = r.get(4).replaceAll("\\*|\\n", "");
            long recordLength = replacedString.length();

//            replacedString.split("\r\n|\r|\n").length;

            commentLength += recordLength;
        }

        System.out.println("Record count: " + recordCount);
        System.out.println("Empty count: " + emptyCount);
        System.out.println("Comment length: " + commentLength);

    }

}
