package models.descent

import com.github.artyomcool.cadabro.RenderCollection
import com.github.artyomcool.cadabro.d2.CADObject2D
import com.github.artyomcool.cadabro.d2.Levels2D
import com.github.artyomcool.cadabro.d2.Union2D
import com.github.artyomcool.cadabro.d3.CADObject3D
import javafx.scene.paint.Color
import models.common.CardHolder
import models.dnd.Drizzt

import static com.github.artyomcool.cadabro.d2.CADObject2D.draw
import static com.github.artyomcool.cadabro.d2.CADObject2D.square
import static com.github.artyomcool.cadabro.d2.CADObject2D.text
import static com.github.artyomcool.cadabro.d3.CADObjects.*

class Descent {
    static double TOTAL_WIDTH = 300
    static double TOTAL_HEIGHT = 105

    final static double TILES_CORNER_TO_LOCK = 4
    final static double TILES_SQUARE_TO_LOCK = 7

    final static double BIG_SQUARE_NO_PAD = 102
    final static double BIG_SQUARE = 106
    final static double BIG_SQUARE_PAD = (BIG_SQUARE - BIG_SQUARE_NO_PAD) / 2
    final static double NANO_TILE_WIDTH = BIG_SQUARE_NO_PAD / 4
    final static double TILE_HEIGHT = 2.4

