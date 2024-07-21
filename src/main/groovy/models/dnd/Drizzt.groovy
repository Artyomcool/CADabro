package models.dnd

import com.github.artyomcool.cadabro.MiniHolder
import com.github.artyomcool.cadabro.RenderCollection
import com.github.artyomcool.cadabro.d3.CADObject3D
import javafx.scene.paint.Color
import models.common.CardHolder
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D

import static com.github.artyomcool.cadabro.d2.CADObject2D.draw
import static com.github.artyomcool.cadabro.d2.CADObject2D.fromTree
import static com.github.artyomcool.cadabro.d3.CADObjects.*
import static models.dnd.Common.*

class Drizzt {

    static double TOTAL_WIDTH = 304

    static render() {
        new RenderCollection().tap {
            add(cube(TOTAL_WIDTH, TOTAL_WIDTH, 115), true, true)

            /*def cardHolders = cardHolders(true)
            add cardHolders

            def tilesHolder = tilesHolder(66, true, true).dy(cardHolders[0].maxY + 0.4)
            add tilesHolder

            def bigSquares = bigSquareHolder(25, true).dy(tilesHolder.maxY + 0.4)
            add bigSquares

            def bigTilesHolder = bigTilesHolder(true).rz(90).dxBy(1).dyz(cardHolders[0].maxY + 0.4, tilesHolder.maxZ + 0.4)
            add bigTilesHolder

            def smallSquares = smallSquaresHolder(3, 2, 18).dxyz(bigSquares.minX, bigSquares.minY, bigSquares.maxZ + 0.4)
            add smallSquares

            def circleTokens = smallCircleHolder(4, 18)
            add circleTokens

            def edgeTiles = edgeTiles()
            add edgeTiles

            add miniHolderBig() */

            add miniHolderSmall2()
        }
    }

    static cardHolders(boolean simplify = false) {
        def holder = new CardHolder(89.6, 64.4, 3)
        double z = 0
        holder.borderThickness = 1.6
        holder.floorThickness = 0.8
        List<CADObject3D> result = []

        def add = (@DelegatesTo(models.common.CardHolder.class) Closure cfg) -> {
            holder.with cfg
            result << (
                    simplify
                            ? cube(holder.totalWidth, holder.totalHeight, holder.totalDepth).color(Color.color(0.5, 1, 0.7, 0.75))
                            : holder.render().color(Color.color(0.5, 1, 0.7))
            ).dz(z)
            z += holder.totalDepth + 0.2
        }

        add { caption = "SEQUENCE OF PLAY" }
        add { caption = "ADVENTURE" }

        holder.innerDepth = 4

        ["ASSASSIN", "SWASHBUCKLER", "BATTLERAGER", "FIGHTER", "BARBARIAN", "ROGUE", "ARCHER", "RANGER"]
                .each { def name -> add { caption = name } }

        add { innerDepth = 14; caption = "ENCOUNTER" }
        add { innerDepth = 10; caption = "MONSTER" }
        add { innerDepth = 12; caption = "TREASURE" }

        result
    }

