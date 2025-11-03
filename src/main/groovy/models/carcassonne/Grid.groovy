package models.carcassonne

import com.github.artyomcool.cadabro.d2.CADObject2D
import com.github.artyomcool.cadabro.d3.CADObject3D
import javafx.scene.paint.Color

import static com.github.artyomcool.cadabro.d2.CADObject2D.*
import static com.github.artyomcool.cadabro.d2.CADObject2D.*
import static com.github.artyomcool.cadabro.d3.CADObjects.*

class Grid {
    double tileWidth = 45
    double internalSpacing = 0.4
    double wall = 0.4
    double bottom = 0.6
    double wallHeight = 0.6
    double bottomCutWidth = 36

    double getTileExternalWidth() {
        wall + tileWidth + internalSpacing + wall
    }

    CADObject3D tile() {
        def cut = (
                (cube(wall + 2, wall + 2, wallHeight) - cylinder(wallHeight, 2).dxy(wall)) &
                        rcube(wall + 4, wall + 4, wallHeight, 1)
        ).dz(bottom)
        rcube(tileExternalWidth, tileExternalWidth, bottom, 1) - rcube(bottomCutWidth, bottomCutWidth, bottom)
                .centerXY()
                .dxy(tileExternalWidth / 2) +
                (cut +
                        cut.rcz(90).dxBy(-1).dx(tileExternalWidth) +
                        cut.rcz(180).dxyBy(-1).dxy(tileExternalWidth) +
                        cut.rcz(-90).dyBy(-1).dy(tileExternalWidth) +
                        cube(tileExternalWidth - 4, wall, wallHeight).dz(bottom).dx(2) +
                        cube(wall, tileExternalWidth - 4, wallHeight).dz(bottom).dy(2) +
                        cube(tileExternalWidth - 4, wall, wallHeight).dz(bottom).dx(2).dyBy(-1).dy(tileExternalWidth) +
                        cube(wall, tileExternalWidth - 4, wallHeight).dz(bottom).dy(2).dxBy(-1).dx(tileExternalWidth)
                ).color(Color.GREENYELLOW)
    }

    CADObject3D tilesWithConnector() {
        def u = union()
        def c = 5
        for (int i = 0; i < c; i++) {
            for (int j = 0; j < c; j++) {
                u.add tile().dxy(tileExternalWidth * i, tileExternalWidth * j)
            }
        }
        double uw = tileExternalWidth * c
        for (int a = 0; a < 360; a += 90) {
            for (int i = 0; i < c; i+=Math.max(1, c-1)) {
                u.add connector().centerY().dx(uw).dy(12 + tileExternalWidth * i).dxy(-uw / 2, -uw / 2).rz(a).dxy(uw / 2, uw / 2)
            }
        }
        u.add (rcube(uw, uw, bottom) - rcube(uw - 14, uw - 14, bottom).centerXY().dxy(uw / 2, uw / 2))
        def r = diff(u)
        for (int a = 0; a < 360; a += 90) {
            for (int i = 0; i < c; i+=Math.max(1, c-1)) {
                def cc = connector().centerY().scaleZ(10)
                cc = cc.dx(0.4) + cc.dx(-0.4)
                cc = cc.dy(0.4) + cc.dy(-0.4)
                r.add cc.rz(180).dx(uw).dy(-12 + tileExternalWidth * (i + 1)).dxy(-uw / 2, -uw / 2).rz(a).dxy(uw / 2, uw / 2)
            }
        }
        r
    }

    CADObject3D connector() {
        def connector = draw().dx(2)
                .smooth(1).dy(4)
                .smooth(1).dx(3)
                .smooth(1).dy(-4 - 6 - 4)
                .smooth(1).dx(-3)
                .smooth(1).dy(4)
                .smooth(1).dx(-2)
                .close(true)
                .extrude(bottom)

        connector.dxy(-connector.minX, -connector.minY)
    }

    CADObject3D render() {
        tilesWithConnector()
    }
}
