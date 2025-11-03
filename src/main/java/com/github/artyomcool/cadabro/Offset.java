package com.github.artyomcool.cadabro;

import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.numbers.core.Precision;

import java.util.ArrayList;
import java.util.List;

import static com.github.artyomcool.cadabro.Transformations.t3d;

public class Offset {
    public static final Precision.DoubleEquivalence e = Precision.doubleEquivalenceOfEpsilon(1e-8);

    public static RegionBSPTree3D offset(RegionBSPTree3D tree, double offset) {
        RegionBSPTree3D result = new RegionBSPTree3D();
        List<ConvexVolume> volumes = tree.toConvex();
        for (ConvexVolume convex : volumes) {
            List<Plane> planes = new ArrayList<>();
            for (PlaneConvexSubset boundary : convex.getBoundaries()) {
                Plane plane = boundary.getPlane();
                planes.add(plane.transform(t3d(v -> v.add(offset, plane.getNormal()))));
            }
            try {
                result.union(ConvexVolume.fromBounds(planes).toTree());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }

    public static RegionBSPTree2D offset(RegionBSPTree2D tree, double offset) {
        RegionBSPTree2D.PartitionedRegionBuilder2D builder2D = RegionBSPTree2D.partitionedRegionBuilder();
        for (LinePath path : tree.getBoundaryPaths()) {
            List<LineConvexSubset> elements = path.getElements();
            List<Vector2D> vertexes = new ArrayList<>(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                Line one = elements.get(i).getLine();
                Line two = elements.get((i + 1) % elements.size()).getLine();

                Vector2D normal1 = one.getOffsetDirection();
                one = one.transform(Transformations.simple(v -> v.add(offset, normal1)));

                Vector2D normal2 = two.getOffsetDirection();
                two = two.transform(Transformations.simple(v -> v.add(offset, normal2)));

                Vector2D intersection = one.intersection(two);
                if (intersection != null) {
                    vertexes.add(intersection);
                }
            }

            builder2D.insertBoundaries(LinePath.fromVertexLoop(vertexes, e));
        }
        return builder2D.build();
    }

}
