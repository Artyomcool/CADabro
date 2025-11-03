package models.carcassonne

import com.github.artyomcool.cadabro.d3.CADObject3D

import static com.github.artyomcool.cadabro.d3.CADObjects.*

class Tower {

    double internalDepth = 130
    double tileWidth = 45
    double internalSpacing = 1

    double wall = 0.8

    double sideCut = 16
    double tileDepth = 1.8
    double bottomCutDepth = tileDepth * 1.5

    double bottomSpacing = 15

    double getInternalWidth() {
        tileWidth + internalSpacing
    }

    double getExternalWidth() {
        wall + internalWidth + wall
    }

    CADObject3D internalBase(double depth = internalDepth + bottomCutDepth) {
        def external = rcube(externalWidth, externalWidth, depth, 2)
        def internal = rcube(internalWidth, internalWidth, depth, 2)

        def base = external - internal.centerXY().dxy(external.centerX, external.centerY)

        return base
    }

    CADObject3D baseWithCut() {
        def base = internalBase()
        base -
                rcut(4, wall)
                        .rx(90)
                        .rcy(180)
                        .dy(wall)
                        .dx(base.centerX + sideCut / 2) -
                rcut(4, wall)
                        .rx(90)
                        .rcy(90)
                        .dy(wall)
                        .dxBy(-1)
                        .dx(base.centerX - sideCut / 2) -
                cube(sideCut, wall, base.depth - 4)
                        .dx(base.centerX - sideCut / 2)/* -
                rcut(4, wall)
                        .rx(90)
                        .rcy(-90)
                        .dy(wall)
                        .dzBy(-1)
                        .dx(base.centerX + sideCut / 2)
                        .dz(base.depth) -
                rcut(4, wall)
                        .rx(90)
                        .dy(wall)
                        .dxBy(-1)
                        .dzBy(-1)
                        .dx(base.centerX - sideCut / 2)
                        .dz(base.depth)*/
    }

    CADObject3D baseWithWindows() {
        baseWithCut()
    }

    CADObject3D baseWithBottomCut() {
        def base = baseWithWindows()
        base -
                cube(base.width, Math.max(wall, 2), bottomCutDepth)
                        .dyzBy(-1)
                        .dyz(base.height, base.depth) +
                rcut(sideCut / 2, wall).dx(base.centerX).rx(90).dy(wall).dzBy(-1).dz(base.depth - 4) +
                rcut(sideCut / 2, wall).dxBy(-1).dx(base.centerX).rx(90).rcy(-90).dy(wall).dzBy(-1).dz(base.depth - 4)
    }

    CADObject3D fix() {
        double fixExtraLength = 2
        double width = 4
        def fix = cube(width, wall, wall + fixExtraLength) + cylinder(width, wall).ry(90).dzBy(0.5)
        fix = fix.scaleZ(-1)
        fix.dz(-fix.minZ)
    }

    CADObject3D baseWithFixesAndCorrector() {
        def base = baseWithBottomCut()
        def delta = 12
        def fixLine = (fix().dx(delta) + fix().dxBy(-1).dx(base.width - delta)).dz(base.depth)
        def w = internalWidth
        def corrector = rcut(4, w).ry(90)
        base +
                corrector.rcx(180).dxy(wall).dz(base.depth) +
                fixLine +
                fixLine
                        .rcz(90)
                        .dxBy(-0.5)
                        .dxy(base.centerX, base.centerY) +
                fixLine
                        .rcz(-90)
                        .dxBy(0.5)
                        .dxy(-base.centerX, base.centerY)
    }

    CADObject3D bottom() {
        def external = rcube(externalWidth, externalWidth, bottomSpacing, 2)
        external - cylinder(bottomSpacing, externalWidth / 3).centerXY().dx(external.centerX) -
                baseWithFixesAndCorrector().scaleZ(-1).dzBy(1).dz(bottomSpacing - fix().depth).rcz(180).dx(0.2) -
                baseWithFixesAndCorrector().scaleZ(-1).dzBy(1).dz(bottomSpacing - fix().depth).rcz(180).dx(-0.2) -
                baseWithFixesAndCorrector().scaleZ(-1).dzBy(1).dz(bottomSpacing - fix().depth).rcz(180).dy(0.2) -
                baseWithFixesAndCorrector().scaleZ(-1).dzBy(1).dz(bottomSpacing - fix().depth).rcz(180).dy(-0.2)
    }

    CADObject3D render() {
        baseWithFixesAndCorrector()
    }
}
