package org.apache.spark.shuffle.api;

import java.util.Map;

import org.apache.spark.annotation.Private;

// just some comment class
@Private
public class ClassWithSomeComment {

    // I should not be counted
    public void comment() {

    }

    /**
     * But I should
     */
    public void comment2() {

    }

}
