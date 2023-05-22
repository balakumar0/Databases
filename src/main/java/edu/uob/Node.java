package edu.uob;

public class Node {
    private final SyntaxType type;
    private String value;
    private Node child;
    private Node sibling;
    public Node(SyntaxType type)
    {
        this.type = type;
    }
    public Node(SyntaxType type, String value)
    {
        this.type = type;
        this.value = value;
    }

    public void setSibling(Node n)
    {
        sibling = n;
    }

    public void setChild(Node n)
    {
        child = n;
    }

    public Node getSibling()
    {
        return sibling;
    }

    public Node getChild()
    {
        return child;
    }

    public SyntaxType getType()
    {
        return type;
    }

    public String getValue()
    {
        return value;
    }

}
