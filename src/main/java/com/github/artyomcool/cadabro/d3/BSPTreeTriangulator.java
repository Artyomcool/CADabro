package com.github.artyomcool.cadabro.d3;

import javafx.scene.paint.Color;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import com.github.artyomcool.cadabro.SamplingProfiler;

import java.util.*;

public class BSPTreeTriangulator {

    private static class VectorKey {
        final double x, y, z;
        private final int hashCode;

        VectorKey(Vector3D v) {
            this.x = Math.round(v.getX() / 1e-7) * 1e-7;
            this.y = Math.round(v.getY() / 1e-7) * 1e-7;
            this.z = Math.round(v.getZ() / 1e-7) * 1e-7;

            long hx = Double.doubleToLongBits(x);
            long hy = Double.doubleToLongBits(y);
            long hz = Double.doubleToLongBits(z);
            int h = (int) (hx ^ (hx >>> 32));
            h = 31 * h + (int) (hy ^ (hy >>> 32));
            h = 31 * h + (int) (hz ^ (hz >>> 32));
            this.hashCode = h;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VectorKey)) return false;
            VectorKey that = (VectorKey) o;
            return Double.compare(that.x, x) == 0 &&
                    Double.compare(that.y, y) == 0 &&
                    Double.compare(that.z, z) == 0;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    private static void characterizeHyperplaneSubset(
            HyperplaneConvexSubset<Vector3D> sub,
            AbstractRegionBSPTree.AbstractRegionNode<Vector3D, RegionNode> node,
            PolygonCollector in,
            PolygonCollector out
    ) {
        if (sub == null) {
            return;
        }
        if (node.isLeaf()) {
            if (in != null && node.isInside()) {
                in.add(sub);
            } else if (out != null && node.isOutside()) {
                out.add(sub);
            }
        } else {
            Split<? extends HyperplaneConvexSubset<Vector3D>> split = sub.split(node.getCutHyperplane());

            // Continue further on down the subtree with the same subset if the
            // subset lies directly on the current node's cut
            if (split.getLocation() == SplitLocation.NEITHER) {
                characterizeHyperplaneSubset(sub, node.getPlus(), in, out);
                characterizeHyperplaneSubset(sub, node.getMinus(), in, out);
            } else {
                characterizeHyperplaneSubset(split.getPlus(), node.getPlus(), in, out);
                characterizeHyperplaneSubset(split.getMinus(), node.getMinus(), in, out);
            }
        }
    }

    private abstract static class PolygonCollector {
        public abstract void add(HyperplaneConvexSubset<Vector3D> f);
    }

    private static void collectPolygons(RegionNode node, List<PolygonData> polygons, Map<VectorKey, Vector3D> uniqueVertices) {
        Color color = node.color;

        HyperplaneConvexSubset<Vector3D> sub = node.getCut();

        PolygonCollector insideFacingCollector = new PolygonCollector() {
            @Override
            public void add(HyperplaneConvexSubset<Vector3D> f) {
                PlaneConvexSubset p = (PlaneConvexSubset) f;
                List<Vector3D> points = getUniqueVertices(p.getVertices(), uniqueVertices);
                Collections.reverse(points);
                polygons.add(new PolygonData(points, color));
            }
        };
        PolygonCollector outsideFacingCollector = new PolygonCollector() {
            @Override
            public void add(HyperplaneConvexSubset<Vector3D> f) {
                PlaneConvexSubset p = (PlaneConvexSubset) f;
                List<Vector3D> points = getUniqueVertices(p.getVertices(), uniqueVertices);
                polygons.add(new PolygonData(points, color));
            }
        };
        PolygonCollector minusInCollector = new PolygonCollector() {
            @Override
            public void add(HyperplaneConvexSubset<Vector3D> f) {
                characterizeHyperplaneSubset(f, node.getPlus(), null, outsideFacingCollector);
            }
        };
        PolygonCollector minusOutCollector = new PolygonCollector() {
            @Override
            public void add(HyperplaneConvexSubset<Vector3D> f) {
                characterizeHyperplaneSubset(f, node.getPlus(), insideFacingCollector, null);
            }
        };
        characterizeHyperplaneSubset(sub, node.getMinus(), minusInCollector, minusOutCollector);
    }

