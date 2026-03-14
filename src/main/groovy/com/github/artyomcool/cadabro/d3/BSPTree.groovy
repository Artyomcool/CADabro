package com.github.artyomcool.cadabro.d3

import javafx.scene.paint.Color
import org.apache.commons.geometry.core.partitioning.Hyperplane
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset
import org.apache.commons.geometry.core.partitioning.Split
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree
import org.apache.commons.geometry.euclidean.threed.*
import org.apache.commons.numbers.core.Precision

import java.util.stream.Stream
import java.util.stream.StreamSupport

import static com.github.artyomcool.cadabro.d3.CADObjects.e

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
        condense()
        List<Triangle> result = new ArrayList<>(32 * 1024)

        TreeMap<Vector3D, Vector3D> vertices = new TreeMap<>((Vector3D o1, Vector3D o2) -> {
            int compare = e.compare(o1.x, o2.x)
            if (compare != 0) return compare
            compare = e.compare(o1.y, o2.y)
            if (compare != 0) return compare
            return e.compare(o1.z, o2.z)
        })

        def getVertex = { Vector3D p ->
            vertices.putIfAbsent(p, p) ?: p
        }

        def polygons = []
        for (def node in nodes()) {
            if (node.isInternal()) {
                def boundary = node.cutBoundary
                for (def f in boundary.outsideFacing) {
                    PlaneConvexSubset p = (PlaneConvexSubset) f
                    polygons.add([
                            points: p.vertices.collect(getVertex),
                            color: node.color,
                            normal: p.plane.normal
                    ])
                }
                for (def f in boundary.insideFacing) {
                    PlaneConvexSubset p = (PlaneConvexSubset) f.reverse()
                    polygons.add([
                            points: p.vertices.collect(getVertex),
                            color: node.color,
                            normal: p.plane.normal
                    ])
                }
            }
        }

        // Split edges with vertices that lie on them
        for (def poly in polygons) {
            List<Vector3D> newPoints = []
            for (int i = 0; i < poly.points.size(); i++) {
                Vector3D p1 = poly.points[i]
                Vector3D p2 = poly.points[(i + 1) % poly.points.size()]
                newPoints.add(p1)

                Vector3D dir = p2.subtract(p1)
                double len = dir.norm()
                if (len < 1e-10) continue
                Vector3D unitDir = dir.multiply(1.0 / len)

                // Find all vertices between p1 and p2
                List<Vector3D> intermediate = []
                Vector3D minP = Vector3D.of(Math.min(p1.x, p2.x) - 1e-9, Math.min(p1.y, p2.y) - 1e-9, Math.min(p1.z, p2.z) - 1e-9)
                Vector3D maxP = Vector3D.of(Math.max(p1.x, p2.x) + 1e-9, Math.max(p1.y, p2.y) + 1e-9, Math.max(p1.z, p2.z) + 1e-9)

                for (Vector3D v in vertices.subMap(minP, maxP).values()) {
                    if (v == p1 || v == p2) continue
                    if (e.compare(v.x, minP.x) < 0 || e.compare(v.x, maxP.x) > 0) continue
                    if (e.compare(v.y, minP.y) < 0 || e.compare(v.y, maxP.y) > 0) continue
                    if (e.compare(v.z, minP.z) < 0 || e.compare(v.z, maxP.z) > 0) continue

                    Vector3D toV = v.subtract(p1)
                    double distOnLine = toV.dot(unitDir)
                    if (distOnLine > 1e-9 && distOnLine < len - 1e-9) {
                        Vector3D projection = p1.add(unitDir.multiply(distOnLine))
                        if (v.distance(projection) < 1e-9) {
                            intermediate.add(v)
                        }
                    }
                }
                if (!intermediate.isEmpty()) {
                    intermediate.sort { v -> v.subtract(p1).norm() }
                    newPoints.addAll(intermediate)
                }
            }
            poly.points = newPoints
        }

        // Triangulate
        for (def poly in polygons) {
            if (poly.points.size() < 3) continue
            Vector3D p1 = poly.points[0]
            for (int i = 1; i < poly.points.size() - 1; i++) {
                result.add(new Triangle(
                        p1: p1,
                        p2: poly.points[i],
                        p3: poly.points[i + 1],
                        norm: poly.normal,
                        color: poly.color
                ))
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
