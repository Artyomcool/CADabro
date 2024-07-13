package com.github.artyomcool.cadabro.d3

import com.github.artyomcool.cadabro.Extrude
import com.github.artyomcool.cadabro.d2.CADObject2D
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset
import org.apache.commons.geometry.euclidean.threed.Vector3D
import org.apache.commons.geometry.euclidean.threed.rotation.AxisAngleSequence
import org.apache.commons.geometry.euclidean.threed.rotation.AxisSequence
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped
import org.apache.commons.geometry.euclidean.threed.shape.Sphere
import org.apache.commons.geometry.euclidean.twod.Vector2D
import org.apache.commons.geometry.euclidean.twod.shape.Circle
import org.apache.commons.numbers.core.Precision

import static com.github.artyomcool.cadabro.d2.CADObject2D.draw
import static com.github.artyomcool.cadabro.d3.BSPTree.from

class CADObjects {
    public static final Precision.DoubleEquivalence e = Precision.doubleEquivalenceOfEpsilon(1e-8)

    static CADObject3D sphere(double r = 0.5) {
        def sphere = Sphere.from(Vector3D.ZERO, r, e)

        fromTree { from(sphere.toTree(4)) }
    }

    static CADObject3D cube(double xyz = 1) {
        return cube(xyz, xyz, xyz)
    }

    static CADObject3D cube(double x, double y, double z) {
        def parallelepiped = Parallelepiped.builder(e)
                .setScale(x, y, z)
                .build()

        return fromTree(x / 2, y / 2, z / 2) { from(parallelepiped.toTree()) }
    }

    static CADObject3D cylinder(double h = 1, double rb = 1, double rt = rb) {
        // FIXME delegate to extrude through circle
        def c = Circle.from(Vector2D.ZERO, rb, e)

        double d = Math.max(rb, rt)
        fromPlanes(d, d, 0) { Extrude.extrude(c.toTree(64), h, rt / rb) }
    }

    static CADObject3D extrudeRotate(double angle = 360, CADObject2D obj2d) {
        fromPlanes { Extrude.extrudeRotate(obj2d.asTree(), angle, 64) }
    }

    static CADObject3D extrude(CADObject2D obj2d, double h = 1, double s = 1) {
        fromPlanes { Extrude.extrude(obj2d.asTree(), h, s) }
    }

    static CADObject2D rsquare(double w, double h = w, double r = 8) {
        draw(0, 0)
                .smooth(r)
                .dx(w)
                .smooth(r)
                .dy(h)
                .smooth(r)
                .dx(-w)
                .smooth(r)
                .close()
    }

    static CADCollection3D dx(double x) {
        dxyz(x, 0, 0)
    }

    static CADCollection3D dy(double y) {
        dxyz(0, y, 0)
    }

    static CADCollection3D dz(double z) {
        dxyz(0, 0, z)
    }

    static CADCollection3D dxy(double x, double y = x) {
        dxyz(x, y, 0)
    }

    static CADCollection3D dxz(double x, double z = x) {
        dxyz(x, 0, z)
    }

    static CADCollection3D dyz(double y, double z = y) {
        dxyz(0, y, z)
    }

    static CADCollection3D dxyz(double d) {
        dxyz(d, d, d)
    }

    static CADCollection3D dxyz(double x, double y, double z) {
        union().postProcess {
            it.transform(AffineTransformMatrix3D.createTranslation(x, y, z))
        }
    }

    static CADCollection3D rx(double x) {
        rxyz(x, 0, 0)
    }

    static CADCollection3D ry(double y) {
        rxyz(0, y, 0)
    }

    static CADCollection3D rz(double z) {
        rxyz(0, 0, z)
    }

    static CADCollection3D rxyz(double x, double y, double z) {
        union().postProcess {
            def rotation = QuaternionRotation.fromAxisAngleSequence(
                    AxisAngleSequence.createRelative(AxisSequence.XYZ, x * Math.PI / 180, y * Math.PI / 180, z * Math.PI / 180))
            it.transform(AffineTransformMatrix3D.identity().rotate(rotation))
        }
    }

    static CADCollection3D scaleX(double x) {
        return scale(x, 1, 1)
    }

    static CADCollection3D scaleY(double y) {
        return scale(1, y, 1)
    }

    static CADCollection3D scaleZ(double z) {
        return scale(1, 1, z)
    }

    static CADCollection3D scaleXY(double xy) {
        return scale(xy, xy, 1)
    }

    static CADCollection3D scaleXY(double x, double y) {
        return scale(x, y, 1)
    }

    static CADCollection3D scaleXZ(double xz) {
        return scale(xz, 1, xz)
    }

    static CADCollection3D scaleXZ(double x, double z) {
        return scale(x, 1, z)
    }

    static CADCollection3D scaleYZ(double yz) {
        return scale(1, yz, yz)
    }

    static CADCollection3D scaleYZ(double y, double z) {
        return scale(1, y, z)
    }

    static CADCollection3D scale(double xyz) {
        return scale(xyz, xyz, xyz)
    }

    static CADCollection3D scale(double x, double y, double z) {
        union().postProcess {
            it.transform(AffineTransformMatrix3D.createScale(x, y, z))
        }
    }

    static Union union(CADObject3D... obj) {
        new Union().call(obj)
    }

    static Diff diff(CADObject3D... obj) {
        new Diff().call(obj)
    }

    static Intersect intersect(CADObject3D... obj) {
        new Intersect().call(obj)
    }

    static Hull hull(CADObject3D... obj) {
        new Hull().call(obj)
    }

    static Union union(@DelegatesTo(Union) Closure c) {
        def u = union()
        c = c.rehydrate(u, c.owner, u)
        c(u)
        return u
    }

    static Diff diff(@DelegatesTo(Diff) Closure c) {
        def u = diff()
        c = c.rehydrate(u, c.owner, u)
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c(u)
        return u
    }

    static Intersect intersect(@DelegatesTo(Intersect) Closure c) {
        def u = intersect()
        c = c.rehydrate(u, c.owner, u)
        c(u)
        return u
    }

    static CADObject3D fromTree(double dx = 0, double dy = 0, double dz = 0, Closure<BSPTree> tree) {
        return new CADObject3D() {
            @Override
            BSPTree toTree() {
                return tree()
            }
        }.tap {
            if (dx != 0 || dy != 0 || dz != 0) {
                postProcess {
                    it.transform(AffineTransformMatrix3D.createTranslation(dx, dy, dz))
                }
            }
        }
    }

    static CADObject3D fromPlanes(double dx = 0, double dy = 0, double dz = 0, Closure<List<PlaneConvexSubset>> tree) {
        return fromTree(dx, dy, dz) { from tree() }
    }

}