    public static List<Triangle> collectAndTriangulate(Iterable<RegionNode> nodes, String callerStr) {
        SamplingProfiler.start(1); // Keep it 1ms, but now it's more accurate
        long start = System.nanoTime();

        List<PolygonData> polygons = new ArrayList<>(16 * 1024);
        Map<VectorKey, Vector3D> uniqueVertices = new HashMap<>(16 * 1024);

        for (RegionNode nodeObj : nodes) {
            HyperplaneConvexSubset<Vector3D> cut = nodeObj.getCut();
            if (cut == null) {
                continue;
            }

            collectPolygons(nodeObj, polygons, uniqueVertices);
        }

        long collectionDone = System.nanoTime();
        System.out.println("[" + callerStr + "] triangles: polygons collected in " + (collectionDone - start) / 1_000_000.0 + "ms, vertices: " + uniqueVertices.size() + ", polygons: " + polygons.size());

        List<Triangle> result = triangulateInternal(polygons, uniqueVertices.values(), collectionDone, callerStr);

        long end = System.nanoTime();
        System.out.println("[" + callerStr + "] triangles total " + (end - start) / 1_000_000.0 + "ms");

        SamplingProfiler.stop();

        return result;
    }

    private static List<Vector3D> getUniqueVertices(List<Vector3D> vertices, Map<VectorKey, Vector3D> uniqueVertices) {
        List<Vector3D> result = new ArrayList<>(vertices.size());
        for (Vector3D v : vertices) {
            VectorKey key = new VectorKey(v);
            Vector3D existing = uniqueVertices.putIfAbsent(key, v);
            result.add(existing == null ? v : existing);
        }
        return result;
    }

    private static List<Triangle> triangulateInternal(List<PolygonData> polygons, Collection<Vector3D> allVertices, long start, String callerStr) {
        List<Triangle> result = new ArrayList<>(32 * 1024);
        if (polygons.isEmpty()) return result;

        double gridScale = calculateGridScale(allVertices);
        Map<Long, List<Vector3D>> grid = buildGrid(allVertices, gridScale);

        long gridDone = System.nanoTime();
        System.out.println("[" + callerStr + "] triangles: grid built in " + (gridDone - start) / 1_000_000.0 + "ms");

        splitEdges(polygons, grid, gridScale);

        long splitDone = System.nanoTime();
        System.out.println("[" + callerStr + "] triangles: edges split in " + (splitDone - gridDone) / 1_000_000.0 + "ms");

        performTriangulation(polygons, result);

        long end = System.nanoTime();
        System.out.println("[" + callerStr + "] triangles: triangulated in " + (end - splitDone) / 1_000_000.0 + "ms, total logic " + (end - start) / 1_000_000.0 + "ms");

        return result;
    }

    private static double calculateGridScale(Collection<Vector3D> vertices) {
        double minVx = Double.POSITIVE_INFINITY, minVy = Double.POSITIVE_INFINITY, minVz = Double.POSITIVE_INFINITY;
        double maxVx = Double.NEGATIVE_INFINITY, maxVy = Double.NEGATIVE_INFINITY, maxVz = Double.NEGATIVE_INFINITY;
        for (Vector3D v : vertices) {
            double x = v.getX(), y = v.getY(), z = v.getZ();
            if (x < minVx) minVx = x;
            if (y < minVy) minVy = y;
            if (z < minVz) minVz = z;
            if (x > maxVx) maxVx = x;
            if (y > maxVy) maxVy = y;
            if (z > maxVz) maxVz = z;
        }
        double sizeX = maxVx - minVx, sizeY = maxVy - minVy, sizeZ = maxVz - minVz;
        double maxDim = Math.max(sizeX, Math.max(sizeY, sizeZ));
        return Math.max(1e-3, maxDim / 100.0);
    }

    private static Map<Long, List<Vector3D>> buildGrid(Collection<Vector3D> vertices, double gridScale) {
        Map<Long, List<Vector3D>> grid = new HashMap<>(vertices.size());
        for (Vector3D v : vertices) {
            long key = getGridKey(v.getX(), v.getY(), v.getZ(), gridScale);
            List<Vector3D> cell = grid.computeIfAbsent(key, k -> new ArrayList<>(4));
            cell.add(v);
        }
        return grid;
    }

    private static long getGridKey(double x, double y, double z, double scale) {
        long gx = (long) Math.floor(x / scale);
        long gy = (long) Math.floor(y / scale);
        long gz = (long) Math.floor(z / scale);
        return gx ^ (gy << 20) ^ (gz << 40);
    }

