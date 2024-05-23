package com.ontimize.gui.tree;

import java.util.Map;

public class PageFetchTreeNode extends OTreeNode {

    public PageFetchTreeNode(Map<Object, Object> params) {
        super(params);
    }

    @Override
    public boolean isLeaf() {
        return super.isLeaf();
    }

    @Override
    protected void updateNodeTextCache() {
        this.cachedText = "more...";
        this.setUserObject(this.cachedText);
    }

    @Override
    public int compareTo(Object object) {
        return +1;
    }

}
