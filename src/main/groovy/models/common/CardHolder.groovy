package models.common


import com.github.artyomcool.cadabro.d3.CADObject3D
import javafx.scene.paint.Color

import static com.github.artyomcool.cadabro.d2.CADObject2D.text
import static com.github.artyomcool.cadabro.d3.CADObjects.*

class CardHolder {

    static final double WALL = 1.6

    double innerWidth
    double innerHeight
    double innerDepth

    double innerRadius = 2
    double borderThickness = WALL
    int captionFontSize = 8

    Double floorThickness = null
    Double outerRadius = null
    Double cutRadius = null
    Double cornerCutRadius = null
    String caption = null
    String captionLeft = null
    String captionRight = null
    Double teethHeight = null

    Closure<CADObject3D> addToBase = null

    CardHolder(double innerWidth, double innerHeight, double innerDepth) {
        this.innerWidth = innerWidth
        this.innerHeight = innerHeight
        this.innerDepth = innerDepth
    }

    double getInnerR() {
        innerRadius
    }

    double getWall() {
        borderThickness
    }

    double getFloor() {
        floorThickness == null ? wall : floorThickness
    }

    double getOuterR() {
        outerRadius == null ? innerRadius + wall : outerRadius
    }

    double getCutR() {
        cutRadius == null ? Math.min(innerWidth, innerHeight) / 4 : cutRadius
    }

    double getCornerCutR() {
        cornerCutRadius == null ? Math.min(outerR, innerDepth) : cornerCutRadius
    }

    double getTeethHeight() {
        return teethHeight == null ? 2 : teethHeight
    }

    double getTotalWidth() {
        wall + innerWidth + wall
    }

    double getTotalHeight() {
        wall + innerHeight + wall
    }

    double getTotalDepth() {
        innerDepth + floor
    }

    CADObject3D render() {
        withTeeth() - cuts()
    }

    CADObject3D renderThin(double thinWall = 0.8, boolean withCut = true) {
        def r = withTeeth() - cutsThin(thinWall) + title(wall - thinWall)
        if (!withCut) {
            return r
        }
        return r - makeCut(cutR, cornerCutR, wall, totalDepth).dx(totalWidth / 2)
    }

    CADObject3D base() {
        def r = rcube(totalWidth, totalHeight, totalDepth, outerR) -
                rcube(innerWidth, innerHeight, innerDepth, innerR).dxyz(wall, wall, floor)
        if (addToBase) {
            r += addToBase()
        }
        return r
    }

    CADObject3D withTeeth() {
        withTeeth(base(), 5, getTeethHeight(), wall, wall * 2 + innerR)
    }

    CADObject3D withTeethAndText() {
        withTeeth() - title()
    }

    CADObject3D withCut() {
        base() - cuts()
    }

    CADObject3D cuts() {
        makeCut(cutR, cornerCutR, wall, totalDepth).dx(totalWidth / 2) + title()
    }

    CADObject3D cutsThin(double thinWall) {
        makeThinCuts(totalWidth, totalHeight, totalDepth, wall, thinWall)
    }

    CardHolder adjustInnerToExternal() {
        innerWidth -= totalWidth - innerWidth
        innerHeight -= totalHeight - innerHeight
        innerDepth -= totalDepth - innerDepth

        return this
    }

    static CADObject3D makeCut(double cutR, double cornerCutR, double wall, double totalDepth) {
        def cut = rcube(cutR * 2, wall + cutR, totalDepth, cutR, false, false)
                .dxBy(-0.5)

        def bounds = cut.bounds()

        cut +
                rcut(cornerCutR, wall * 4)
                        .rx(90)
                        .dxyzBy(-1, 1, -1)
                        .dxz(bounds.min.x, bounds.max.z) +
                rcut(cornerCutR, wall * 4)
                        .rx(90)
                        .rz(180)
                        .dxyzBy(1, 0, -1)
                        .dxz(bounds.max.x, bounds.max.z)
    }

    CADObject3D title(double depth = Math.min(wall - 1, 2)) {
        def r = union()

        if (caption != null) r += extrude(text(caption, null, captionFontSize), depth)
                .color(Color.GREENYELLOW)
                .center()
                .rx(-90)
                .dyBy(-0.5)
                .dxyz(totalWidth / 2, totalHeight, totalDepth / 2)

        if (captionLeft != null) r += extrude(text(captionLeft, null, captionFontSize), depth)
                .color(Color.GREENYELLOW)
                .center()
                .rx(-90)
                .rz(-90)
                .dxBy(-0.5)
                .dxyz(totalWidth, totalHeight / 2, totalDepth / 2)

        if (captionRight != null) r += extrude(text(captionRight, null, captionFontSize), depth)
                .color(Color.GREENYELLOW)
                .center()
                .rx(-90)
                .rz(90)
                .dxBy(0.5)
                .dxyz(0, totalHeight / 2, totalDepth / 2)

        return r
    }

    static CADObject3D makeThinCuts(double totalWidth, double totalHeight, double totalDepth, double wall, double thinWall) {
        double r = 2
        double wallDiff = wall - thinWall
        CADObject3D cut = rrcube(totalWidth - r * 4, totalHeight - r * 4, totalDepth - r * 4)
        return cut.endX().dxyz(wallDiff, r, r) +
                cut.dxyz(totalWidth - wallDiff, r, r) +
                cut.endY().dxyz(r, wallDiff, r) +
                cut.dxyz(r, totalHeight - wallDiff, r)

    }

    static CADObject3D withTeeth(CADObject3D obj, double tw, double th, double t, double d) {
        new Teeth().tap {
            teethWidth = tw
            teethHeight = th
            thickness = t
            delta = d
        }.renderAll(obj)
    }
}
