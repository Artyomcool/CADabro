package com.github.artyomcool.cadabro.d3

class Diff extends CADCollection3D {
    @Override
    BSPTree toTree(List<CADObject3D> objects) {
        def tree = objects.get(0).asTree().copy();

        for (def n in objects.subList(1, objects.size())) {
            tree.difference(n.asTree())
        }

        return tree
    }
}
