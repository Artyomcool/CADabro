package models.common

import com.github.artyomcool.cadabro.d2.CADObject2D
import com.github.artyomcool.cadabro.d3.CADObject3D

class Outline {

    CADObject2D outline2d
    CADObject2D inline2d
    CADObject3D outline
    CADObject3D inline
    CADObject3D wall

    static Outline draw(
            double wall,
            double height,
            @DelegatesTo(value = DelegatesTo.Target.class, type = "com.github.artyomcool.cadabro.d2.CADObject2D.Drawer")
                    Closure<CADObject2D> outline
    ) {
        def outline2d = CADObject2D.draw().with(outline)
        def inline2d = outline2d.offset(-wall)
        return new Outline(outline2d, inline2d, height)
    }

    Outline(CADObject2D outline2d, CADObject2D inline2d, double height) {
        this.outline2d = outline2d
        this.inline2d = inline2d
        this.outline = outline2d.extrude(height)
        this.inline = inline2d.extrude(height)
        this.wall = outline - inline
    }
}
