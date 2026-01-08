package models.dnd

import com.github.artyomcool.cadabro.MiniHolder
import com.github.artyomcool.cadabro.RenderCollection
import com.github.artyomcool.cadabro.d2.CADObject2D
import com.github.artyomcool.cadabro.d2.Levels2D
import com.github.artyomcool.cadabro.d2.Union2D
import com.github.artyomcool.cadabro.d3.CADObject3D
import javafx.scene.paint.Color
import models.common.CardHolder
import models.common.Outline
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D

import static com.github.artyomcool.cadabro.d2.CADObject2D.draw
import static com.github.artyomcool.cadabro.d2.CADObject2D.fromTree
import static com.github.artyomcool.cadabro.d3.CADObjects.*
import static models.dnd.Common.*

class ElementalEvil {

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

            // add miniHolderSmall2()

            //add heroStash()
            //add tilesHolder(72, true)
            //add cardHolders(false)
            //add bigTilesHolder(false)
            //add moneyHolder(false)
            //add moneyHolder(true)
            //add advancedTokenHolder("REGAIN 2HP", "RECHARGE")
            //add advancedTokenHolder("REROLL", "+1 DAMAGE")
            //add circleTokens()
            //add bigSquareHolder()
            //add miniHolderSmall1()
            //add miniHolderBig()
            add trapsHolder()
            add diceHolder()
            add tmpHolder()
        }
    }

    static trapsHolder() {
        def holder = new CardHolder(100, 62, 25).tap {
            captionLeft = "Traps"
        }
        return holder.withTeethAndText()
    }

    static diceHolder() {
        def holder = new CardHolder(100, 62, 35)
        return holder.withTeethAndText()
    }

    static tmpHolder() {
        def holder = new CardHolder(100, 62, 16)
        return holder.withTeethAndText()
    }

    static advancedToken() {
        draw()
            .go(37)
            .r(45)
            .go(27 + 0.6)
            .r(90)
            .go(52 + 27 + 0.4 + 0.6)
            .close()
    }

    static advancedTokenHolder(String left, String right) {
        def holder = new CardHolder(100, 62, 14)
        holder.tap {
            captionLeft = left
            captionRight = right
        }.withTeethAndText() + cube(CardHolder.WALL, CardHolder.WALL + holder.innerHeight * Math.sqrt(2), holder.totalDepth)
                .centerXY()
                .rz(45)
                .dxy(holder.totalWidth / 2, holder.totalHeight / 2)
    }

    static roundCut(CADObject3D obj) {
        obj & rcube(obj.width, obj.height, obj.depth)
    }

    static circleTokens() {
        def totalWidth = 100 + CardHolder.WALL * 2
        def totalHeight = 62 + CardHolder.WALL * 2
        def innerDepth = 14

        def tokenInnerWidth = (totalWidth - CardHolder.WALL) / 3 - CardHolder.WALL
        def tokenInnerHeight = (totalHeight - CardHolder.WALL) / 2 - CardHolder.WALL

        def holder = new CardHolder(tokenInnerWidth, tokenInnerHeight, innerDepth).tap {
            innerRadius = 13
            outerRadius = 0
            cornerCutRadius = 3
            cutRadius = 13
        }

        def result = union()
        3.times {
            result << holder.base().dx(it * (tokenInnerWidth + holder.wall))
            result << holder.base().rcz(180).dx(it * (tokenInnerWidth + holder.wall)).dy((tokenInnerHeight + holder.wall))
        }
        def cut = holder.cuts() +
                rcut(holder.cornerCutR, holder.wall * 4)
                        .rx(90)
                        .rz(-90)
                        .centerX()
                        .endZ()
                        .dx(0.5  * (tokenInnerWidth + holder.wall))
                        .dy(holder.cutR + holder.cornerCutR + holder.wall - 0.1)
                        .dz(holder.totalDepth)
        def cuts = union()
        cuts << cut.dx(0.5 * (tokenInnerWidth + holder.wall))
        cuts << cut.rcz(180).dx(0.5 * (tokenInnerWidth + holder.wall)).dy(result.height-cut.height)
        cuts << cut.rcz(90).endX().centerY().dx(result.width).dy((result.height)/2)
        result -= cuts

        CardHolder.withTeeth(
                roundCut(result),
                5,
                holder.getTeethHeight(),
                holder.wall,
                holder.wall * 2 + 2
        )
    }

    static heroStash() {
        def bigSquareSize = BIG_SQUARE_NO_PAD + 2
        def token = advancedToken()

        def t = Union2D.plus(token, token.rotate(180).dxy(CARD_WIDTH, CARD_HEIGHT))
        def main = new Levels2D()
                .add(14, t.dxy(3))
                .add(16, rsquare(CARD_WIDTH, CARD_HEIGHT, 2).dxy(3))
                .add(2.6, rsquare(bigSquareSize, bigSquareSize, 2))
                .extrude()
        def extra = (rcube(33, 50, 30) + rcube(33, 47, 30).endY().dy(main.height - 6))
                .endX()
                .dx(main.width)
                .dxy(-2.4, 3)

        def cut = main + extra
        def r = rcube(cut.width + 4, cut.height + 4, cut.depth + 2, 2) - cut.dxyz(2)
        return r - cylinder(r.depth, 16).centerXY().dy(63) - cylinder(cut.depth, 14).centerXY().dxyz(70, 38, 2)
    }

    static moneyHolder(boolean putWall) {
        def innerHeight = 64.4
        def innerDepth = 16
        def holder = new CardHolder(89.6, innerHeight, innerDepth)
                .tap { caption = "Money" }.withTeethAndText()
        if (!putWall) {
            return holder
        }

        holder + cube(CardHolder.WALL, innerHeight, innerDepth).centerXY().dxy(36, holder.centerY)
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

        //holder.innerDepth = 4

        //["ASSASSIN", "SWASHBUCKLER", "BATTLERAGER", "FIGHTER", "BARBARIAN", "ROGUE", "ARCHER", "RANGER"]
        //        .each { def name -> add { caption = name } }

        add { innerDepth = 14; caption = "ENCOUNTER" }
        add { innerDepth = 14; caption = "MONSTER" }
        add { innerDepth = 15; caption = "TREASURE" }

        result
    }

    static bigSquareHolder() {
        return bigSquareHolder(6.8)
    }

    static bigTilesHolder(boolean simplify = false) {
        double internalWidth = BIG_SQUARE + BIG_SQUARE_NO_PAD
        double internalHeight = BIG_SQUARE
        double tokensDepth = 13
        double wall = 0.8
        double floor = wall
        double tilesDepth = 14

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

        double timeWidth = 31
        double timeHeight = 26
        def time = outlineXYCube(cube(timeWidth, timeHeight, tokensDepth), wall) -
                cube(wall, timeHeight - ww1.bounds().size().x, tokensDepth).dxy(timeWidth, ww1.bounds().size().x)

        def hp5Width = 40
        def hp5Height = 28
        def hp5 = outlineXYCube(rcube(hp5Width, hp5Height, tokensDepth, 8, false, false, true, true), wall) -
                cube(16, wall, tokensDepth).dxBy(-0.5).dxy(hp5Width / 2, hp5Height)

        def dmgWidth = 22.5
        def dmgHeight = 18
        def dmg = outlineXYCube(cube(dmgWidth, dmgHeight, tokensDepth), wall)

        def stunWidth = 30
        def stunHeight = 18
        def stun = outlineXYCube(cube(stunWidth, stunHeight, tokensDepth), wall)

        def statuses1 = statuses
        def statuses2 = statuses.rcz(180).dxy(statusWidth + wall, internalHeight - statusHeight)
        def healing1 = healingSurge.dx((statusWidth + wall) * 2)
        def time1 = time.dxy((statusWidth + wall) * 2, healingSurgeSize + wall)
        def time2 = time.dxy((statusWidth + wall) * 2, healingSurgeSize + wall + timeHeight + wall)
        def timePair = time1 + time2 - cube(timeWidth / 1.5, timeHeight, tokensDepth).endY().dxy(time2.centerX + 7, time2.centerY)
        def hp51 = hp5.dx(healing1.bounds().max.x)
        def hp52 = hp5.rz(90).dx(internalWidth)

        def stun1 = stun.dxy(internalWidth - stunWidth, internalHeight - stunHeight)
        def stun2 = stun1.dx(-stunWidth - wall)
        def stunPair = (stun1 + stun2 -
                cube(stunWidth, stunHeight / 2, tokensDepth)
                        .dxyBy(-0.5)
                        .dxy(stun1.minX + wall / 2, stun1.minY + wall / 2)
        )

        def dmg1 = dmg.rz(90).dxy(internalWidth, internalHeight - dmgWidth)
        def dmg2 = dmg1.dy(-dmgWidth - wall)
        def dmgPair = (dmg1 + dmg2 -
                cube(dmgHeight / 2, dmgWidth, tokensDepth)
                        .dxyBy(-0.5)
                        .dxy(dmg1.minX + wall / 2, dmg1.minY + wall / 2)
        )
        def dmgPair1 = dmgPair.dy(-(stunHeight + wall))
        def dmgPair2 = dmgPair.rz(90)
                .end(true, true, false)
                .dxy(stunPair.bounds().min.x+wall, stunPair.bounds().max.y)

        def tilesStroke = extrude(bigTilesHolderStroke(), tilesDepth)
        def tilesHolder = outlineXYCube(tilesStroke, wall, 4).dxy(wall).start()
        def tokensHolder = rcube(tilesHolder.width, tilesHolder.height, tokensDepth + floor, 4)
        def tokens = statuses1 + statuses2 + healing1 + timePair + hp51 + hp52 +
                stunPair + dmgPair1 + dmgPair2 +
                ww1.dxy(time1.bounds().max.x, time1.bounds().min.y)


        def bottom = tokensHolder - (cube(internalWidth, internalHeight, tokensDepth) - tokens).center().dzBy(0.5).dxyz(tokensHolder.centerX, tokensHolder.centerY, floor)
        return bottom + tilesHolder.dz(bottom.getMaxZ()) - cylinder(tilesDepth + floor * 8, 24).center().dzBy(-0.5).dz(floor + tilesDepth + tokensDepth)
    }

    static double cornerPartWidth = 205
    static double cornerPartHeight = 70
    static double mainPartWidth = 175
    static double mainPartHeight = 125
    static double wall = 1.6
    static double toothD = 16
    static double toothW = 8

    static Outline minisOutline(double depth) {
        Outline.draw(wall, depth) {
            smooth(8).dx(cornerPartWidth)
            smooth(8).dy(cornerPartHeight)
            smooth(8).dx(mainPartWidth-cornerPartWidth)
            smooth(0).dy(mainPartHeight)
            smooth(8).dx(-mainPartWidth)
            smooth(8).close()
        }
    }

    static CADObject3D miniHolderBig() {
        def mid = new MiniHolder(cornerPartHeight, 56, 48.5).tap {
            holdThickness = 1.6
            cutDelta = 1
            bridgeSize = 6
        }
        def small = new MiniHolder(
                mainPartHeight / 3,
                (mid.height * 2 - (cornerPartWidth-mainPartWidth))/ 2,
                24.8
        ).tap {
            bridgeSize = 3
            extraShift = 4
        }

        def out = minisOutline(65)

        def midMinis = mid.render2().rz(90).endX().startY().dx(out.outline.width)
        def smallMinis = small.render2().rz(90).start().stripeY(3).endX().dx(mainPartWidth).dy(midMinis.height)

        def floor = cube(midMinis.minX, smallMinis.maxY, small.bgHeight)
        def w = (
                cube(wall, out.outline.height, out.outline.depth) -
                        cube(wall, out.outline.height / 3, out.outline.depth).dy(out.outline.height / 3)
        ).dx(midMinis.minX - wall)

        return (CardHolder.withTeeth(
                (midMinis + smallMinis + floor + w + out.wall) -
                        cube(wall, mainPartHeight / 2, out.outline.depth)
                                .endX().dx(mainPartWidth)
                                .endY().dy(out.outline.height - mainPartHeight / 4)
                                .dz(25),
                toothW, 2, 1.6, toothD
        ) & (out.outline + out.outline.dz(20))).mirrorX()
    }

    static CADObject3D miniHolderSmall1() {
        double totalDepth = 44

        def cornerMinis = union {
            def sPair1 = new MiniHolder(
                    (cornerPartWidth - wall * 2) / 6,
                    (cornerPartHeight - wall) / 2,
                    24.8
            ).tap {
                bridgeSize = 3
                extraShift = 4
            }.render2()

            6.times {
                add sPair1.dx(it * sPair1.width)
            }
        }

        def mainMinis = union {
            def small = new MiniHolder(
                    (mainPartWidth - wall * 2) / 5,
                    (mainPartHeight - wall) / 3,
                    24.8
            ).tap {
                bridgeSize = 3
                extraShift = 4
            }

            def tiny = new MiniHolder(
                    small.width,
                    small.height,
                    20
            ).tap {
                bridgeSize = 3
                extraShift = 4
            }

            def sTriple = small.render(3)

            4.times {x ->
                add sTriple.dx(x * sTriple.width)
            }

            def t1 = tiny.render(3)
            add t1.dx(4 * sTriple.width)
        }

        def holders = cornerMinis + mainMinis.dy(cornerMinis.height)

        def out = minisOutline(totalDepth)

        def outline = out.outline

        double depthDelta = 24
        return (CardHolder.withTeeth((holders.dxy(wall) + out.wall) -
                cube(outline.width - (toothW + toothD) * 2, wall*2, outline.depth)
                        .centerX().dxz(outline.centerX, depthDelta) -
                cube(wall*2, outline.height - (toothW + toothD) * 2, outline.depth)
                        .centerY().dyz(outline.centerY, depthDelta) -
                cube(outline.width - (toothW + toothD) * 2 - 40, wall*2, outline.depth)
                        .centerX().dxyz(outline.centerX - 20, outline.height-wall, depthDelta) -
                cube(wall*2, mainPartHeight - 32, outline.depth)
                        .dxyz(mainPartWidth-wall, cornerPartHeight + 16, depthDelta),
                toothW, 2, 1.6, toothD
        ) & (outline + outline.dz(20))).mirrorX()
    }
}
