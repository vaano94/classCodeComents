package edu.unibe.kravchenko.extractor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Record {

    private String classFileName;
    private String commentAmount;
    private String codeAmount;
    private double ratio;
    private String classComment;
    private String methodComment;
    private String methodBody;
    private String constructorComment;
    private String constructorBody;
    private String fieldComment;
    private String fieldBody;
    private String classCommentLineLength;
    private String classCommentSymbolLength;

}
