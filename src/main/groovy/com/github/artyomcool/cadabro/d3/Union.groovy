package com.github.artyomcool.cadabro.d3

class Union extends CADCollection3D {
    @Override
    BSPTree toTree(List<CADObject3D> objects) {
        BSPTree result = new BSPTree(false)

        for (def n in objects) {
            result.union(n.asTree())
        }

        return result
    }
}
