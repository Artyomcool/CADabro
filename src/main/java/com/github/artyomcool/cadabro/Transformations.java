package com.github.artyomcool.cadabro;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

import java.util.function.Function;

public class Transformations {

    public static <P extends Point<P>> Transform<P> simple(Function<P, P> func) {
        return new Transform<>() {
            @Override
            public Transform<P> inverse() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean preservesOrientation() {
                return true;
            }

            @Override
            public P apply(P vector3D) {
                return func.apply(vector3D);
            }
        };
    }

    public static Transform<Vector2D> t2d(Function<Vector2D, Vector2D> func) {
        return simple(func);
    }

    public static Transform<Vector3D> t3d(Function<Vector3D, Vector3D> func) {
        return simple(func);
    }


}
