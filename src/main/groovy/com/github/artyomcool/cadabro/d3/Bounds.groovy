package com.github.artyomcool.cadabro.d3

import org.apache.commons.geometry.euclidean.threed.Vector3D

class Bounds {
    final Vector3D min
    final Vector3D max

    Bounds(Vector3D min, Vector3D max) {
        this.min = min
        this.max = max
    }

    Vector3D size() {
        max.add(-1, min)
    }

    Vector3D center() {
        Vector3D.of((max.x + min.x) / 2, (max.y + min.y) / 2, (max.z + min.z) / 2)
    }

}
