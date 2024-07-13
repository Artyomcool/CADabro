package com.github.artyomcool.cadabro.d3

abstract class CADCollection3D extends CADObject3D {

    protected final List<CADObject3D> objects = new ArrayList<>()

    CADCollection3D getSelf() {
        return this
    }

    def <T extends CADObject3D> T add(T t) {
        objects.add(t)
        return t
    }

    void add(CADObject3D... t) {
        objects.addAll(t)
    }

    CADCollection3D getAt(CADObject3D obj) {
        add(obj)
        this
    }

    CADCollection3D leftShift(CADObject3D obj) {
        add(obj)
        this
    }

    CADCollection3D call(CADObject3D... obj) {
        add(obj)
        this
    }

    private <T extends CADObject3D> T apply(T a, Closure closure) {
        add a.tap((Closure<Void>) closure)
    }

    Union union(@DelegatesTo(Union.class) Closure closure) {
        apply(new Union(), closure)
    }

    Diff diff(@DelegatesTo(Diff.class) Closure closure) {
        apply(new Diff(), closure)
    }

    Intersect intersect(@DelegatesTo(Intersect.class) Closure closure) {
        apply(new Intersect(), closure)
    }

    Hull hull(@DelegatesTo(Hull.class) Closure closure) {
        apply(new Hull(), closure)
    }

    @Override
    BSPTree toTree() {
        if (objects.empty) {
            return new BSPTree(false)
        }

        if (objects.size() == 1) {
            return objects.get(0).asTree()
        }

        return toTree(objects)
    }

    abstract BSPTree toTree(List<CADObject3D> objects);

}
