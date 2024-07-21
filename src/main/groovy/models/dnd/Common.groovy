package models.dnd

import com.github.artyomcool.cadabro.d2.CADObject2D
import com.github.artyomcool.cadabro.d3.CADObject3D
import javafx.scene.paint.Color
import models.common.CardHolder

import static com.github.artyomcool.cadabro.d2.CADObject2D.draw
import static com.github.artyomcool.cadabro.d3.CADObjects.*

class Common {

    final static double TILES_CORNER_TO_LOCK = 25
    final static double TILES_SQUARE_TO_LOCK = 7

    final static int BIG_SQUARE_NO_PAD = 102
    final static int BIG_SQUARE = 106
    final static int SMALL_SQUARE = 34
    final static int SMALL_CIRCLE = 28

    static CADObject3D bigSquareHolder(double depth, boolean simplify = false) {
        if (simplify) {
            return cube(
                    CardHolder.WALL + BIG_SQUARE + CardHolder.WALL,
                    CardHolder.WALL + BIG_SQUARE + CardHolder.WALL,
                    CardHolder.WALL + depth
            ).color(Color.color(1, 0.5, 0.3, 0.75))
        }
        return new CardHolder(BIG_SQUARE, BIG_SQUARE, depth).withCut().color(Color.color(1, 0.5, 0.3))
    }

    static CADObject3D smallCircleHolder(int count, double depth, boolean simplify = false) {
        if (simplify) {
            return cube(
                    CardHolder.WALL + (SMALL_CIRCLE + CardHolder.WALL) * count,
                    CardHolder.WALL + (SMALL_CIRCLE + CardHolder.WALL) * 1,
                    CardHolder.WALL + depth
            )
        }

        def holder = new CardHolder(SMALL_CIRCLE, SMALL_CIRCLE, depth).tap {
            innerRadius = 7
            outerRadius = 0
            cornerCutRadius = 4
        }

        def result = union()
        count.times {
            result << holder.withCut().dx(it * (SMALL_CIRCLE + holder.wall))
        }

        return result & rcube(result.width, result.height, result.depth)
    }

    static CADObject3D smallSquaresHolder(int xcount, int ycount, double depth, boolean simplify = false) {
        if (simplify) {
            return cube(
                    CardHolder.WALL + (SMALL_SQUARE + CardHolder.WALL) * xcount,
                    CardHolder.WALL + (SMALL_SQUARE + CardHolder.WALL) * ycount,
                    CardHolder.WALL + depth
            )
        }
        def holder = new CardHolder(SMALL_SQUARE, SMALL_SQUARE, depth).tap {
            innerRadius = 0
            outerRadius = 0
            cornerCutRadius = 4
        }

        def result = union()
        xcount.times {
            result << holder.withCut().dx(it * (SMALL_SQUARE + holder.wall))
        }

        if (ycount > 1) {
            result += result.rcz(180).dy(SMALL_SQUARE + holder.wall)
        }

        result & rcube(result.width, result.height, result.depth)
    }

    static CADObject3D tilesHolder(double depth, boolean cut, boolean simplify = false) {
        double wall = 2
        double floor = 4
        double totalWidth = wall + TILES_SQUARE_TO_LOCK + BIG_SQUARE + TILES_SQUARE_TO_LOCK + wall

        if (simplify) {
            return cube(totalWidth, totalWidth, depth + floor).color(Color.color(0.7, 0.2, 0.7, 0.75))
        }

        def stroke = tilesHolderStroke2(2)

        def r = (extrude(stroke, depth).dz(floor) + rcube(totalWidth, totalWidth, floor, 4)).color(Color.color(0.7, 0.2, 0.7))
        return cut ? (r - cutBottom()) : r
    }

    static CADObject3D cutBottom() {
        double wall = 2
        double floor = 4
        double r = 4
        double totalWidth = TILES_SQUARE_TO_LOCK + BIG_SQUARE + TILES_SQUARE_TO_LOCK
        cube(totalWidth, totalWidth, floor).dz(floor).dxy(wall, -(BIG_SQUARE - TILES_CORNER_TO_LOCK) + r) +
                cube(totalWidth, 8, 8).center().rx(45).dxz(wall + totalWidth / 2, floor).color(Color.GREEN) +
                cylinder(floor, 16, 20).center().dxz(wall + totalWidth / 2, floor / 2)
    }

    static CADObject2D bigTilesHolderStroke() {
        CADObject2D.fromTree tilesHolderStroke().asTree().tap { it.union(tilesHolderStroke(BIG_SQUARE_NO_PAD).asTree()) }
    }

    static CADObject2D tilesHolderStroke(double sx = 0, double sy = 0) {
        def drawer = draw(sx + TILES_SQUARE_TO_LOCK, sy + TILES_SQUARE_TO_LOCK)
        for (int i = 0; i < 4; i++) {
            drawer.go(TILES_CORNER_TO_LOCK).ccw()
                    .smooth(4).go(TILES_SQUARE_TO_LOCK).cw()
                    .smooth(4).go(BIG_SQUARE - TILES_CORNER_TO_LOCK * 2).cw()
                    .smooth(4).go(TILES_SQUARE_TO_LOCK).ccw()
                    .smooth(4).go(TILES_CORNER_TO_LOCK).cw()
        }
        return drawer.close()
    }

    static CADObject2D tilesHolderStroke2(double wall) {
        // TODO reuse tilesHolderStroke
        double totalWidth = wall + TILES_SQUARE_TO_LOCK + BIG_SQUARE + TILES_SQUARE_TO_LOCK + wall
        return draw((TILES_CORNER_TO_LOCK + TILES_SQUARE_TO_LOCK + wall), 0)
                .smooth(4).dx(-(TILES_CORNER_TO_LOCK + TILES_SQUARE_TO_LOCK + wall))
                .smooth(4).dy(totalWidth)
                .smooth(4).dx(totalWidth)
                .smooth(4).dy(-totalWidth)
                .smooth(4).dx(-(TILES_CORNER_TO_LOCK + TILES_SQUARE_TO_LOCK + wall))
                .smooth(4).dy(wall + TILES_SQUARE_TO_LOCK)
                .smooth(4).dx(TILES_CORNER_TO_LOCK)
                .smooth(4).dy(TILES_CORNER_TO_LOCK)
                .smooth(4).dx(TILES_SQUARE_TO_LOCK)
                .smooth(4).dy(BIG_SQUARE - TILES_CORNER_TO_LOCK * 2)
                .smooth(4).dx(-TILES_SQUARE_TO_LOCK)
                .smooth(4).dy(TILES_CORNER_TO_LOCK)
                .smooth(4).dx(-TILES_CORNER_TO_LOCK)
                .smooth(4).dy(TILES_SQUARE_TO_LOCK)
                .smooth(4).dx(-(BIG_SQUARE - TILES_CORNER_TO_LOCK * 2))
                .smooth(4).dy(-TILES_SQUARE_TO_LOCK)
                .smooth(4).dx(-TILES_CORNER_TO_LOCK)
                .smooth(4).dy(-TILES_CORNER_TO_LOCK)
                .smooth(4).dx(-TILES_SQUARE_TO_LOCK)
                .smooth(4).dy(-(BIG_SQUARE - TILES_CORNER_TO_LOCK * 2))
                .smooth(4).dx(TILES_SQUARE_TO_LOCK)
                .smooth(4).dy(-TILES_CORNER_TO_LOCK)
                .smooth(4).dx(TILES_CORNER_TO_LOCK)
                .smooth(4).close(true)
    }

}
