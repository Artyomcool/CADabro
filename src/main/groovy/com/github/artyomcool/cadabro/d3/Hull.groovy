package com.github.artyomcool.cadabro.d3

import com.github.quickhull3d.QuickHull3D
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset
import org.apache.commons.geometry.euclidean.threed.Planes
import org.apache.commons.geometry.euclidean.threed.Vector3D

import static com.github.artyomcool.cadabro.d3.CADObjects.e
import static com.github.artyomcool.cadabro.d3.CADObjects.fromTree

class Hull extends Union {

    static CADObject3D of(List<Vector3D> points) {
        fromTree { hull(points) }
    }

    static CADObject3D of(Vector3D... points) {
        of(Arrays.asList(points))
    }

    static CADObject3D of(double... points) {
        fromTree { hull(points) }
    }

    @Override
    BSPTree toTree() {
        def tree = super.toTree();
        List<Vector3D> points = []
        for (def c in tree.triangles()) {
            points << c.p1 << c.p2 << c.p3
        }
        return hull(points)
    }

    private static BSPTree hull(List<Vector3D> points) {
        double[] p = new double[points.size() * 3]
        int i = 0
        for (def pp in points) {
            p[i++] = pp.x
            p[i++] = pp.y
            p[i++] = pp.z
        }


        return hull(p)
    }

    private static BSPTree hull(double[] p) {
        def hull3D = new QuickHull3D(p)
        List<PlaneConvexSubset> result = new ArrayList<>()

        def faces = hull3D.faces
        def vertices = hull3D.vertices
        for (def face in faces) {
            List<Vector3D> v = new ArrayList<>(faces.length)
            for (def pi in face) {
                def f = vertices[pi]
                v.add(Vector3D.of(f.x, f.y, f.z))
            }
            result.add(Planes.convexPolygonFromVertices(v, e))
        }

        return BSPTree.from(result)
    }
}
