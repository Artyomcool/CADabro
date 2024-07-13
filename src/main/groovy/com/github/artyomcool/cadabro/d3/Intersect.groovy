package com.github.artyomcool.cadabro.d3

class Intersect extends CADCollection3D {
    @Override
    BSPTree toTree(List<CADObject3D> objects) {
        def tree = objects.get(0).asTree().copy()
        def union = objects.get(1).asTree().copy()

        for (def n in objects.subList(2, objects.size())) {
            union.union(n.asTree())
        }

        return tree.tap { intersection(union) }
    }
}
