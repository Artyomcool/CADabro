package com.github.artyomcool.cadabro.d2

import com.github.artyomcool.cadabro.d3.CADObject3D
import com.github.artyomcool.cadabro.d3.Union

class Levels2D {

    private static class Level {
        CADObject2D obj;
        double depth;
    }

    private final List<Level> levels = []

    Levels2D add(double depth, CADObject2D... objs) {
        Level l = new Level()
        l.obj = objs.length == 1 ? objs[0] : new Union2D().tap { add(objs) }
        l.depth = depth
        levels.add(l)
        return this
    }

    CADObject3D extrude() {
        double totalDepth = levels.sum { it.depth }
        double e = 0
        def union = new Union()
        for (def l in levels.reversed()) {
            e += l.depth
            union.add l.obj.extrude(e).dz(totalDepth - e)
        }
        return union
    }

}
