package edu.unibe.kravchenko.extractor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Iterate over the classes and print their Javadoc.
 */
public class ClassesJavadocExtractor {
    public static void main(String[] args) {

        // todo cleanup all created directories

        ClassesJavadocExtractor extractor = new ClassesJavadocExtractor();

        Map<String, String> scanPaths = new HashMap<>();
        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\ClassCodeComments\\src\\main\\resources\\test", "test");
//        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\swagger-java-spring-example\\class-comment-extractor\\src\\main\\resources\\test", "spark");
//        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\org.eclipse.cdt", "eclipseCdt");
//        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\guava", "guava");
//        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\hadoop", "hadoop");
//        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\guice", "guice");
//        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\framework", "vaadin");

        List<CSVRecord> readCommentRecords;
        for (Map.Entry<String, String> e : scanPaths.entrySet()) {
            StringWriter stringBuffer = extractor.readComment(e.getKey(), e.getValue());

            try {
                StringReader reader = new StringReader(stringBuffer.toString());
                CSVParser extractorParser = new CSVParser(reader, CSVFormat.DEFAULT);
                readCommentRecords = extractorParser.getRecords();
                List<CSVRecord> recordListWithComments = readCommentRecords.subList(1, readCommentRecords.size());

                List<CSVRecord> csvRecordsRaw = extractor.countCommentsForEachFile("C:\\Users\\vaano\\IdeaProjects\\ClassCodeComments\\src\\main\\resources\\test");
                List<CSVRecord> csvRecordsProcessed = csvRecordsRaw.subList(6, csvRecordsRaw.size() - 1);
                csvRecordsProcessed.removeIf(a -> a.get(1).contains("package-info.java") || a.get(1).contains("package.scala"));


//                recordList.forEach(a -> a.get(1)replace());

                Pattern pattern = Pattern.compile("(\\w|[-.])+$");

                File csvProjectStatistics = new File("C:\\Users\\vaano\\IdeaProjects\\ClassCodeComments\\src\\main\\resources" + "test.csv");
                BufferedWriter writer = Files.newWriter(csvProjectStatistics, Charset.forName("UTF-8"));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("filename", "comment_amount", "code_amount", "ratio", "classComment", "methodComment", "methodBody", "constructorComment", "constructorBody", "fieldComment", "fieldBody"));

                for (CSVRecord r: recordListWithComments) {
                    Matcher m = pattern.matcher(r.get(0));
                    if (m.find()) {
                        String classFileName = m.group(0);
                        for (CSVRecord processed: csvRecordsProcessed) {
                            if (processed.get(1).contains(classFileName)) {

                                String commentAmount = processed.get(3);
                                String codeAmount = processed.get(4);
                                csvPrinter.printRecord(classFileName, commentAmount, codeAmount, Double.valueOf(commentAmount)/Double.valueOf(codeAmount),
                                    r.get(1), r.get(2), r.get(3), r.get(4), r.get(5), r.get(6), r.get(7)
                                        );
                                break;
                            }
                        }
                    }
                }

                csvPrinter.flush();
                csvPrinter.close();

            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }


//        File csvProjectStatistics = new File("C:\\Users\\vaano\\IdeaProjects\\swagger-java-spring-example\\class-comment-extractor\\src\\main\\resources" + "spark.csv");
//        BufferedWriter writer = Files.newWriter(csvProjectStatistics, Charset.forName("UTF-8"));
//
//        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
//                .withHeader("filename", "comment", "code", "ratio", "classComment"));
//
//
//        for (CSVRecord record: recordList) {
//            csvPrinter.printRecord();
//        }
//
//
        System.out.println("123");
    }