    private static void splitEdges(List<PolygonData> polygons, Map<Long, List<Vector3D>> grid, double gridScale) {
        List<Vector3D> candidates = new ArrayList<>(256);
        List<Vector3D> newPoints = new ArrayList<>(256);
        for (PolygonData poly : polygons) {
            List<Vector3D> polyPoints = poly.points;
            int initialSize = polyPoints.size();
            newPoints.clear();
            for (int i = 0; i < initialSize; i++) {
                Vector3D p1 = polyPoints.get(i);
                Vector3D p2 = polyPoints.get((i + 1) % initialSize);
                newPoints.add(p1);

                double p1x = p1.getX(), p1y = p1.getY(), p1z = p1.getZ();
                double p2x = p2.getX(), p2y = p2.getY(), p2z = p2.getZ();

                double dx = p2x - p1x;
                double dy = p2y - p1y;
                double dz = p2z - p1z;
                double lenSq = dx * dx + dy * dy + dz * dz;
                if (lenSq < 1e-20) continue;
                double len = Math.sqrt(lenSq);
                double invLen = 1.0 / len;
                double ux = dx * invLen;
                double uy = dy * invLen;
                double uz = dz * invLen;

                double minX = Math.min(p1x, p2x) - 1e-9;
                double minY = Math.min(p1y, p2y) - 1e-9;
                double minZ = Math.min(p1z, p2z) - 1e-9;
                double maxX = Math.max(p1x, p2x) + 1e-9;
                double maxY = Math.max(p1y, p2y) + 1e-9;
                double maxZ = Math.max(p1z, p2z) + 1e-9;

                candidates.clear();
                long gx1 = (long) Math.floor(minX / gridScale), gx2 = (long) Math.floor(maxX / gridScale);
                long gy1 = (long) Math.floor(minY / gridScale), gy2 = (long) Math.floor(maxY / gridScale);
                long gz1 = (long) Math.floor(minZ / gridScale), gz2 = (long) Math.floor(maxZ / gridScale);

                for (long x = gx1; x <= gx2; x++) {
                    for (long y = gy1; y <= gy2; y++) {
                        long xyKey = x ^ (y << 20);
                        for (long z = gz1; z <= gz2; z++) {
                            long key = xyKey ^ (z << 40);
                            List<Vector3D> cell = grid.get(key);
                            if (cell != null) {
                                for (Vector3D v : cell) {
                                    if (v == p1 || v == p2) continue;
                                    double vx = v.getX(), vy = v.getY(), vz = v.getZ();
                                    if (vx < minX || vx > maxX || vy < minY || vy > maxY || vz < minZ || vz > maxZ)
                                        continue;

                                    double tvx = vx - p1x;
                                    double tvy = vy - p1y;
                                    double tvz = vz - p1z;
                                    double distOnLine = tvx * ux + tvy * uy + tvz * uz;

                                    if (distOnLine > 1e-9 && distOnLine < len - 1e-9) {
                                        double prx = p1x + ux * distOnLine;
                                        double pry = p1y + uy * distOnLine;
                                        double prz = p1z + uz * distOnLine;

                                        double dpx = vx - prx;
                                        double dpy = vy - pry;
                                        double dpz = vz - prz;
                                        if (dpx * dpx + dpy * dpy + dpz * dpz < 1e-18) {
                                            candidates.add(v);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!candidates.isEmpty()) {
                    if (candidates.size() > 1) {
                        candidates.sort((v1, v2) -> {
                            double d1x = v1.getX() - p1x, d1y = v1.getY() - p1y, d1z = v1.getZ() - p1z;
                            double d2x = v2.getX() - p1x, d2y = v2.getY() - p1y, d2z = v2.getZ() - p1z;
                            return Double.compare(d1x * d1x + d1y * d1y + d1z * d1z, d2x * d2x + d2y * d2y + d2z * d2z);
                        });
                    }
                    newPoints.addAll(candidates);
                }
            }
            if (newPoints.size() < polyPoints.size()) {
                poly.points = new ArrayList<>(newPoints);
            }
        }
    }

    private static void performTriangulation(List<PolygonData> polygons, List<Triangle> result) {
        for (PolygonData poly : polygons) {
            List<Vector3D> points = poly.points;
            int ptsSize = points.size();
            if (ptsSize < 3) continue;
            Vector3D p1 = points.get(0);
            Color color = poly.color;
            for (int i = 1; i < ptsSize - 1; i++) {
                Triangle t = new Triangle();
                t.p1 = p1;
                t.p2 = points.get(i);
                t.p3 = points.get(i + 1);
                t.color = color;
                result.add(t);
            }
        }
    }
}
