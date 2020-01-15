package edu.unibe.kravchenko.extractor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;

import java.util.List;
import java.util.stream.Collectors;

public class NodeDescriber {

    public NodeContent describe(Node node, String path, String folder) {

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
            nodeContent.setComment(declaration.getComment().map(Comment::toString).orElse(""));
            nodeContent.setType("class");
            return nodeContent;
        }

        if (node instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
            List<String> varNames = fieldDeclaration.getVariables().stream().map(v -> v.getName().getId()).collect(Collectors.toList());
            String a = "Field " + String.join(", ", varNames);

            NodeContent nodeContent = new NodeContent();
            nodeContent.setBody(a);
            nodeContent.setComment(fieldDeclaration.getComment().map(Comment::toString).orElse(""));
            nodeContent.setType("field");
            return nodeContent;
        } else {
        }


        return empty;
    }

}
