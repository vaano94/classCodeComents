package edu.unibe.kravchenko.extractor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.io.Files;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Iterate over the classes and collect their Javadoc
 */
public class ClassesJavadocExtractor {

    public static final String OUTPUT_FOLDER = "C:\\Users\\vaano\\IdeaProjects\\ClassCodeComments\\src\\main\\resources\\";
    public static final String PROJECTS_STATISTICS_FOLDER = "C:\\Users\\vaano\\IdeaProjects\\ClassCodeComments\\";

    private static BiConsumer<CSVPrinter, Record> writeSingleRecord = (printer, r) -> {
        try {
            printer.printRecord(r.getClassFileName(), r.getCommentAmount(), r.getCodeAmount(), r.getRatio(),
                    r.getClassComment(), r.getMethodComment(), r.getMethodBody(), r.getConstructorComment(), r.getConstructorBody(),
                    r.getFieldComment(), r.getFieldBody(), r.getClassCommentLineLength(), r.getClassCommentSymbolLength());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    };
    private static BiConsumer<CSVPrinter, List<Record>> writeMultipleRecords = (printer, r) -> {
        try {
            printer.printRecord(r);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    };

    public static void main(String[] args) throws IOException {

        ClassesJavadocExtractor extractor = new ClassesJavadocExtractor();

        Map<String, String> scanPaths = new HashMap<>();

        // test directories/
        scanPaths.put(new ClassPathResource("test").getFile().getPath(), "test_new");
        scanPaths.put(new ClassPathResource("classAmountTest").getFile().getPath(), "classAmountTest");

        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\swagger-java-spring-example\\class-comment-extractor\\src\\main\\resources\\spark", "spark");
        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\org.eclipse.cdt", "eclipseCdt");
        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\guava", "guava");
        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\java_comment_projects\\hadoop", "hadoop");
        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\guice", "guice");
        scanPaths.put("C:\\Users\\vaano\\IdeaProjects\\framework", "vaadin");

        // project directories
        scanPaths.put(new ClassPathResource("spark").getFile().getPath(), "spark");
        scanPaths.put(new ClassPathResource("org.eclipse.cdt").getFile().getPath(), "eclipseCdt");
        scanPaths.put(new ClassPathResource("guava").getFile().getPath(), "guava");
        scanPaths.put(new ClassPathResource("hadoop").getFile().getPath(), "hadoop");
        scanPaths.put(new ClassPathResource("guice").getFile().getPath(), "guice");
        scanPaths.put(new ClassPathResource("framework").getFile().getPath(), "vaadin");

        List<CSVRecord> readCommentRecords;
        for (Map.Entry<String, String> e : scanPaths.entrySet()) {
            StringWriter stringBuffer = extractor.readComment(e.getKey(), e.getValue());

            try {
                StringReader reader = new StringReader(stringBuffer.toString());
                CSVParser extractorParser = new CSVParser(reader, CSVFormat.DEFAULT);
                readCommentRecords = extractorParser.getRecords();
                List<CSVRecord> recordListWithComments = readCommentRecords.subList(1, readCommentRecords.size());
//
                List<CSVRecord> csvRecordsRaw = extractor.countCommentsForEachFile(e.getKey());
                List<CSVRecord> csvRecordsProcessed = csvRecordsRaw.subList(6, csvRecordsRaw.size() - 1);
                csvRecordsProcessed.removeIf(a -> {
                    if (a != null && a.size() >= 2) {
                        return a.get(1) != null && a.get(1).contains("package-info.java") || a.get(1).contains("package.scala");
                    }
                    return false;
                });


                Pattern pattern = Pattern.compile("(\\w|[-.])+$");
                File csvProjectStatistics = new File(OUTPUT_FOLDER + e.getValue() + ".csv");
                OutputStreamWriter writer = Files.newWriterSupplier(csvProjectStatistics, Charset.forName("UTF-8")).getOutput();
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("filename", "comment_amount", "code_amount", "ratio", "classComment", "methodComment", "methodBody",
                                "constructorComment", "constructorBody", "fieldComment", "fieldBody", "classCommentLineLength", "classCommentSymbolLength"));

                List<Record> recordList = new ArrayList<>();
                for (CSVRecord r : recordListWithComments) {
                    Matcher m = pattern.matcher(r.get(0));
                    if (m.find()) {
                        String classFileName = m.group(0);
                        for (CSVRecord processed : csvRecordsProcessed) {
                            if (processed != null && processed.size() >= 2) {
                                if (processed.get(1).contains(classFileName)) {

                                    String commentAmount = processed.get(3);
                                    String codeAmount = processed.get(4);
                                    Record rec = Record.builder()
                                            .classFileName(classFileName)
                                            .commentAmount(commentAmount)
                                            .codeAmount(codeAmount)
                                            .ratio(Double.valueOf(commentAmount) / Double.valueOf(codeAmount))
                                            .classComment(r.get(1))
                                            .methodComment(r.get(2))
                                            .methodBody(r.get(3))
                                            .constructorComment(r.get(4))
                                            .constructorBody(r.get(5))
                                            .fieldComment(r.get(6))
                                            .fieldBody(r.get(7))
                                            .classCommentLineLength(r.get(8))
                                            .classCommentSymbolLength(r.get(9))
                                            .build();

                                    csvPrinter.printRecord(rec.getClassFileName(), rec.getCommentAmount(), rec.getCodeAmount(), rec.getRatio(),
                                            rec.getClassComment(), rec.getMethodComment(), rec.getMethodBody(), rec.getConstructorComment(), rec.getConstructorBody(),
                                            rec.getFieldComment(), rec.getFieldBody(), rec.getClassCommentLineLength(), rec.getClassCommentSymbolLength());
                                        processOutput(csvPrinter, rec, writeSingleRecord);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                csvPrinter.close(true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static <T> void processOutput(CSVPrinter csvPrinter, T object, BiConsumer<CSVPrinter, T> recordWriter) {
        recordWriter.accept(csvPrinter, object);
    }

    private StringWriter readComment(String scanDir, String resultingFileName) {
        try {

            AtomicReference<Long> authorCommentsInJavadoc = new AtomicReference<>(0L);
            AtomicReference<Long> authorCommentsDangling = new AtomicReference<>(0L);
            AtomicReference<Long> versionCommentsInJavadoc = new AtomicReference<>(0L);
            AtomicReference<Long> versionCommentsDangling = new AtomicReference<>(0L);
            AtomicReference<Long> amountOfClasses = new AtomicReference<>(0L);
            AtomicReference<Long> classesWithoutComments = new AtomicReference<>(0L);
            Long[] timesLongs = {0L};
            Long[] longs = {0L};
            Long[] lines = {0L};
            AtomicReferenceArray<Long> overallCommentLines = new AtomicReferenceArray<>(lines);
            AtomicReferenceArray<Long> overallCommentLength = new AtomicReferenceArray<Long>(longs);
            AtomicReferenceArray<Long> timesIncremented = new AtomicReferenceArray<Long>(timesLongs);

            JavaParser javaParser = new JavaParser();

            File projectDir = new File(scanDir);

            StringWriter out = new StringWriter();
            final CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.DEFAULT
                    .withHeader("filename", "classComment", "methodComment", "methodBody", "constructorComment", "constructorBody", "fieldComment", "fieldBody"));

            File commentFile = new File(PROJECTS_STATISTICS_FOLDER + resultingFileName + ".txt");
            if (commentFile.exists()) {
                System.out.println("deleting file with name " + commentFile.toString());
                System.out.println("File is deleted : " + commentFile.delete());
            }

            new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
                if (path.contains("package-info.java")) {
                    return;
                }
                amountOfClasses.updateAndGet(v -> v + 1);
                try {

                    CompilationUnit cu = StaticJavaParser.parse(file);

                    if (cu.getTypes().size() > 0) {

                        Optional<ClassOrInterfaceDeclaration> classByName = cu.getClassByName(cu.getType(0).getName().asString());
                        Optional<ClassOrInterfaceDeclaration> interfaceByName = cu.getInterfaceByName(cu.getType(0).getName().asString());
                        Optional<AnnotationDeclaration> annotationDeclarationByName = cu.getAnnotationDeclarationByName(cu.getType(0).getName().asString());
                        Optional<EnumDeclaration> enumDeclarationByName = cu.getEnumByName(cu.getType(0).getName().asString());
                        if (classByName.isPresent()) {

                            List<Comment> allContainedComments = classByName.get().getAllContainedComments();
                            Optional<Comment> comment = classByName.get().getComment();
                            incrementIfHasContendedCommentOrJustComment(classByName.get(), classesWithoutComments);
                        } else if (interfaceByName.isPresent()) {
                            incrementIfHasContendedCommentOrJustComment(interfaceByName.get(), classesWithoutComments);
                        } else if (annotationDeclarationByName.isPresent()) {
                            incrementIfHasContendedCommentOrJustComment(annotationDeclarationByName.get(), classesWithoutComments);
                        } else if (enumDeclarationByName.isPresent()) {
                            incrementIfHasContendedCommentOrJustComment(enumDeclarationByName.get(), classesWithoutComments);
                        } else {
                            System.out.println("Types empty");
                        }
                    }

                    final StringBuilder methodBodyBuilder = new StringBuilder();
                    final StringBuilder methodCommentBuilder = new StringBuilder();
                    final StringBuilder constructorBodyBuilder = new StringBuilder();
                    final StringBuilder constructorCommentBuilder = new StringBuilder();
                    final StringBuilder classCommentBuilder = new StringBuilder();
                    final StringBuilder fieldCommentBuilder = new StringBuilder();
                    final StringBuilder fieldBodyBuilder = new StringBuilder();

                    List<String> comments = cu.getAllContainedComments()
                            .stream()
                            .filter(e -> {
                                if (e.getCommentedNode().isPresent()) {
                                    return (e.getCommentedNode().get() instanceof TypeDeclaration);
                                } else {

                                    return false;
                                }
                            })
                            .map(e -> {
                                Node node = e.getCommentedNode().get();
                                if (node instanceof TypeDeclaration) {
                                    StringBuilder sb = new StringBuilder();
                                    // lookup upper comments to see if they're dangling
                                    e.getCommentedNode().ifPresent(ch -> ch.getParentNode().ifPresent(pn -> {
                                        List<Comment> orphanComments = pn.getOrphanComments();
                                        List<String> collectedOrphan = orphanComments.stream().map(cc -> cc.getContent()).collect(Collectors.toList());
                                        String joinedOrphan = String.join("|", collectedOrphan);
                                        sb.append(joinedOrphan);

                                        if (containsAuthorComment(joinedOrphan)) {
                                            authorCommentsDangling.updateAndGet(v -> v + 1);
                                        }
                                        if (containsVersionComment(joinedOrphan)) {
                                            versionCommentsDangling.updateAndGet(v -> v + 1);
                                        }

                                    }));
                                    if (sb.length() > 0) {
                                        sb.append("|");
                                    }
                                    sb.append(e.getContent());
                                    return sb.toString();
                                }
                                return "";
                            })
                            .collect(Collectors.toList());
                    classCommentBuilder.append(String.join("|", comments));


                    new VoidVisitorAdapter<Object>() {
                        @Override
                        public void visit(JavadocComment comment, Object arg) {

                            super.visit(comment, arg);
                            String title = null;
                            if (comment.getCommentedNode().isPresent()) {
                                NodeDescriber describer = new NodeDescriber();
                                NodeContent describe = describer.describe(comment.getCommentedNode().get(), path, resultingFileName);

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
                                        fieldBodyBuilder.append(describe.getBody()).append("\n");
                                        fieldCommentBuilder.append(describe.getComment()).append(" \n");
                                        break;
                                    case "class":
                                        String classComment = describe.getComment();
                                        if (containsVersionComment(classComment)) {
                                            versionCommentsInJavadoc.updateAndGet(v -> v + 1);
                                        }
                                        if (containsAuthorComment(classComment)) {
                                            authorCommentsInJavadoc.updateAndGet(v -> v + 1);
                                        }
                                        break;
                                }

                                title = String.format("%s (%s)", describe, path, file);
                            } else {
                                title = String.format("No element associated (%s)", path);
                            }
                        }
                    }.visit(javaParser.parse(file).getResult().get(), null);


                    try {

                        String s = classCommentBuilder.toString();

                        String replacedString = s.replaceAll("\\*|\\n", "");
                        int commentLines = s.trim().split("\\r\\n|\r|\n").length;
                        int commentLength = replacedString.length();

                        overallCommentLines.set(0, overallCommentLines.get(0) + commentLines);
                        overallCommentLength.set(0, overallCommentLength.get(0) + commentLength);
                        timesIncremented.set(0, timesIncremented.get(0) + 1);

                        csvPrinter.printRecord(path, s, methodCommentBuilder.toString(), methodBodyBuilder.toString(),
                                constructorCommentBuilder.toString(), constructorBodyBuilder.toString(),
                                fieldCommentBuilder.toString(), fieldBodyBuilder.toString(), commentLines, commentLength);
                        methodBodyBuilder.setLength(0);
                        methodCommentBuilder.setLength(0);
                        constructorBodyBuilder.setLength(0);
                        constructorCommentBuilder.setLength(0);
                        classCommentBuilder.setLength(0);
                        fieldCommentBuilder.setLength(0);
                        fieldBodyBuilder.setLength(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).explore(projectDir);


            System.out.println("Results for directory " + scanDir);
            System.out.println("Amount of classes: " + amountOfClasses);
            System.out.println("Amount of classes or interfaces without comment: " + classesWithoutComments);
            System.out.println("authorComments in dangling comments: " + authorCommentsDangling);
            System.out.println("authorComments in JavaDoc comments: " + authorCommentsInJavadoc);
            System.out.println("versionComments in dangling comments: " + versionCommentsDangling);
            System.out.println("versionComments in JavaDoc comments: " + versionCommentsInJavadoc);
            System.out.println("Overall class comment length is: " + overallCommentLength.get(0));
            System.out.println("Average class comment length is : " + overallCommentLength.get(0).doubleValue()
                                / (amountOfClasses.get().doubleValue() - classesWithoutComments.get().doubleValue()));
            System.out.println("Overall comment lines: " + overallCommentLines.get(0));
            System.out.println("Average class comment lines amount is : " + overallCommentLines.get(0).doubleValue()
                                / (amountOfClasses.get().doubleValue() - classesWithoutComments.get().doubleValue()));

            return out;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<CSVRecord> countCommentsForEachFile(String directory) {
        try {
            Process cloc = new ProcessBuilder(new ClassPathResource("c/cloc/cloc-1.84.exe").getURL().getPath(),
                    directory, "--by-file", "-csv", "--include-lang=Java").start();
            String s = IOUtils.toString(cloc.getInputStream());
            Reader in = new StringReader(s);
            CSVParser parser = new CSVParser(in, CSVFormat.EXCEL);
            List<CSVRecord> list = parser.getRecords();
            return list;
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean containsAuthorComment(String s) {
        return s.contains("@author");
    }

    public boolean containsVersionComment(String s) {
        return s.contains("@version");
    }

    public boolean isJavaDocComment() {
        return false;
    }

    private boolean isNodeAClassInterfaceEnum(Node node) {
        return node instanceof ClassOrInterfaceDeclaration
                || node instanceof AnnotationDeclaration
                || node instanceof EnumDeclaration;
    }

    private void incrementIfHasContendedCommentOrJustComment(TypeDeclaration<?> node, AtomicReference<Long> value) {
        List<Comment> allContainedComments = node.getAllContainedComments();
        Optional<Comment> comment = node.getComment();
        if (allContainedComments.size() == 0 && (!comment.isPresent() || comment.get().toString().isEmpty())) {
            value.updateAndGet(v -> v + 1L);
            System.out.println("Java file without comment " + node.getName());
        }
    }

}