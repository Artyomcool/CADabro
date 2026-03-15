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

    static class VectorKey {
        final double x, y, z
        private final int hashCode

        VectorKey(Vector3D v, Precision.DoubleEquivalence e) {
            this.x = Math.round(v.x / 1e-7) * 1e-7
            this.y = Math.round(v.y / 1e-7) * 1e-7
            this.z = Math.round(v.z / 1e-7) * 1e-7
            
            long hx = Double.doubleToLongBits(x)
            long hy = Double.doubleToLongBits(y)
            long hz = Double.doubleToLongBits(z)
            int h = (int) (hx ^ (hx >>> 32))
            h = 31 * h + (int) (hy ^ (hy >>> 32))
            h = 31 * h + (int) (hz ^ (hz >>> 32))
            this.hashCode = h
        }

        @Override
        boolean equals(Object o) {
            if (this === o) return true
            if (!(o instanceof VectorKey)) return false
            VectorKey that = (VectorKey) o
            return Double.compare(that.x, x) == 0 &&
                    Double.compare(that.y, y) == 0 &&
                    Double.compare(that.z, z) == 0
        }

        @Override
        int hashCode() {
            return hashCode
        }
    }

    static class PolygonData {
        List<Vector3D> points
        Color color
        Vector3D normal
    }

    List<Triangle> triangles() {
        long start = System.currentTimeMillis()
        def caller = Thread.currentThread().stackTrace.find { it.className != BSPTree.class.name && it.className != 'java.lang.Thread' && !it.className.contains(".groovy") }
        def callerStr = caller ? "${caller.className.tokenize('$')[0].tokenize('.')[-1]}.${caller.methodName}:${caller.lineNumber}" : "unknown"

        condense()
        List<Triangle> result = new ArrayList<>(32 * 1024)

        Map<VectorKey, Vector3D> vertices = new HashMap<>(32 * 1024)

        def getVertex = { Vector3D p ->
            def key = new VectorKey(p, e)
            def existing = vertices.get(key)
            if (existing != null) return existing
            vertices.put(key, p)
            return p
        }

        List<PolygonData> polygons = new ArrayList<>(16 * 1024)
        for (def node in nodes()) {
            if (node.isInternal()) {
                def boundary = node.cutBoundary
                def color = node.color
                for (def f in boundary.outsideFacing) {
                    PlaneConvexSubset p = (PlaneConvexSubset) f
                    polygons.add(new PolygonData(
                            points: p.vertices.collect(getVertex),
                            color: color,
                            normal: p.plane.normal
                    ))
                }
                for (def f in boundary.insideFacing) {
                    PlaneConvexSubset p = (PlaneConvexSubset) f.reverse()
                    polygons.add(new PolygonData(
                            points: p.vertices.collect(getVertex),
                            color: color,
                            normal: p.plane.normal
                    ))
                }
            }
        }

        long polygonsDone = System.currentTimeMillis()
        println "[$callerStr] triangles: polygons collected in ${polygonsDone - start}ms, vertices: ${vertices.size()}, polygons: ${polygons.size()}"

        if (polygons.isEmpty()) return result

        // Calculate bounding box to determine grid scale
        double minVx = Double.POSITIVE_INFINITY, minVy = Double.POSITIVE_INFINITY, minVz = Double.POSITIVE_INFINITY
        double maxVx = Double.NEGATIVE_INFINITY, maxVy = Double.NEGATIVE_INFINITY, maxVz = Double.NEGATIVE_INFINITY
        for (Vector3D v in vertices.values()) {
            if (v.x < minVx) minVx = v.x
            if (v.y < minVy) minVy = v.y
            if (v.z < minVz) minVz = v.z
            if (v.x > maxVx) maxVx = v.x
            if (v.y > maxVy) maxVy = v.y
            if (v.z > maxVz) maxVz = v.z
        }
        
        double sizeX = maxVx - minVx, sizeY = maxVy - minVy, sizeZ = maxVz - minVz
        double maxDim = Math.max(sizeX, Math.max(sizeY, sizeZ))
        double gridScale = Math.max(1e-3, maxDim / 100.0) // Finer grid
        
        Map<Long, List<Vector3D>> grid = new HashMap<>(vertices.size())
        def getGridKey = { double val -> (long) Math.floor(val / gridScale) }
        
        for (Vector3D v in vertices.values()) {
            long gx = getGridKey(v.x)
            long gy = getGridKey(v.y)
            long gz = getGridKey(v.z)
            long key = gx ^ (gy << 20) ^ (gz << 40)
            List<Vector3D> cell = grid.get(key)
            if (cell == null) {
                cell = new ArrayList<Vector3D>(4)
                grid.put(key, cell)
            }
            cell.add(v)
        }

        long gridDone = System.currentTimeMillis()
        println "[$callerStr] triangles: grid built in ${gridDone - polygonsDone}ms"

        // Split edges with vertices that lie on them
        List<Vector3D> candidates = new ArrayList<>(256)
        for (PolygonData poly in polygons) {
            List<Vector3D> polyPoints = poly.points
            int initialSize = polyPoints.size()
            List<Vector3D> newPoints = new ArrayList<>(initialSize * 2)
            for (int i = 0; i < initialSize; i++) {
                Vector3D p1 = polyPoints.get(i)
                Vector3D p2 = polyPoints.get((i + 1) % initialSize)
                newPoints.add(p1)

                double dx = p2.x - p1.x
                double dy = p2.y - p1.y
                double dz = p2.z - p1.z
                double lenSq = dx * dx + dy * dy + dz * dz
                if (lenSq < 1e-20) continue
                double len = Math.sqrt(lenSq)
                double invLen = 1.0 / len
                double ux = dx * invLen
                double uy = dy * invLen
                double uz = dz * invLen

                double minX = Math.min(p1.x, p2.x) - 1e-9
                double minY = Math.min(p1.y, p2.y) - 1e-9
                double minZ = Math.min(p1.z, p2.z) - 1e-9
                double maxX = Math.max(p1.x, p2.x) + 1e-9
                double maxY = Math.max(p1.y, p2.y) + 1e-9
                double maxZ = Math.max(p1.z, p2.z) + 1e-9

                candidates.clear()
                long gx1 = getGridKey(minX), gx2 = getGridKey(maxX)
                long gy1 = getGridKey(minY), gy2 = getGridKey(maxY)
                long gz1 = getGridKey(minZ), gz2 = getGridKey(maxZ)
                
                for (long x = gx1; x <= gx2; x++) {
                    for (long y = gy1; y <= gy2; y++) {
                        long xyKey = x ^ (y << 20)
                        for (long z = gz1; z <= gz2; z++) {
                            long key = xyKey ^ (z << 40)
                            List<Vector3D> cell = grid.get(key)
                            if (cell != null) {
                                for (int j = 0; j < cell.size(); j++) {
                                    Vector3D v = cell.get(j)
                                    if (v === p1 || v === p2) continue
                                    if (v.x < minX || v.x > maxX || v.y < minY || v.y > maxY || v.z < minZ || v.z > maxZ) continue
                                    
                                    double tvx = v.x - p1.x
                                    double tvy = v.y - p1.y
                                    double tvz = v.z - p1.z
                                    double distOnLine = tvx * ux + tvy * uy + tvz * uz
                                    
                                    if (distOnLine > 1e-9 && distOnLine < len - 1e-9) {
                                        double prx = p1.x + ux * distOnLine
                                        double pry = p1.y + uy * distOnLine
                                        double prz = p1.z + uz * distOnLine
                                        
                                        double dpx = v.x - prx
                                        double dpy = v.y - pry
                                        double dpz = v.z - prz
                                        if (dpx * dpx + dpy * dpy + dpz * dpz < 1e-18) {
                                            candidates.add(v)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (!candidates.isEmpty()) {
                    if (candidates.size() > 1) {
                        candidates.sort { v -> 
                            double dvx = v.x - p1.x
                            double dvy = v.y - p1.y
                            double dvz = v.z - p1.z
                            dvx * dvx + dvy * dvy + dvz * dvz
                        }
                    }
                    newPoints.addAll(candidates)
                }
            }
            poly.points = newPoints
        }

        long splitDone = System.currentTimeMillis()
        println "[$callerStr] triangles: edges split in ${splitDone - gridDone}ms"

        // Triangulate
        for (PolygonData poly in polygons) {
            List<Vector3D> points = poly.points
            int ptsSize = points.size()
            if (ptsSize < 3) continue
            Vector3D p1 = points.get(0)
            Vector3D normal = poly.normal
            Color color = poly.color
            for (int i = 1; i < ptsSize - 1; i++) {
                result.add(new Triangle(
                        p1: p1,
                        p2: points.get(i),
                        p3: points.get(i + 1),
                        norm: normal,
                        color: color
                ))
            }
        }

        long end = System.currentTimeMillis()
        println "[$callerStr] triangles: triangulated in ${end - splitDone}ms, total ${end - start}ms"

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