    static render() {
        def sq4x4 = extrude tilesHolderStroke(4, 4), TILE_HEIGHT * 4
        def sq4x2 = extrude tilesHolderStroke(2, 4), TILE_HEIGHT * 4
        def sq6x4 = extrude tilesHolderStroke(6, 4), TILE_HEIGHT * 4
        def sq6x2 = extrude tilesHolderStroke(6, 2), TILE_HEIGHT * 4
        def sq6x3 = extrude tilesHolderStroke(6, 3), TILE_HEIGHT * 4

        def tilesCuts = union(
                sq4x4,
                sq4x2.dx(sq4x4.width),
                sq6x2.dy(sq4x4.height),
                sq6x3.dy(sq4x4.height).dz(sq4x4.depth),
                sq6x4.dz(sq4x4.depth),
                sq6x4.end(true, false, false).dx(sq4x4.width + sq4x2.width).dz(sq4x4.depth),
        )

        def tilesCuts2 = tilesHolder2()



        new RenderCollection().tap {
            add(cube(TOTAL_WIDTH, TOTAL_WIDTH, TOTAL_HEIGHT), true, true)
            // add rcube(tilesCuts.width + 2, tilesCuts.height + 2, tilesCuts.depth + 1) - tilesCuts.dxyz(1)
            // add rcube(tilesCuts2.width + 2, tilesCuts2.height + 2, tilesCuts2.depth + 1) - tilesCuts2.dxyz(1)

            //add circleCounter(50, 24)
            //add circleCounter(25, 12)
            /*def cc = circleCounter(52, 0, true, true).extrude3d()
            def base = rcube(130, 220, cc.depth)
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 3; y++) {
                    base -= cc.start().centerY().dxy(
                            base.centerX + (cc.width - 0.2) * (x - 1),
                            base.height / 6 + base.height / 3 * y
                    ).dz(1).rcz(x * 180)
                }
            }
            add base -
                    cylinder(base.depth, 16).centerXY().dx(base.centerX).dy(-4) -
                    cylinder(base.depth, 16).centerXY().dx(base.centerX).dy(base.height + 4) -
                    cube(7, 12, base.depth - 2).centerXY().dx(base.centerX).dy(base.height / 3) -
                    cube(7, 12, base.depth - 2).centerXY().dx(base.centerX).dy(base.height / 3 * 2) -
                    cube(28, base.height, 1.2).centerX().dx(base.centerX).dz(base.depth - 1.2)*/

            /*def cc = circleCounter(27, 0, true, true).extrude3d()
            def base = rcube(180, 180, cc.depth)
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 5; y++) {
                    base -= cc.start().centerY().dxy(
                            base.width / 4 + (cc.width - 0.2) * (x % 2 - 1) + (base.width / 2) * (x >> 1),
                            base.height / 10 + base.height / 5 * y
                    ).dz(1).rcz((x + 1) * 180)
                }
            }
            for (int i = 0; i < 2; i++) {
                base = base -
                        cube(7, 12, base.depth - 2).centerXY().dx(base.centerX * i + base.centerX / 2).dy(base.height / 5) -
                        cube(7, 12, base.depth - 2).centerXY().dx(base.centerX * i + base.centerX / 2).dy(base.height / 5 * 4) -
                        cube(12, base.height, 1.2).centerX().dx(base.centerX * i + base.centerX / 2).dz(base.depth - 1.2)
            }
            add base -
                    cylinder(base.depth, 16).centerXY().dx(base.centerX).dy(-4) -
                    cylinder(base.depth, 16).centerXY().dx(base.centerX).dy(base.height + 4)*/
            // add circleCounter(50, 24, false, false, 74.0 / 50)
            // add circleCounter(25.8, 12, false, false, 50 / 25.8)

            /*def cc = circleCounter(51, 0, true, true, 74.0 / 50).extrude3d()
            def base = rcube(80, 170, cc.depth + 1)
            add base - cc.rcz(-90).startY().centerX().dx(base.centerX).dy(base.height / 2 - cc.width + 0.4).dz(1) -
                    cc.rcz(90).endY().centerX().dx(base.centerX).dy(base.height - (base.height / 2 - cc.width + 0.4)).dz(1) -
                    cube(base.width, 12, 1.2).centerXY().dxy(base.centerX, base.centerY).endZ().dz(base.maxZ) -
                    cylinder(base.depth, 8).centerXY().dy(base.centerY) -
                    cylinder(base.depth, 8).centerXY().dy(base.centerY).dx(base.width) -
                    cube(12, 7, base.depth - 2).centerXY().dy(base.centerY).dx(15) -
                    cube(12, 7, base.depth - 2).centerXY().dy(base.centerY).dx(base.width - 15)*/
            /*def cc = circleCounter(27, 0, true, true, 54 / 27).extrude3d()
            def base = rcube(80, 130, 10)
            for (int i = 0; i < 4; i++)
                base -= cc.centerXY().dxy(base.centerX, base.centerY / 4 + base.height / 4 * i).dz(1)
            add base - cube(16, base.height, 1.2).dx(4).endZ().dz(base.maxZ) -
                    cube(12, 7, base.depth - 2).centerY().dy(base.height / 4 * 1).dx(4) -
                    cube(12, 7, base.depth - 2).centerY().dy(base.height / 4 * 3).dx(4)*/
            /*add new CardHolder(65, 42, 19).tap {
                cutRadius = 7.5
                //floorThickness = 0.8
                borderThickness = 0.8
            }.render()*/

            /*def t = union()
                [[80, 130, 18, 0, 0], [25, 130, 18, 0, 0], [55 + 0.8, 25, 18, 25 - 0.8, 0],].each {
                    t.add new CardHolder(it[0], it[1], it[2]).tap { borderThickness = 0.8 }.adjustInnerToExternal().base().dxy(it[3], it[4])
                }
            add t*/

            /*def holder = tilesHolder3()
            add draw(0, 0)
                    .smooth(2)
                    .go(130)
                    .cw()
                    .smooth(2)
                    .go(holder.height + 0.8 * 2)
                    .cw()
                    .smooth(2)
                    .go(110)
                    .cw()
                    .smooth(2)
                    .go(20)
                    .ccw()
                    .smooth(2)
                    .go(20)
                    .cw()
                    .smooth(2)
                    .close()
                    .extrude(holder.depth + 0.8) - holder.centerX().dxy(130 / 2, 0.8).dz(0.8) -
                    cylinder(holder.depth + 0.8, 10).dx(5).dy(-10) -
                    cylinder(holder.depth + 0.8, 10).dx(130-25).dy(-10) -
                    cylinder(holder.depth + 0.8, 10).dx(130-25).dy(holder.height - 10)*/

            /*add new CardHolder(54, 42, 32).tap {
                //cutRadius = 7.5
                //floorThickness = 0.8
                borderThickness = 0.8
            }.withTeeth()
            add new CardHolder(54, 42, 12).tap {
                //cutRadius = 7.5
                //floorThickness = 0.8
                borderThickness = 0.8
            }.withTeeth()
            add new CardHolder(54, 42, 10).tap {
                //cutRadius = 7.5
                //floorThickness = 0.8
                borderThickness = 0.8
            }.withTeeth()
            add new CardHolder(54, 42, 12).tap {
                //cutRadius = 7.5
                //floorThickness = 0.8
                borderThickness = 0.8
            }.withTeeth()*/

            /*add new CardHolder(65, 42, 4).tap {
                cutRadius = 5
                //floorThickness = 0.8
                borderThickness = 0.8
            }.render()*/
            add new CardHolder(90, 59, 3.2).tap {
                borderThickness = 0.8
            }.render()
            add new CardHolder(90, 59, 2).tap {
                borderThickness = 0.8
            }.render()
            add new CardHolder(90, 59, 7.6).tap {
                borderThickness = 0.8
            }.render()
            add new CardHolder(90, 59, 7.6).tap {
                borderThickness = 0.8
            }.render()
        }
    }

