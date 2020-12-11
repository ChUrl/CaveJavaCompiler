package util.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Node {

    private final String name;
    private String value;
    private final List<Node> children = new ArrayList<>();

    public Node(String name) {
        this.name = name;
        this.value = "";
    }

    public void addChild(Node node) {
        this.children.add(node);
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public List<Node> getChildren() {
        return this.children;
    }

    public String getName() {
        return this.name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            for (int i = 0; i < this.children.size(); i++) {
                if (!this.children.get(i).equals(((Node) obj).children.get(i))) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    // toString() und print() von hier: https://stackoverflow.com/a/8948691
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        this.print(buffer, "", "");
        return buffer.toString();
    }

    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(this.name);
        if (!this.value.isBlank()) {
            buffer.append(": ");
            buffer.append(this.value);
        }
        buffer.append('\n');

        for (Iterator<Node> it = this.children.listIterator(); it.hasNext(); ) {
            Node next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.value, this.children); // TODO: children?
    }

    public String getValue() {
        return this.value;
    }
}