    static bigTilesHolder(boolean simplify = false) {
        double internalWidth = BIG_SQUARE + BIG_SQUARE_NO_PAD
        double internalHeight = BIG_SQUARE
        double tokensDepth = 13
        double wall = 0.8
        double floor = wall
        double tilesDepth = 10

        if (simplify) {
            return cube(wall + TILES_SQUARE_TO_LOCK + BIG_SQUARE + BIG_SQUARE_NO_PAD + TILES_SQUARE_TO_LOCK + wall,
                    wall + TILES_SQUARE_TO_LOCK + BIG_SQUARE + TILES_SQUARE_TO_LOCK + wall, floor + tokensDepth + tilesDepth)
        }

        def ww1 = cube(10, 10, tokensDepth) - cylinder(tokensDepth, 10)

        double statusWidth = 29
        double statusHeight = 79
        def statuses = outlineXYCube(
                rcube(statusWidth, statusHeight, tokensDepth, 8, true, false, false, true),
                wall
        ) - cube(wall, statusHeight / 2, tokensDepth).dxy(statusWidth, -wall)

        double healingSurgeSize = 53
        def healingSurge = cube(healingSurgeSize + wall, healingSurgeSize + wall, tokensDepth) -
                rcube(healingSurgeSize, healingSurgeSize, tokensDepth, 16, false, false, true, true)

        double chestWidth = 53
        double chestHeight = 27
        def chest = outlineXYCube(cube(chestWidth, chestHeight, tokensDepth), wall) -
                cube(8, wall, tokensDepth).dxy(chestWidth - 8, chestHeight) -
                cube(wall, chestHeight / 2 + wall, tokensDepth).dxy(chestWidth, chestHeight / 2)

        double timeWidth = 31
        double timeHeight = 24.5
        def time = outlineXYCube(cube(timeWidth, timeHeight, tokensDepth), wall) -
                cube(wall, timeHeight - ww1.bounds().size().x, tokensDepth).dxy(timeWidth, ww1.bounds().size().x)

        def hp5Width = 38
        def hp5Height = 28
        def hp5 = outlineXYCube(rcube(hp5Width, hp5Height, tokensDepth, 8, false, false, true, true), wall) -
                cube(16, wall, tokensDepth).dxBy(-0.5).dxy(hp5Width / 2, hp5Height)

        def stanceWidth = 54
        def stanceHeight = 28
        def stance = (outlineXYCube(rcube(stanceWidth, stanceHeight, tokensDepth, 8), wall) -
                cube(32, wall, tokensDepth).dxBy(-0.5).dxy(stanceWidth / 2, stanceHeight)).rcz(180)

        def dmgWidth = 21
        def dmgHeight = 18
        def dmg = outlineXYCube(cube(dmgWidth, dmgHeight, tokensDepth), wall)

        def statuses1 = statuses
        def statuses2 = statuses.rcz(180).dxy(statusWidth + wall, internalHeight - statusHeight)
        def healing1 = healingSurge.dx((statusWidth + wall) * 2)
        def chest1 = chest.dxy((statusWidth + wall) * 2, healingSurgeSize + wall)
        def time1 = time.dxy((statusWidth + wall) * 2, healingSurgeSize + wall + chestHeight + wall)
        def hp51 = hp5.dx(healing1.bounds().max.x)
        def stance1 = stance.dxy(internalWidth - stanceWidth, internalHeight - stanceHeight)
        def dmg1 = dmg.rz(90).dxy(internalWidth, internalHeight - stanceHeight - dmgWidth - wall)
        def dmg2 = dmg1.dy(-dmgWidth - wall)


        def tilesStroke = extrude(bigTilesHolderStroke(), tilesDepth)
        def tilesHolder = outlineXYCube(tilesStroke, wall, 4).dxy(wall).start()
        def tokensHolder = rcube(tilesHolder.width, tilesHolder.height, tokensDepth + floor, 4)
        def tokens = statuses1 + statuses2 + healing1 + chest1 + time1 + hp51 + stance1 +
                (dmg1 + dmg2 -
                        cube(dmgHeight / 2, dmgWidth, tokensDepth)
                                .dxyBy(-0.5)
                                .dxy(dmg1.minX + wall / 2, dmg1.minY + wall / 2)
                ) +
                ww1.dxy(time1.bounds().max.x, time1.bounds().min.y + wall)


        def bottom = tokensHolder - (cube(internalWidth, internalHeight, tokensDepth) - tokens).center().dzBy(0.5).dxyz(tokensHolder.centerX, tokensHolder.centerY, floor)
        return bottom + tilesHolder.dz(bottom.getMaxZ()) - cylinder(tilesDepth + floor * 8, 24).center().dzBy(-0.5).dz(floor + tilesDepth + tokensDepth)
    }

    static edgeTiles(boolean simplify = false) {
        double extra = 2
        double widthExact = 45
        double heightExact = 101

        double width = widthExact + extra
        double height = heightExact + extra

        def drawer = draw(0, 0).go(width)
                .cw().go(height)
                .cw().go(9)
                .r(45).go((width - 9) * Math.sqrt(2))

        def tree = drawer.close().asTree()
        def t2 = tree.copy().tap { transform(AffineTransformMatrix2D.createRotation(Math.toRadians(-90)).scale(1, -1).translate(0, height - width + 16)) }
        tree.transform(AffineTransformMatrix2D.createTranslation(width + 15, 0))
        tree.union(draw(0, 0).go(60).cw().go(65).cw().go(60).close().asTree())
        tree.union(t2)

        def cuts = extrude(fromTree(tree), 23)
        return (
                rcube(112, 122, 25).dxyBy(-0.5, -0.5) -
                        cuts.dxyBy(-0.5, -0.5).dz(2)
        ).start() -
                CardHolder.makeCut(16, 4, 2, 25).dx(30) -
                CardHolder.makeCut(16, 4, 2, 25).rz(90).dx(112).dy(122 / 3) -
                CardHolder.makeCut(16, 4, 2, 25).rz(180).dx(112 / 3).dy(122)
    }

