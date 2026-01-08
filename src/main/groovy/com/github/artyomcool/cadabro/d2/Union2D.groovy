package com.github.artyomcool.cadabro.d2

import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D

class Union2D extends CADCollection2D {
    @Override
    RegionBSPTree2D toTree(List<CADObject2D> objects) {
        def tree = objects.get(0).asTree().copy()

        for (def n in objects.subList(1, objects.size())) {
            def t2 = n.asTree()
            tree.union(t2)
        }

        return tree
    }

    static Union2D plus(CADObject2D one, CADObject2D two) {
        return new Union2D().tap { add(one); add(two) }
    }

}
