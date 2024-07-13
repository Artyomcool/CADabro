package com.github.artyomcool.cadabro;

import org.apache.commons.geometry.euclidean.threed.*;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.numbers.core.Precision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.geometry.euclidean.threed.Planes.convexPolygonFromVertices;
import static org.apache.commons.geometry.euclidean.threed.Planes.subsetFromConvexArea;

public class Extrude {
    public static final Precision.DoubleEquivalence e = Precision.doubleEquivalenceOfEpsilon(1e-8);
    public static final EmbeddingPlane basePlane = Planes.fromPointAndPlaneVectors(
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(0, 1, 0),
            e
    );

    public static List<PlaneConvexSubset> extrude(RegionBSPTree2D subspaceRegion, double delta, double scale) {
        final List<PlaneConvexSubset> extrudedBoundaries = new ArrayList<>();

        // add the boundaries
        addEnds(subspaceRegion, extrudedBoundaries, delta, scale);
        addSides(subspaceRegion, extrudedBoundaries, delta, scale);

        return extrudedBoundaries;
    }

    public static List<PlaneConvexSubset> extrudeRotate(RegionBSPTree2D subspaceRegion, double angle, int segments) {
        if (angle >= 360) {
            return extrudeRotateFull(subspaceRegion, segments);
        }

        return extrudeRotatePartial(subspaceRegion, angle, segments);
    }

    private static List<PlaneConvexSubset> extrudeRotateFull(RegionBSPTree2D subspaceRegion, int segments) {
        List<PlaneConvexSubset> result = new ArrayList<>();

        for (final LinePath path : subspaceRegion.getBoundaryPaths()) {
            for (final LineConvexSubset lineSubset : path.getElements()) {
                Vector2D subStartPt = lineSubset.getStartPoint();
                Vector2D subEndPt = lineSubset.getEndPoint();
                for (int i = 0; i < segments; i++) {
                    double angleStart = i * 360. / segments;
                    double angleEnd = (i + 1) * 360. / segments;

                    Vector3D startPt = rotateTo3D(subStartPt, angleStart);
                    Vector3D endPt = rotateTo3D(subEndPt, angleStart);
                    Vector3D startPtExtruded = rotateTo3D(subStartPt, angleEnd);
                    Vector3D endPtExtruded = rotateTo3D(subEndPt, angleEnd);

                    ConvexPolygon3D convex =
                            convexPolygonFromVertices(Arrays.asList(startPt, endPt, endPtExtruded, startPtExtruded), e);

                    result.add(convex);
                }
            }
        }

        return result;
    }

    private static List<PlaneConvexSubset> extrudeRotatePartial(RegionBSPTree2D subspaceRegion, double angle, int segments) {

        List<ConvexArea> baseAreas = subspaceRegion.toConvex();

        List<PlaneConvexSubset> result = new ArrayList<>();
        List<PlaneConvexSubset> extrudedList = new ArrayList<>(baseAreas.size());

        for (final ConvexArea area : baseAreas) {
            result.add(convexPolygonFromVertices(area.getVertices().stream().map(v -> Vector3D.of(v.getX(), v.getY(), 0)).toList().reversed(), e));
            extrudedList.add(convexPolygonFromVertices(area.getVertices().stream().map(v -> Vector3D.of(v.getX() * Math.cos(angle * Math.PI / 180), v.getY(), v.getX() * Math.sin(angle * Math.PI / 180))).toList(), e));
        }
        result.addAll(extrudedList);

        for (final LinePath path : subspaceRegion.getBoundaryPaths()) {
            for (final LineConvexSubset lineSubset : path.getElements()) {
                Vector2D subStartPt = lineSubset.getStartPoint();
                Vector2D subEndPt = lineSubset.getEndPoint();
                for (int i = 0; i < segments; i++) {
                    double angleStart = i * angle / segments;
                    double angleEnd = (i + 1) * angle / segments;

                    Vector3D startPt = rotateTo3D(subStartPt, angleStart);
                    Vector3D endPt = rotateTo3D(subEndPt, angleStart);
                    Vector3D startPtExtruded = rotateTo3D(subStartPt, angleEnd);
                    Vector3D endPtExtruded = rotateTo3D(subEndPt, angleEnd);

                    ConvexPolygon3D convex =
                            convexPolygonFromVertices(Arrays.asList(startPt, endPt, endPtExtruded, startPtExtruded), e);

                    result.add(convex);
                }
            }
        }

        return result;
    }

    private static Vector3D rotateTo3D(Vector2D p, double angle) {
        angle *= Math.PI / 180;
        return Vector3D.of(p.getX() * Math.cos(angle), p.getY(), p.getX() * Math.sin(angle));
    }

    /**
     * Add the end ("top" and "bottom") of the extruded subspace region to the result list.
     *
     * @param subspaceRegion subspace region being extruded.
     * @param result         list to add the boundary results to
     */
    private static void addEnds(RegionBSPTree2D subspaceRegion, List<? super PlaneConvexSubset> result, double h, double c) {
        // add the base boundaries
        final List<ConvexArea> baseAreas = subspaceRegion.toConvex();

        final List<PlaneConvexSubset> baseList = new ArrayList<>(baseAreas.size());
        final List<PlaneConvexSubset> extrudedList = new ArrayList<>(baseAreas.size());


        PlaneConvexSubset base;
        for (final ConvexArea area : baseAreas) {
            base = subsetFromConvexArea(basePlane, area);
            base = base.reverse();

            baseList.add(base);
            extrudedList.add(base.transform(Transformations.simple(v -> Vector3D.of(v.getX() * c, v.getY() * c, h))).reverse());
        }

        result.addAll(baseList);
        result.addAll(extrudedList);
    }

    /**
     * Add the side boundaries of the extruded region to the result list.
     *
     * @param subspaceRegion subspace region being extruded.
     * @param result         list to add the boundary results to
     */
    private static void addSides(final RegionBSPTree2D subspaceRegion, final List<? super PlaneConvexSubset> result, double h, double c) {
        Vector2D subStartPt;
        Vector2D subEndPt;

        PlaneConvexSubset boundary;
        for (final LinePath path : subspaceRegion.getBoundaryPaths()) {
            for (final LineConvexSubset lineSubset : path.getElements()) {
                subStartPt = lineSubset.getStartPoint();
                subEndPt = lineSubset.getEndPoint();

                boundary = extrudeSideFinite(basePlane.toSpace(subStartPt), basePlane.toSpace(subEndPt), h, c);

                result.add(boundary);
            }
        }
    }

    /**
     * Extrude a single, finite boundary forming one of the sides of the extruded region.
     *
     * @param startPt start point of the boundary
     * @param endPt   end point of the boundary
     * @return the extruded region side boundary
     */
    private static ConvexPolygon3D extrudeSideFinite(final Vector3D startPt, final Vector3D endPt, double h, double c) {

        final Vector3D extrudedStartPt = Vector3D.of(startPt.getX() * c, startPt.getY() * c, h);
        final Vector3D extrudedEndPt = Vector3D.of(endPt.getX() * c, endPt.getY() * c, h);

        final List<Vector3D> vertices = Arrays.asList(startPt, endPt, extrudedEndPt, extrudedStartPt);

        return convexPolygonFromVertices(vertices, e);
    }

}