    public StringWriter readComment(String scanDir, String folder) {
        try {

            File projectDir = new File(scanDir);
            Path directory;

            StringWriter out = new StringWriter();
            final CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.DEFAULT
                    .withHeader("filename", "classComment", "methodComment", "methodBody", "constructorComment", "constructorBody", "fieldComment", "fieldBody"));

            File commentFile = new File("C:\\Users\\vaano\\IdeaProjects\\ClassCodeComments\\" + folder + ".txt");
            if (commentFile.exists()) {
                System.out.println("deleting file with name " + commentFile.toString());
                System.out.println("File is deleted : " + commentFile.delete());
            }

            final boolean[] metNewClass = {false};
            final boolean[] firstEntry = {false};

            new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
                try {
                    final StringBuilder methodBodyBuilder = new StringBuilder();
                    final StringBuilder methodCommentBuilder = new StringBuilder();
                    final StringBuilder constructorBodyBuilder = new StringBuilder();
                    final StringBuilder constructorCommentBuilder = new StringBuilder();
                    final StringBuilder classCommentBuilder = new StringBuilder();
                    final StringBuilder fieldCommentBuilder = new StringBuilder();
                    final StringBuilder fieldBodyBuilder = new StringBuilder();


                    new VoidVisitorAdapter<Object>() {
                        @Override
                        public void visit(JavadocComment comment, Object arg) {

                            super.visit(comment, arg);
                            String title = null;
                            if (comment.getCommentedNode().isPresent()) {
                                NodeContent describe = describe(comment.getCommentedNode().get(), path, folder);

                                switch (describe.getType()) {
                                    case "method":
                                        methodBodyBuilder.append(describe.getBody()).append(" \n");
                                        methodCommentBuilder.append(describe.getComment()).append(" \n");
                                        break;
                                    case "constructor":
                                        constructorBodyBuilder.append(describe.getBody()).append(" \n");
                                        constructorCommentBuilder.append(describe.getComment()).append(" \n");
                                        break;
                                    case "field":
                                        fieldBodyBuilder.append(describe.getBody()).append( "\n");
                                        fieldCommentBuilder.append(describe.getComment()).append(" \n");
                                        break;
                                    case "class":
                                        metNewClass[0] = true;
                                        firstEntry[0] = true;
                                        classCommentBuilder.append(describe.getComment()).append("\n");
                                        if (metNewClass[0] && firstEntry[0]) {
                                            try {
                                                csvPrinter.printRecord(path, classCommentBuilder.toString(), methodCommentBuilder.toString(), methodBodyBuilder.toString(),
                                                        constructorCommentBuilder.toString(), constructorBodyBuilder.toString(),
                                                        fieldCommentBuilder.toString(), fieldBodyBuilder.toString());
                                                methodBodyBuilder.setLength(0);
                                                methodCommentBuilder.setLength(0);
                                                constructorBodyBuilder.setLength(0);
                                                constructorCommentBuilder.setLength(0);
                                                classCommentBuilder.setLength(0);
                                                fieldCommentBuilder.setLength(0);
                                                fieldBodyBuilder.setLength(0);
                                                metNewClass[0] = false;
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        firstEntry[0] = true;
                                        break;
                                }

                                title = String.format("%s (%s)", describe, path, file);
                            } else {
                                title = String.format("No element associated (%s)", path);
                            }
//                            System.out.println(title);
//                            System.out.println(Strings.repeat("=", title.length()));
//                            System.out.println(comment);
                        }
                    }.visit(JavaParser.parse(file), null);
                } catch (IOException e) {
                    new RuntimeException(e);
                }
            }).explore(projectDir);
            return out;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static NodeContent describe(Node node, String path, String folder) {

        NodeContent empty = new NodeContent();
        empty.setType("");
        empty.setComment("");
        empty.setBody("");

        if (node instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) node;
            NodeContent nodeContent = new NodeContent();
            nodeContent.setBody(methodDeclaration.getBody().map(Node::toString).orElse(methodDeclaration.getName().asString()));
            nodeContent.setComment(methodDeclaration.getComment().map(Comment::toString).orElse(""));
            nodeContent.setType("method");
            return nodeContent;
        }


        if (node instanceof ConstructorDeclaration) {
            ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) node;
            NodeContent nodeContent = new NodeContent();
            nodeContent.setBody(constructorDeclaration.getBody().toString());
            nodeContent.setComment(constructorDeclaration.getComment().map(Comment::toString).orElse(""));
            nodeContent.setType("constructor");
            return nodeContent;
        }

        if (node instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration) node;
            NodeContent nodeContent = new NodeContent();
//            nodeContent.setBody(declaration.getBody().toString());
            nodeContent.setComment(declaration.getComment().map(Comment::toString).orElse(""));
            nodeContent.setType("class");
            return nodeContent;
        }


//        if (node instanceof ClassOrInterfaceDeclaration) {
//            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration)node;
//            try {
//                File commentFile = new File("C:\\Users\\vaano\\IdeaProjects\\swagger-java-spring-example\\class-comment-extractor\\" + folder + ".txt");
//                if (!commentFile.exists()) {
//                    System.out.println("creating file with name " + commentFile.toString());
//                    Files.touch(commentFile);
//                }
//                String title = String.format("%s (%s)", ((ClassOrInterfaceDeclaration) node).getName(), path);
//                Files.append(title, commentFile, Charset.forName("utf-8"));
//                Files.append(node.getComment().get().toString(), commentFile, Charset.forName("utf-8"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (classOrInterfaceDeclaration.isInterface()) {
//                String a = "Interface " + classOrInterfaceDeclaration.getName();
//            } else {
//                String a = return "Class " + classOrInterfaceDeclaration.getName();
//            }
//        }
//
//
        if (node instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
            List<String> varNames = fieldDeclaration.getVariables().stream().map(v -> v.getName().getId()).collect(Collectors.toList());
            String a = "Field " + String.join(", ", varNames);

            NodeContent nodeContent = new NodeContent();
//            nodeContent.setBody(fieldDeclaration.gegetBody().toString());
            nodeContent.setBody(a);
            nodeContent.setComment(fieldDeclaration.getComment().map(Comment::toString).orElse(""));
            nodeContent.setType("field");
            return nodeContent;
        }

        else {
            System.out.println("This is another declaration" + node);
        }


        return empty;
    }


    public List<CSVRecord> countCommentsForEachFile(String directory) {
        try {
            Process cloc = new ProcessBuilder("C:\\Users\\vaano\\IdeaProjects\\ClassCodeComments\\src\\main\\c\\cloc\\cloc-1.84.exe",
                    directory, "--by-file", "-csv").start();
            String s = IOUtils.toString(cloc.getInputStream());
            Reader in = new StringReader(s);
            CSVParser parser = new CSVParser(in, CSVFormat.EXCEL);
            List<CSVRecord> list = parser.getRecords();
            System.out.println(s);
            return list;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}