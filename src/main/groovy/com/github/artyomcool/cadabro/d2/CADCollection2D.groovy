package com.github.artyomcool.cadabro.d2

import com.github.artyomcool.cadabro.d3.BSPTree
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D

abstract class CADCollection2D extends CADObject2D {
    protected final List<CADObject2D> objects = new ArrayList<>()

    CADCollection2D getSelf() {
        return this
    }

    def <T extends CADObject2D> T add(T t) {
        objects.add(t)
        return t
    }

    void add(CADObject2D... t) {
        objects.addAll(t)
    }

    CADCollection2D getAt(CADObject2D obj) {
        add(obj)
        this
    }

    CADCollection2D leftShift(CADObject2D obj) {
        add(obj)
        this
    }

    CADCollection2D call(CADObject2D... obj) {
        add(obj)
        this
    }

    private <T extends CADObject2D> T apply(T a, Closure closure) {
        add a.tap((Closure<Void>) closure)
    }

    Union2D union(@DelegatesTo(Union2D.class) Closure closure) {
        apply(new Union2D(), closure)
    }

    @Override
    RegionBSPTree2D toTree() {
        if (objects.empty) {
            return new BSPTree(false)
        }

        if (objects.size() == 1) {
            return objects.get(0).asTree()
        }

        return toTree(objects)
    }

    abstract RegionBSPTree2D toTree(List<CADObject2D> objects);
}
