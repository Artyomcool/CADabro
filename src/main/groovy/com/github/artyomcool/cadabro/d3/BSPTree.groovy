package com.github.artyomcool.cadabro.d3

import javafx.scene.paint.Color
import org.apache.commons.geometry.core.partitioning.Hyperplane
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset
import org.apache.commons.geometry.core.partitioning.Split
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree
import org.apache.commons.geometry.euclidean.threed.*

import java.util.stream.Stream
import java.util.stream.StreamSupport

class BSPTree extends AbstractRegionBSPTree<Vector3D, RegionNode> implements BoundarySource3D {

    private static final double EPS = 1e-6

    BSPTree(boolean full) {
        super(full)
    }

    static BSPTree from(RegionBSPTree3D b) {
        def tree = new BSPTree(false)
        tree.copySubtree(b.root, tree.root)
        return tree
    }

    static BSPTree from(List<PlaneConvexSubset> planes) {
        def tree = new BSPTree(false)
        tree.insert(planes);
        return tree
    }

    RegionNode copySubtree(RegionBSPTree3D.RegionNode3D src, RegionNode dst) {
        // only copy if we're actually switching nodes
        // copy non-structural properties
        dst.setLocationValue(src.getLocation());

        // copy the subtree structure
        HyperplaneConvexSubset<Vector3D> cut = null;
        RegionNode minus = null;
        RegionNode plus = null;

        if (!src.isLeaf()) {
            final AbstractBSPTree<Vector3D, RegionNode> dstTree = dst.getTree();

            cut = src.getCut();
            minus = copySubtree(src.getMinus(), dstTree.createNode());
            plus = copySubtree(src.getPlus(), dstTree.createNode());
        }

        dst.setSubtree(cut, minus, plus);

        return dst;
    }

    List<ConvexVolume> toConvex() {
        final List<ConvexVolume> result = new ArrayList<>();

        toConvexRecursive(getRoot(), ConvexVolume.full(), result);

        return result;
    }

    @Override
    protected void copyNodeProperties(RegionNode src, RegionNode dst) {
        super.copyNodeProperties(src, dst)
        if (src.color != null || dst.color != null) {
            dst.color = src.color
        }
    }

    private void toConvexRecursive(final RegionNode node, final ConvexVolume nodeVolume,
                                   final List<? super ConvexVolume> result) {

        if (node.isLeaf()) {
            // base case; only add to the result list if the node is inside
            if (node.isInside()) {
                result.add(nodeVolume);
            }
        } else {
            // recurse
            Split<ConvexVolume> split = nodeVolume.split(node.getCutHyperplane());
            toConvexRecursive(node.getMinus(), split.getMinus(), result);
            toConvexRecursive(node.getPlus(), split.getPlus(), result);
        }
    }

    List<PlaneConvexSubset> planes() {
        condense()
        List<PlaneConvexSubset> result = new ArrayList<>(32 * 1024)
        for (def node in nodes()) {
            if (!node.isInternal()) {
                continue
            }
            def boundary = node.cutBoundary
            for (def f in boundary.outsideFacing) {
                result.add((PlaneConvexSubset) f)
            }
            for (def f in boundary.insideFacing) {
                result.add((PlaneConvexSubset) f.reverse())
            }
        }
        return result
    }

    List<Triangle> triangles() {
        def caller = Thread.currentThread().stackTrace.find { it.className != BSPTree.class.name && it.className != 'java.lang.Thread' && !it.className.contains(".groovy") }
        def callerStr = caller ? "${caller.className.tokenize('$')[0].tokenize('.')[-1]}.${caller.methodName}:${caller.lineNumber}" : "unknown"

        condense()

        return BSPTreeTriangulator.collectAndTriangulate(nodes(), callerStr)
    }

    BSPTree copy() {
        def tree = new BSPTree(false)
        tree.copy(this)
        return tree
    }

    @Override
    protected RegionSizeProperties<Vector3D> computeRegionSizeProperties() {
        if (isFull()) {
            return new RegionSizeProperties<>(Double.POSITIVE_INFINITY, null);
        } else if (isEmpty()) {
            return new RegionSizeProperties<>(0, null);
        }

        final RegionBSPTree3D.RegionSizePropertiesVisitor visitor = new RegionBSPTree3D.RegionSizePropertiesVisitor();
        accept(visitor);

        return visitor.getRegionSizeProperties();
    }

    @Override
    Split<? extends HyperplaneBoundedRegion<Vector3D>> split(Hyperplane<Vector3D> splitter) {
        return split(splitter, new BSPTree(false), new BSPTree(false));
    }

    @Override
    protected RegionNode createNode() {
        return new RegionNode(this);
    }

    @Override
    Iterable<PlaneConvexSubset> boundaries() {
        return createBoundaryIterable(PlaneConvexSubset.class::cast);
    }

    @Override
    Stream<PlaneConvexSubset> boundaryStream() {
        return StreamSupport.stream(boundaries().spliterator(), false);
    }

    @Override
    List<PlaneConvexSubset> getBoundaries() {
        return createBoundaryList(PlaneConvexSubset.class::cast);
    }


    static class Triangle {
        public Vector3D p1,p2,p3
        public Vector3D norm
        public Color color
    }

}