    static CADObject2D tilesHolderStroke(int cw, int ch) {
        def drawer = draw(TILES_SQUARE_TO_LOCK, TILES_SQUARE_TO_LOCK)
        for (int i = 0; i < 2; i++) {
            drawer.go(TILES_CORNER_TO_LOCK).ccw()
                    .smooth(2).go(TILES_SQUARE_TO_LOCK).cw()
                    .smooth(4).go(NANO_TILE_WIDTH * cw + BIG_SQUARE_PAD - TILES_CORNER_TO_LOCK * 2).cw()
                    .smooth(4).go(TILES_SQUARE_TO_LOCK).ccw()
                    .smooth(2).go(TILES_CORNER_TO_LOCK).cw()
            drawer.go(TILES_CORNER_TO_LOCK).ccw()
                    .smooth(2).go(TILES_SQUARE_TO_LOCK).cw()
                    .smooth(4).go(NANO_TILE_WIDTH * ch + BIG_SQUARE_PAD - TILES_CORNER_TO_LOCK * 2).cw()
                    .smooth(4).go(TILES_SQUARE_TO_LOCK).ccw()
                    .smooth(2).go(TILES_CORNER_TO_LOCK).cw()
        }
        return drawer.close()
    }

    static CADObject3D tilesHolder3() {
        def s4x4 = square(55)
        def s2x1 = makeHolder("xx").dx(s4x4.width + 0.8)
        def s2x1x2 = makeHolder("xx").dx(s4x4.width + 0.8).dy(s2x1.height + 0.8)

        new Levels2D()
                .add(7 * TILE_HEIGHT, s4x4, s2x1, s2x1x2)
                .add(2 * TILE_HEIGHT, square(55, 60), square(55, 70).dx(55 + 0.8))
                .extrude()
    }

    static CADObject3D tilesHolder2() {
        def l0 = makeHolder(
                'oooooooo',
                'ooooxxxx',
                'xxooxxxx',
                'xxoooxxo',
                'xxxxoooo',
                'xxxxoooo',
        )
        def l1 = makeHolder(
                'oooooxxo',
                'ooooxxxx',
                'xxooxxxx',
                'xxoooxxo',
                'xxxxoooo',
                'xxxxoooo',
        )
        def l2 = makeHolder(
                'ooooxxoo',
                'oxxxxxxx',
                'xxxxxxxx',
                'xxxxxoxx',
                'xxxxxooo',
                'xxxxoooo',
        )
        def l3 = makeHolder(
                'xxxxxxxx',
                'xxxxxxxx',
                'xxxxxoxx',
                'oxxxxooo',
                'oxxxxooo',
                'ooxxxooo',
        )
        def l45 = makeHolder(
                'xxxxxxxx',
                'xxxxxxxx',
                'xxxxxxxx',
                'xxxxxxxx',
                'xxxxxxxx',
                'xxxxxxxx',
        )
        def l6 = makeHolder(
                'xxxx',
                'xxxx',
                'xxxx',
                'xxxx',
                'xxxx',
                'xxxx',
        )

        new Levels2D()
                .add(1 * TILE_HEIGHT, l0)
                .add(1 * TILE_HEIGHT, l1)
                .add(1 * TILE_HEIGHT, l2)
                .add(1 * TILE_HEIGHT, l3)
                .add(2 * TILE_HEIGHT, l45)
                .add(1 * TILE_HEIGHT, l6)
                .extrude()
    }

    static CADObject3D tilesHolder() {
        def s4x4 = makeHolder(
                'xxxx',
                'xxxx',
                'xxxx',
                'xxxx',
        )
        def s2x4 = makeHolder(
                'xx',
                'xx',
                'xx',
                'xx',
        ).dx(s4x4.width + 2)
        def s6x2 = makeHolder(
                'xxxxxx',
                'xxxxxx',
        ).dy(s4x4.height + 2)

        def n6x6a = makeHolder(
                'oxxxxo',
                'xxxxxx',
                'xxxxxx',
                'xxxxxx',
                'xxoooo',
                'xxoooo',
        )

        /*def n4x4a = makeHolder(
                'xxoo',
                'xxoo',
                'xxxx',
                'xxxx',
        )

        def n4x4b = makeHolder(
                'oxxo',
                'xxxx',
                'xxxx',
                'oxxo',
        ).dx(NANO_TILE_WIDTH * 3 + TILES_SQUARE_TO_LOCK)*/

        def s6x6= makeHolder(
                'xxxxxx',
                'xxxxxx',
                'xxxxxx',
                'xxxxxx',
                'xxxxxx',
                'xxxxxx',
        )

        new Levels2D()
                .add(4 * TILE_HEIGHT, s4x4, s2x4, s6x2)
                .add(1 * TILE_HEIGHT, n6x6a)
                .add(3 * TILE_HEIGHT, s6x6)
                .extrude()
    }

