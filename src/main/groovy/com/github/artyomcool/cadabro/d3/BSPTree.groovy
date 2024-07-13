package com.github.artyomcool.cadabro.d3

import javafx.scene.paint.Color
import org.apache.commons.geometry.core.internal.IteratorTransform
import org.apache.commons.geometry.core.partitioning.Hyperplane
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset
import org.apache.commons.geometry.core.partitioning.Split
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutBoundary
import org.apache.commons.geometry.euclidean.threed.*

import java.util.stream.Stream
import java.util.stream.StreamSupport

import static com.github.artyomcool.cadabro.d3.CADObjects.*

class BSPTree extends AbstractRegionBSPTree<Vector3D, RegionNode> implements BoundarySource3D {
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

    public RegionNode copySubtree(RegionBSPTree3D.RegionNode3D src, RegionNode dst) {
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
            if (node.isInternal()) {
                def boundary = node.cutBoundary
                for (def f in boundary.outsideFacing) {
                    PlaneConvexSubset p = (PlaneConvexSubset) f
                    result.add(p)
                }
                for (def f in boundary.insideFacing) {
                    PlaneConvexSubset p = (PlaneConvexSubset) f
                    result.add(p.reverse())
                }
            }
        }
        return result
    }

    List<Triangle> triangles() {
        condense()
        List<Triangle> result = new ArrayList<>(32 * 1024)
        TreeMap<Vector3D, Vector3D> remap = new TreeMap<>(new Comparator<Vector3D>() {
            @Override
            int compare(Vector3D o1, Vector3D o2) {
                def compare = e.compare(o1.x, o2.x)
                if (compare != 0) {
                    return compare
                }
                compare = e.compare(o1.y, o2.y)
                if (compare != 0) {
                    return compare
                }
                return e.compare(o1.z, o2.z)
            }
        })
        def repoint = (Vector3D p) -> {
            return remap.putIfAbsent(p, p) ?: p
        }
        for (def node in nodes()) {
            if (node.isInternal()) {
                def boundary = node.cutBoundary
                for (def f in boundary.outsideFacing) {
                    PlaneConvexSubset p = (PlaneConvexSubset) f
                    for (def t in p.toTriangles()) {
                        def tr = new Triangle()
                        tr.color = node.color
                        tr.p1 = repoint(t.point1)
                        tr.p2 = repoint(t.point2)
                        tr.p3 = repoint(t.point3)
                        tr.norm = p.plane.normal
                        result.add(tr)
                    }
                }
                for (def f in boundary.insideFacing) {
                    PlaneConvexSubset p = (PlaneConvexSubset) f
                    for (def t in p.reverse().toTriangles()) {
                        def tr = new Triangle()
                        tr.color = node.color
                        tr.p1 = repoint(t.point1)
                        tr.p2 = repoint(t.point2)
                        tr.p3 = repoint(t.point3)
                        tr.norm = p.plane.normal
                        result.add(tr)
                    }
                }
            }
        }
        return result
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

    static class RegionNode extends AbstractRegionBSPTree.AbstractRegionNode<Vector3D, RegionNode> {

        public Color color

        RegionNode(final AbstractBSPTree<Vector3D, RegionNode> tree) {
            super(tree);
        }

        @Override
        protected RegionNode getSelf() {
            return this
        }
    }

    static class Triangle {
        Vector3D p1,p2,p3
        Vector3D norm
        Color color
    }

}
