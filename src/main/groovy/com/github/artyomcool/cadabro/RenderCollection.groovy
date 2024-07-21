package com.github.artyomcool.cadabro


import com.github.artyomcool.cadabro.d3.CADObject3D

class RenderCollection {

    static class Render {
        boolean renderOnly, wiresOnly
        CADObject3D obj
    }

    List<Render> renders = []

    void add(CADObject3D obj,
               boolean renderOnly = false,
               boolean wiresOnly = false
    ) {
        new Render().tap {
            it.obj = obj; it.renderOnly = renderOnly; it.wiresOnly = wiresOnly
            renders << it
        }
    }

    void add(List<CADObject3D> objs,
               boolean renderOnly = false,
               boolean wiresOnly = false
    ) {
        for (def obj in objs) {
            add(obj, renderOnly, wiresOnly)
        }
    }

}