    static CADObject3D circleCounter(double d, int c, boolean connected = false, boolean simplified = false, double stretch = 1) {
        double magnetHoleH = 2.4
        double magnetHoleD = 6
        double tiniestWall = 0.4
        double smallLayerH = 0.6
        double holderH = 4
        double font = 3.5
        def bottom = union {
            add cylinder(smallLayerH * 2, d / 2).centerXY()
            add((cylinder(smallLayerH * 6, d / 2).centerXY() - cylinder(smallLayerH * 6, d / 2 - 5.6).centerXY()).dz(smallLayerH * 2))

            for (int i = 1; i <= c; i++) {
                def txt = text(i + "")
                add txt
                        .extrude(smallLayerH)
                        .scaleXY(font / txt.height)
                        .centerX()
                        .dy(d / 2 - 0.8)
                        .dz(smallLayerH * 8)
                        .rz(360 / c * i)
                        .color(Color.RED)
            }

            add cylinder(magnetHoleH + smallLayerH, magnetHoleD / 2 + tiniestWall).centerXY()

            //cylinder(magnetHoleH + smallLayerH, d / 2).center() - cylinder(magnetHoleH, d / 2 - font).center().dz(magnetHoleD)
        }
        if (!simplified) {
            bottom -= cylinder(magnetHoleH, magnetHoleD / 2).centerXY()
        }

        def top = union {
            def hold = (cylinder(holderH, d / 2 + tiniestWall + tiniestWall).centerXY()
                    - cylinder(holderH, d / 2 - tiniestWall * 1.5, d / 2).centerXY()
                    + cylinder(smallLayerH, d / 2 + tiniestWall + tiniestWall).dz(holderH).centerXY()
            ).scaleX(stretch).dx(-8 - (stretch - 1) * d / 2).color(Color.GREEN)
            add hold // for big

            /*def hold = (cylinder(holderH, d / 2 + tiniestWall + tiniestWall).centerXY()
                    - cylinder(holderH, d / 2 - tiniestWall * 0.5, d / 2 + tiniestWall * 0.5).centerXY()
                    + cylinder(smallLayerH, d / 2 + tiniestWall + tiniestWall).dz(holderH).centerXY()
            ).scaleX(stretch).dx(-8 - (stretch - 1) * d / 2).color(Color.GREEN)
            add hold // for small*/
            // add cylinder(bottom.depth + smallLayerH, 8).centerY().dx(hold.minX).dz(hold.depth)
            add cylinder(bottom.depth + smallLayerH, d / 2 + tiniestWall + tiniestWall).centerXY().scaleX(stretch).dx(-8 - (stretch - 1) * d / 2).dz(hold.depth) - cube(hold.width, hold.height / 2, bottom.depth + smallLayerH).centerY().scaleX(stretch).dx(-8 - (stretch - 1) * d / 2).dz(hold.depth)

            add cylinder(smallLayerH, d / 2).centerXY().dz(holderH).color(Color.YELLOW)
            add cylinder(magnetHoleH, magnetHoleD / 2 + tiniestWall).centerXY().dz(holderH + smallLayerH).color(Color.RED)
            add((cylinder(magnetHoleH * 2, d / 2 - 6.4).centerXY() - cylinder(magnetHoleH * 2, d / 2 - 6.4 - tiniestWall - tiniestWall).centerXY()).dz(holderH + smallLayerH).color(Color.CORAL))
        } - cylinder(magnetHoleH, magnetHoleD / 2).centerXY().dz(holderH) - cylinder(magnetHoleH * 2, 4.4).centerXY().dx(d / 2 - 2)

        if (connected) {
            return bottom + top.ry(180).dz(top.depth)
        }
        return top + bottom.dx(top.width + 10)
    }

    static CADObject2D makeHolder(String... s) {
        def u = new Union2D()
        for (int y = 0; y < Math.min(s.length, 6); y++) {
            for (int x = 0; x < Math.min(s[y].length(), 8); x++) {
                if (s[y][x] == 'x') {
                    def square = draw(x * NANO_TILE_WIDTH, y * NANO_TILE_WIDTH)
                            .go(NANO_TILE_WIDTH).cw()
                            .go(NANO_TILE_WIDTH).cw()
                            .go(NANO_TILE_WIDTH).cw()
                            .go(NANO_TILE_WIDTH).cw()
                            .close()

                    u.add(square)
                }
            }
        }

        return u.offset(TILES_SQUARE_TO_LOCK).dxy(TILES_SQUARE_TO_LOCK, TILES_SQUARE_TO_LOCK)
    }
}
