package org.lxz.common;

import com.google.common.collect.Lists;
import org.lxz.analysis.NullLiteral;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<NodeType extends TreeNode<NodeType>> {
    protected ArrayList<NodeType> children = Lists.newArrayList();

    public NodeType getChild(int i) {
        return hasChild(i) ? children.get(i) : null;
    }

    public void removeNullChild() {
        children.removeIf(child -> child instanceof NullLiteral);
    }

    public void addChild(NodeType n) {
        children.add(n);
    }

    public void addChildren(List<? extends NodeType> n) {
        children.addAll(n);
    }

    public boolean hasChild(int i) {
        return children.size() > i;
    }

    public void setChild(int index, NodeType n) {
        children.set(index, n);
    }

    public ArrayList<NodeType> getChildren() {
        return children;
    }

    public void clearChildren() {
        children.clear();
    }
}
