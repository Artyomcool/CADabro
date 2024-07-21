package com.github.artyomcool.cadabro.d3

import javafx.scene.paint.Color
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D
import org.apache.commons.geometry.euclidean.threed.rotation.AxisAngleSequence
import org.apache.commons.geometry.euclidean.threed.rotation.AxisSequence
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation

import java.util.function.Consumer

import static com.github.artyomcool.cadabro.d3.CADObjects.fromTree
import static com.github.artyomcool.cadabro.d3.CADObjects.hull

abstract class CADObject3D {

    private Exception stack = new Exception()
    private BSPTree cachedTree
    private Bounds cachedBounds
    private Consumer<BSPTree> postProcess = t -> { }

    protected abstract BSPTree toTree();

    Union plus(CADObject3D obj) {
        operation(new Union(), obj)
    }

    Union or(CADObject3D obj) {
        operation(new Union(), obj)
    }

    Diff minus(CADObject3D obj) {
        operation(new Diff(), obj)
    }

    Intersect and(CADObject3D obj) {
        operation(new Intersect(), obj)
    }

    Hull bitwiseNegate() {
        hull this
    }

    CADObject3D dx(double x) {
        return dxyz(x, 0, 0)
    }

    CADObject3D dy(double y) {
        return dxyz(0, y, 0)
    }

    CADObject3D dz(double z) {
        return dxyz(0, 0, z)
    }

    CADObject3D dxy(double x, double y = x) {
        return dxyz(x, y, 0)
    }

    CADObject3D dxz(double x, double z = x) {
        return dxyz(x, 0, z)
    }

    CADObject3D dyz(double y, double z = y) {
        return dxyz(0, y, z)
    }

    CADObject3D dxyz(double d) {
        return dxyz(d, d, d)
    }

    CADObject3D dxyz(double x, double y, double z) {
        fromTree(x, y, z) { asTree().copy() }
    }

    CADObject3D rx(double x) {
        rxyz(x, 0, 0)
    }

    CADObject3D ry(double y) {
        rxyz(0, y, 0)
    }

    CADObject3D rz(double z) {
        rxyz(0, 0, z)
    }

    CADObject3D rcx(double x) {
        rcxyz(x, 0, 0)
    }

    CADObject3D rcy(double y) {
        rcxyz(0, y, 0)
    }

    CADObject3D rcz(double z) {
        rcxyz(0, 0, z)
    }

    CADObject3D rxyz(double x, double y, double z) {
        def relative = AxisAngleSequence.createRelative(
                AxisSequence.XYZ,
                x * Math.PI / 180,
                y * Math.PI / 180,
                z * Math.PI / 180
        )
        def rotation = QuaternionRotation.fromAxisAngleSequence(relative)

        fromTree {
            def tree = asTree().copy()
            tree.transform(AffineTransformMatrix3D.identity().rotate(rotation))
            return tree
        }
    }

    CADObject3D rcxyz(double x, double y, double z) {
        def center = bounds().center()
        def relative = AxisAngleSequence.createRelative(
                AxisSequence.XYZ,
                x * Math.PI / 180,
                y * Math.PI / 180,
                z * Math.PI / 180
        )
        def rotation = QuaternionRotation.fromAxisAngleSequence(relative)

        fromTree {
            def tree = asTree().copy()
            tree.transform(AffineTransformMatrix3D.identity().translate(center.negate()).rotate(rotation).translate(center))
            return tree
        }
    }

    CADObject3D scaleX(double x) {
        return scale(x, 1, 1)
    }

    CADObject3D scaleY(double y) {
        return scale(1, y, 1)
    }

    CADObject3D scaleZ(double z) {
        return scale(1, 1, z)
    }

    CADObject3D scaleXY(double xy) {
        return scale(xy, xy, 1)
    }

    CADObject3D scaleXY(double x, double y) {
        return scale(x, y, 1)
    }

    CADObject3D scaleXZ(double xz) {
        return scale(xz, 1, xz)
    }

    CADObject3D scaleXZ(double x, double z) {
        return scale(x, 1, z)
    }

