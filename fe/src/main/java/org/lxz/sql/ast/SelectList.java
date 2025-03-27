package org.lxz.sql.ast;

import com.google.common.collect.Lists;
import org.lxz.analysis.HintNode;

import java.util.List;

/**
 * Select list items plus distinct clause.
 */
public class SelectList {
    private boolean isDistinct;
    private List<HintNode> hitNodes;

    // BEGIN: Members that need to be reset()
    private final List<SelectListItem> items;

    // END: Members that need to be reset()

    public SelectList(SelectList other) {
        items = Lists.newArrayList();
        for (SelectListItem item : other.items) {
            items.add(item.clone());
        }
        isDistinct = other.isDistinct;
    }

    public SelectList() {
        items = Lists.newArrayList();
        this.isDistinct = false;
    }

    public SelectList(List<SelectListItem> items, boolean isDistinct) {
        this.isDistinct = isDistinct;
        this.items = items;
    }

    public List<SelectListItem> getItems() { return items; }

    public void addItem(SelectListItem item) { items.add(item); }

    public boolean isDistinct() { return isDistinct; }

    public void setIsDistinct(boolean isDistinct) { this.isDistinct = isDistinct; }

    public void reset() {
        for (SelectListItem item : items) {
            if (!item.isStar()) {
                item.getExpr().reset();
            }
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new SelectList(this);
    }

    public List<HintNode> getHitNodes() {
        return hitNodes;
    }

    public void setHitNodes(List<HintNode> hitNodes) {
        this.hitNodes = hitNodes;
    }
}
