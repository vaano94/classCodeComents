package edu.unibe.kravchenko.extractor.test;

import lombok.ToString;

import java.util.Map;



// more more more
// more single commentt @author @version
// just some comment class
@ToString
public class ClassWithComment {

    // I should not be counted
    public void comment() {

    }

    /**
     * But I should
     */
    public void comment2() {

    }

}