    static CADObject3D miniHolderBig() {
        double tw = TOTAL_WIDTH / 2
        double tw1 = 65
        double th1 = TOTAL_WIDTH - 94  // card holder + extra
        double th2 = TOTAL_WIDTH - 130 // tiles holder + extra
        double wall = 1.6

        def outline = (double r, double r2, double d) -> {
            draw(d, d).smooth(r).go(tw - d * 2).cw()
                    .smooth(r).go(th1 - d * 2).cw()
                    .smooth(r).go(tw1 - d * 2).cw()
                    .smooth(r).go(th1 - th2).ccw()
                    .smooth(r2).go(tw - tw1).cw()
                    .smooth(r).close()
        }


        def outout = extrude(outline(8, 8 - wall, 0), 100)
        def out = outout - extrude(outline(8 - wall, 8, wall), 100)

        def mid = new MiniHolder((th1 - wall * 2) / 3, tw1 - wall, 48.5).tap {
            holdThickness = 1.6
            cutDelta = 1
            bridgeSize = 6
        }

        def big = new MiniHolder(th2 - mid.width, tw - tw1, 71.6).tap {
            holdThickness = 2
            cutDelta = 1
            bridgeSize = 8
        }

        def first = mid.render()
                .rz(90)
                .end(true, false, false)
                .dxy(tw - wall, wall)

        def bigMini = big.render()
                .rz(-90)
                .start(true, true, false)
                .dx(wall)

        return (out + first + first.dyBy(1) + first.dyBy(2) + bigMini +
                mid.tap { height = big.height }.render().rz(-90).start(true, true, false).dy(bigMini.maxY) +
                cube(20, 20, 1).dxy(tw - tw1 - 20, th2)) & outout -
                cube(tw, th1, 100).dxyz(-16, 60, 16)
    }

    static CADObject3D miniHolderSmall1() {
        double tw = TOTAL_WIDTH / 2
        double th = TOTAL_WIDTH - 130 // tiles holder + extra
        double wall = 1.6

        def small = new MiniHolder((th - wall * 2) / 5, (tw - wall * 2) / 4, 24.8).tap {
            bridgeSize = 3
            extraShift = 4
        }

        def s1 = small.render()
        def s2 = s1.rcz(180).dy(s1.maxY)
        def s3 = s1.dy(s2.maxY)
        def s4 = s2.start().dy(s3.maxY)

        def row = s1 + s2 + s3 + s4 -
                cylinder(s1.width * 1.5, 16)
                        .rx(90)
                        .center(true, false, false)
                        .dy(tw * 0.55)
                        .dx(s1.centerX)
                        .dz(3.6)

        def holders = row + row.dx(s1.width) + row.dx(s1.width * 2) + row.dx(s1.width * 3) + row.dx(s1.width * 4)

        def outline = rcube(th, tw, 45, 8)
        def inline = rcube(th - wall * 2, tw - wall * 2, 45, 8 - wall).dxy(wall)

        return CardHolder.withTeeth((holders.dxy(wall) + (outline - inline)) & outline -
                cube(outline.height * 0.75, outline.width, outline.height)
                        .center(true, false, false).dxz(outline.centerX, 16),
                8, 2, 1.6, 16
        )
    }

    static CADObject3D miniHolderSmall2() {
        double tw = TOTAL_WIDTH / 2
        double th = TOTAL_WIDTH - 130 // tiles holder + extra
        double wall = 1.6

        def small = new MiniHolder((th - wall * 2) / 4, (tw - wall * 2) / 4, 24.8).tap {
            bridgeSize = 3
            extraShift = 4
        }

        def small2 = new MiniHolder((th - wall * 2) / 4, (tw - wall * 2) / 4, 25).tap {
            bridgeSize = 3
            extraShift = 4
            trampoline = 2
        }

        def tiny = new MiniHolder(small.width, small.height, 19.8).tap {
            bridgeSize = 2
            extraShift = 0.5
        }

        def ss = small.render()
        def s2 = small2.render()

        def ts = tiny.render()

        def rowSmall = ss + ss.dx(ss.width) + ss.dx(ss.width * 2) + ss.dx(ss.width * 3)
        def rowSmallR = rowSmall.rcz(180)
        def rowSmall2 = s2 + s2.dx(s2.width) + s2.dx(s2.width * 2) + s2.dx(s2.width * 3)
        def rowSmall2R = rowSmall2.rcz(180)
        def rowTiny = ts + ts.dx(ts.width) + ts.dx(ts.width * 2) + ts.dx(ts.width * 3)
        def rowTinyR = rowTiny.rcz(180)

        def holders = rowSmall + rowTinyR.dy(rowSmall.height) + rowTiny.dy(rowSmall.height * 2) + rowSmall2R.dy(rowSmall.height * 3)

        def outline = rcube(th, tw, 55, 8)
        def inline = rcube(th - wall * 2, tw - wall * 2, 55, 8 - wall).dxy(wall)

        return CardHolder.withTeeth((holders.dxy(wall) + (outline - inline)) & outline -
                cube(outline.height * 0.75, outline.width, outline.height)
                        .center(true, false, false).dxz(outline.centerX, 16),
                8, 2, 1.6, 16
        )
    }
}
