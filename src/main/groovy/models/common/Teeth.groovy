package models.common

import com.github.artyomcool.cadabro.d3.CADObject3D

import static com.github.artyomcool.cadabro.d3.CADObjects.cube
import static com.github.artyomcool.cadabro.d3.CADObjects.cylinder
import static com.github.artyomcool.cadabro.d3.CADObjects.diff
import static com.github.artyomcool.cadabro.d3.CADObjects.hull
import static com.github.artyomcool.cadabro.d3.CADObjects.sphere
import static com.github.artyomcool.cadabro.d3.CADObjects.union

class Teeth {

    double teethWidth = 4
    double teethHeight = 2
    double thickness = 1.6
    double delta = 1.6

    static CADObject3D tooth(double width, double height, double thickness) {
        double r = thickness

        def s = sphere(r).dz(height - r * 2)
        def part = r >= height ? s : s + cylinder(height - r, r)
        part = part.dy(-r) & cube(r * 2, r, height)
        hull(part, part.dx(width - r * 2))
    }

    CADObject3D renderTop(CADObject3D obj) {
        render(obj, true, false)
    }

    CADObject3D renderBottom(CADObject3D obj) {
        render(obj, false, true)
    }

    CADObject3D renderAll(CADObject3D obj) {
        render(obj, true, true)
    }

    CADObject3D render(CADObject3D obj, boolean renderTop, boolean renderBottom) {
        double cut = 0.4
        double shift = 0.2 + cut
        def bounds = obj.bounds()

        List<CADObject3D> teeth = []
        List<CADObject3D> teethCut = []

        def add = (double x, double y, double rotation) -> {
            if (renderTop) teeth << tooth(teethWidth, teethHeight, thickness).rz(rotation).dxyz(x, y, bounds.max.z)
            if (renderBottom) teethCut << tooth(cut + teethWidth + cut, teethHeight + cut, thickness).dx(-cut).rz(rotation).dxyz(x, y, bounds.min.z)
        }

        add(bounds.min.x + delta, bounds.min.y, 0)
        add(bounds.max.x - delta - teethWidth, bounds.min.y, 0)
        add(bounds.min.x + delta + teethWidth, bounds.max.y, 180)
        add(bounds.max.x - delta, bounds.max.y, 180)


        add(bounds.min.x, bounds.min.y + delta + teethWidth, -90)
        add(bounds.min.x, bounds.max.y - delta, -90)
        add(bounds.max.x, bounds.min.y + delta, 90)
        add(bounds.max.x, bounds.max.y - delta - teethWidth, 90)

        def u = union(obj)
        teeth.each { u it }

        def d = diff(u)
        teethCut.each { d it }

        return d
    }
}