    CADObject3D scaleYZ(double yz) {
        return scale(1, yz, yz)
    }

    CADObject3D scaleYZ(double y, double z) {
        return scale(1, y, z)
    }

    CADObject3D scale(double xyz) {
        return scale(xyz, xyz, xyz)
    }

    CADObject3D scale(double x, double y, double z) {
        fromTree {
            def tree = asTree().copy()
            tree.transform(AffineTransformMatrix3D.createScale(x, y, z))
            return tree
        }
    }

    CADObject3D dxBy(double dx) {
        dxyzBy(dx, 0, 0)
    }

    CADObject3D dyBy(double dy) {
        dxyzBy(0, dy, 0)
    }

    CADObject3D dzBy(double dz) {
        dxyzBy(0, 0, dz)
    }

    CADObject3D dxyBy(double dx, double dy = dx) {
        dxyzBy(dx, dy, 0)
    }

    CADObject3D dxzBy(double dx, double dz = dx) {
        dxyzBy(dx, 0, dz)
    }

    CADObject3D dyzBy(double dy, double dz = dy) {
        dxyzBy(0, dy, dz)
    }

    CADObject3D dxyzBy(double d) {
        dxyzBy(d, d, d)
    }

    CADObject3D dxyzBy(double dx, double dy, double dz) {
        def tree = asTree().copy()
        def size = bounds().size()
        double x = size.x * dx
        double y = size.y * dy
        double z = size.z * dz
        fromTree(x, y, z) { tree }
    }

    CADObject3D center(boolean withX = true, boolean withY = true, boolean withZ = true) {
        def tree = asTree().copy()
        def size = bounds().center()
        fromTree(withX ? -size.x : 0, withY ? -size.y : 0, withZ ? -size.z : 0) { tree }
    }

    CADObject3D start(boolean withX = true, boolean withY = true, boolean withZ = true) {
        def tree = asTree().copy()
        def size = bounds().min
        fromTree(withX ? -size.x : 0, withY ? -size.y : 0, withZ ? -size.z : 0) { tree }
    }

    CADObject3D end(boolean withX = true, boolean withY = true, boolean withZ = true) {
        def tree = asTree().copy()
        def size = bounds().max
        fromTree(withX ? -size.x : 0, withY ? -size.y : 0, withZ ? -size.z : 0) { tree }
    }

    CADObject3D postProcess(Consumer<BSPTree> consumer) {
        postProcess = postProcess.andThen(consumer)
        return this
    }

    private <T extends CADCollection3D> T operation(T t, CADObject3D obj) {
        t.tap { add(this); add(obj) }
    }

    CADObject3D color(Color color) {
        BSPTree tree = new BSPTree(false)
        tree.copy(asTree())
        for (def node in tree.nodes()) {
            node.color = color
        }
        fromTree { tree }
    }

    Bounds bounds() {
        if (cachedBounds == null) {
            def b = asTree().getBounds()
            cachedBounds = new Bounds(b.min, b.max)
        }
        return cachedBounds
    }

    double getWidth() {
        return bounds().size().x
    }

    double getHeight() {
        return bounds().size().y
    }

    double getDepth() {
        return bounds().size().z
    }

    double getMinX() {
        return bounds().min.x
    }

    double getMinY() {
        return bounds().min.y
    }

    double getMinZ() {
        return bounds().min.z
    }

    double getMaxX() {
        return bounds().max.x
    }

    double getMaxY() {
        return bounds().max.y
    }

    double getMaxZ() {
        return bounds().max.z
    }

    double getCenterX() {
        return bounds().center().x
    }

    double getCenterY() {
        return bounds().center().y
    }

    double getCenterZ() {
        return bounds().center().z
    }

    BSPTree asTree() {
        if (cachedTree == null) {
            def tree
            try {
                tree = toTree()
            } catch (Throwable t) {
                t.addSuppressed(stack)
                throw t
            }
            if (tree == null) {
                throw new IllegalArgumentException(stack)
            }
            postProcess.accept(tree)
            cachedTree = tree
        }
        return cachedTree
    }
}