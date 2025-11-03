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

    Double floorThickness = null
    Double outerRadius = null
    Double cutRadius = null
    Double cornerCutRadius = null
    String caption = null
    Double captionFontSize = null
    Double teethHeight = null

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

    double getCaptionFontHeight() {
        captionFontSize == null ? totalDepth / 2 : captionFontSize
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

    CADObject3D base() {
        rcube(totalWidth, totalHeight, totalDepth, outerR) -
                rcube(innerWidth, innerHeight, innerDepth, innerR).dxyz(wall, wall, floor)
    }

    CADObject3D withTeeth() {
        withTeeth(base(), 5, getTeethHeight(), wall, wall * 2 + innerR)
    }

    CADObject3D withCut() {
        base() - cuts()
    }

    CADObject3D cuts() {
        makeCut(cutR, cornerCutR, wall, totalDepth).dx(totalWidth / 2) + title()
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

    CADObject3D title() {
        if (caption == null) {
            return union()
        }
        extrude(text(caption, null), Math.min(wall - 1, 2)).color(Color.GREENYELLOW).center()
                .rx(-90)
                .rz(180)
                .dyBy(-0.5)
                .dxyz(totalWidth / 2, totalHeight, totalDepth / 2)
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
