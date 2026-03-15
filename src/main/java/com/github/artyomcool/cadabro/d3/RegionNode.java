package com.github.artyomcool.cadabro.d3;

import javafx.scene.paint.Color;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

public class RegionNode extends AbstractRegionBSPTree.AbstractRegionNode<Vector3D, RegionNode> {

    public Color color;

    public RegionNode(final AbstractBSPTree<Vector3D, RegionNode> tree) {
        super(tree);
    }

    @Override
    protected RegionNode getSelf() {
        return this;
    }
}
