package models.dnd

import com.github.artyomcool.cadabro.MiniHolder
import com.github.artyomcool.cadabro.RenderCollection
import com.github.artyomcool.cadabro.d2.Levels2D
import com.github.artyomcool.cadabro.d2.Union2D
import com.github.artyomcool.cadabro.d3.CADObject3D
import com.github.artyomcool.cadabro.d3.CADObjects
import com.github.artyomcool.cadabro.d3.Union
import javafx.scene.paint.Color
import models.common.CardHolder
import models.common.Outline

import static com.github.artyomcool.cadabro.d2.CADObject2D.draw
import static com.github.artyomcool.cadabro.d2.CADObject2D.square
import static com.github.artyomcool.cadabro.d3.CADObjects.*
import static models.common.CardHolder.WALL
import static models.dnd.Common.*

class DungeonOfTheMadMage {

    static double TOTAL_WIDTH = 304

    static render() {
        new RenderCollection().tap {
            add(cube(TOTAL_WIDTH, TOTAL_WIDTH, 115), true, true)

            // add miniHolderSmall2()

            //add heroStash()
            //add tilesHolder(97, true, false, 0.8, 3.2)
            add cardHolders(false)
            //add cardHoldersExtra(false)
            //add trapCardHolder()
            //add advancedTokenHolder("YAWNING PORTAL", "THE DEPTHS", 6)
            //add advancedTokenHolder("SKULLPORT", "SKULLPORT", 4)
            //add circleTokens()
            //add weakenAndLevels()
            //add advantageAndHits()
            //add hp5SurgeHealing()
            //add stunAndAdvantage()
            //add bigMoneyTokens()
            //add smallMoneyTokens()
            //add smallHpTokens()
            //add bigSquareHolder()
            //add bigTilesHolder()
            //add miniHolderSmall1()
            //add miniHolderMedium()
            //add diceHolder()
            //add tmpHolder()
            //add fixer()
            //add moneyHolder(true)
            //add trapsHolder()
            //add mediumMinis()
            //add bigMinis()
            //add bottomMinis()
            //add smallAndTinyMinis()
            //add miniHolderMedium2()
            //add miniHolder()
        }
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

    static advancedTokenHolder(String left, String right, int count = 6) {
        def holder = new CardHolder(89.6, 64.4, count * 2.4)
        holder.tap {
            captionLeft = left
            captionRight = right
            captionFontSize = 7
            addToBase = {
                cube(WALL, WALL + holder.innerHeight * Math.sqrt(2), holder.totalDepth)
                        .centerXY()
                        .rz(45)
                        .dxy(holder.totalWidth / 2, holder.totalHeight / 2)
            }
        }.renderThin(0.8, false)
    }

    static roundCut(CADObject3D obj, double r = 2) {
        obj & rcube(obj.width, obj.height, obj.depth, r)
    }

    static weakenAndLevels() {
        double innerDepth = 2.4 * 5 + 0.2
        double wall = 0.8
        double floor = wall

        def holder = new CardHolder(CARD_HEIGHT, CARD_WIDTH, innerDepth).tap {
            floorThickness = floor
            captionLeft = "WEAKEN"
            captionRight = "LEVEL"
        }

        def r = union()
        r << holder.renderThin(0.8, false)
        for (int i = 1; i <= 2; i++) {
            r << cube(wall, CARD_WIDTH, innerDepth + 0.8).dx(i * CARD_HEIGHT / 3).dxy(WALL)
        }
        return r
    }

    static advantageAndHits() {
        double innerDepth = 2.4 * 5 + 1
        double wall = 0.8
        double floor = wall

        def holder = new CardHolder(CARD_HEIGHT, CARD_WIDTH, innerDepth).tap {
            floorThickness = floor
            captionLeft = "ADVANTAGE"
            captionRight = "HITS"
        }

        def r = union()
        r << holder.renderThin(0.8, false)
        for (int i = 1; i <= 2; i++) {
            r << cube(CARD_HEIGHT, wall, innerDepth + 0.8).dy(27).dxy(WALL)
        }
        return r
    }

    static hp5SurgeHealing() {
        double innerDepth = 2.4 * 4 + 1
        double wall = 0.4
        double floor = 0.4

        def holder = new CardHolder(CARD_HEIGHT, CARD_WIDTH, innerDepth).tap {
            floorThickness = floor
            captionLeft = "HP5"
            captionRight = "HEAL SURGE"
        }

        return holder.renderThin(wall, false) + cube(wall, CARD_WIDTH, innerDepth + floor).dx(52).dxy(WALL)
    }

    static stunAndAdvantage() {
        double innerDepth = 2.4 * 3 + 1
        double wall = 0.4
        double floor = 0.4

        def holder = new CardHolder(CARD_HEIGHT, CARD_WIDTH, innerDepth).tap {
            floorThickness = floor
            captionLeft = "STUNNED!"
            captionRight = "ADVANTAGE"
        }

        return holder.renderThin(wall, false) + cube(wall, CARD_WIDTH, innerDepth + floor).dx(CARD_HEIGHT / 2).dxy(WALL)
    }

    static smallMoneyTokens() {
        double innerDepth = 2.4 * 5

        def holder = new CardHolder(CARD_HEIGHT, CARD_WIDTH, innerDepth).tap {
            caption = "MONEY"
            floorThickness = 0.4
        }
        return holder.renderThin(0.8, false)
    }

    static smallHpTokens() {
        double innerDepth = 2.4 * 5

        def holder = new CardHolder(CARD_HEIGHT, CARD_WIDTH, innerDepth).tap {
            caption = "HP"
            floorThickness = 0.4
        }
        return holder.renderThin(0.8, false)
    }

    static bigMoneyTokens() {
        def wall = 0.8
        def innerDepth = 2.4 * 4 + 0.4

        def totalWidth = CARD_HEIGHT + WALL * 2
        def totalHeight = CARD_WIDTH + WALL * 2

        def hugeMoneyWidth = (totalHeight - wall) / 2 - wall
        def tokenInnerWidth = (totalWidth - wall - hugeMoneyWidth) / 2 - wall

        def bigHolder = new CardHolder(hugeMoneyWidth, hugeMoneyWidth, innerDepth).tap {
            innerRadius = 13
            outerRadius = 0
            cornerCutRadius = 3
            cutRadius = 13
            borderThickness = wall
            floorThickness = 0.8
        }

        def smallHolder = new CardHolder(tokenInnerWidth, hugeMoneyWidth, innerDepth).tap {
            innerRadius = 13
            outerRadius = 0
            cornerCutRadius = 3
            cutRadius = 13
            borderThickness = wall
            floorThickness = 0.8
        }

        def result = union()
        2.times {
            result << smallHolder.base().dx(it * (tokenInnerWidth + smallHolder.wall))
            result << smallHolder.base().rcz(180).dx(it * (tokenInnerWidth + smallHolder.wall)).dy((hugeMoneyWidth + bigHolder.wall))
        }
        result << bigHolder.base().dx(2 * (tokenInnerWidth + smallHolder.wall))
        result << bigHolder.base().rcz(180).dx(2 * (tokenInnerWidth + smallHolder.wall)).dy((hugeMoneyWidth + bigHolder.wall))

        def cut = smallHolder.cuts() +
                rcut(smallHolder.cornerCutR, smallHolder.wall * 4)
                        .rx(90)
                        .rz(-90)
                        .centerX()
                        .endZ()
                        .dx(0.5 * (tokenInnerWidth + smallHolder.wall))
                        .dy(smallHolder.cutR + smallHolder.cornerCutR + smallHolder.wall - 0.1)
                        .dz(smallHolder.totalDepth)
        def cuts = union()
        cuts << cut.dx(0.5 * (tokenInnerWidth + smallHolder.wall))
        cuts << cut.rcz(180).dx(0.5 * (tokenInnerWidth + smallHolder.wall)).dy(result.height - cut.height)
        cuts << cut.rcz(90).endX().centerY().dx(result.width).dy((result.height) / 2)
        result -= cuts

        return result
    }

    static circleTokens() {
        def wall = 0.8
        def innerDepth = 2.4 * 1

        def totalWidth = CARD_HEIGHT + WALL * 2
        def totalHeight = CARD_WIDTH + WALL * 2

        def tokenInnerWidth = (totalWidth - wall) / 3 - wall
        def tokenInnerHeight = (totalHeight - wall) / 2 - wall

        def holder = new CardHolder(tokenInnerWidth, tokenInnerHeight, innerDepth).tap {
            innerRadius = 13
            outerRadius = 0
            cornerCutRadius = 3
            cutRadius = 13
            borderThickness = wall
            floorThickness = 0.8
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
                        .dx(0.5 * (tokenInnerWidth + holder.wall))
                        .dy(holder.cutR + holder.cornerCutR + holder.wall - 0.1)
                        .dz(holder.totalDepth)
        def cuts = union()
        cuts << cut.dx(0.5 * (tokenInnerWidth + holder.wall))
        cuts << cut.rcz(180).dx(0.5 * (tokenInnerWidth + holder.wall)).dy(result.height - cut.height)
        cuts << cut.rcz(90).endX().centerY().dx(result.width).dy((result.height) / 2)
        result -= cuts

        CardHolder.withTeeth(
                roundCut(result, WALL + 2),
                5,
                holder.getTeethHeight(),
                WALL,
                WALL * 2 + 2
        )
    }

    static mediumMinis() {
        double wall = 0.8
        double floor = 0.4
        double totalWidth = 115

        def base = rcube(totalWidth, totalWidth, 65).centerXY() -
                rcube(totalWidth - wall * 2, totalWidth - wall * 2, 65).dz(floor).centerXY() +
                cube(wall, totalWidth, 10).centerXY() +
                cube(totalWidth, wall, 10).centerXY()
        return base - cylinder(totalWidth, 50).rx(90).center().dz(base.depth) -
                cylinder(totalWidth, 50).ry(90).center().dz(base.depth)
    }

    static bigMinis() {
        double wall = 0.8
        double floor = 0.4
        double totalWidth = 115
        double totalHeight = WALL * 2 + CARD_WIDTH
        def base = rcube(totalWidth, totalHeight, 65).centerXY() -
                rcube(totalWidth - wall * 2, totalHeight - wall * 2, 65).dz(floor).centerXY()
        return base - cylinder(totalWidth/2, 50).rx(90).center().dz(base.depth).dy(-totalWidth/2)
    }

    static bottomMinis() {
        double floor = 0.4
        double totalWidth = 230
        double totalHeight = 60
        double z = 70
        int cntX = 4

        def small = new MiniHolder(totalWidth / (cntX + 0.5), totalHeight, 24.8).tap {
            holdThickness = 1.6
            cutDelta = 1
            bridgeSize = 3
            bgHeight = floor
        }

        def first = small.render()
        def second = first.rcz(180)

        def outline = minisOutline(totalWidth, totalHeight, z)

        def cut = small.bottomCuts() - small.renderHolder()

        def base = first.stripeX(cntX) + second.stripeX(cntX).dx(first.width / 2)

        def cuts = cut.stripeX(cntX, first.width) + cut.rz(180).dxy(first.width * 1.5, first.height).stripeX(cntX, first.width)

        return (base - cuts + outline.wall) & outline.outline -
                cube(totalWidth * 0.8, totalHeight, z).dy(totalWidth * 0.1).dz(10).dx(totalWidth * 0.1)
    }

    static smallAndTinyMinis() {
        double floor = 0.4
        double totalWidth = 230
        double totalHeight = 180

        def small = new MiniHolder(totalHeight / 4, 75 / 2, 24.8).tap {
            holdThickness = 1.6
            cutDelta = 1
            bridgeSize = 8
            bgHeight = floor
            cutAngle = 90
        }

        def tiny = new MiniHolder(small.width, small.height, 20).tap {
            holdThickness = 1.6
            cutDelta = 1
            bridgeSize = 8
            bgHeight = floor
            strengthWallSize = small.strengthWall
            extraShift = 4
            cutAngle = 90
        }

        def stripe1 = small.render2().stripeX(4)
        def stripe20 = small.render().stripeX(2)
        def stripe25 = tiny.render().stripeX(2)
        def stripe2 = stripe20 + stripe25.dx(stripe20.width)

        def t = tiny.render(3, true).stripeX(4)

        def base = stripe1 + stripe2.dy(75) + t.dy(75 * 1.5)

        def outline = minisOutline(base.width, base.height, 40)
        println base.width + " " + base.height

        return (base + outline.wall) & outline.outline -
                cube(totalHeight * 0.9, totalWidth * 0.9, 40).dz(10)
    }

    static heroStash() {
        double wall = 0.8
        double floor = 0.8
        double oneLayerDepth = 2.4
        double totalWidth = totalWidthTilesHolderStroke(wall)
        double totalHeight = wall + wall + BIG_SQUARE_NO_PAD + wall + wall
        double totalDepth = 27.4
        double miniDepth = totalDepth - floor
        double tokenDepth = oneLayerDepth * 4
        def bigSquareSize = wall + BIG_SQUARE_NO_PAD + wall
        def token = advancedToken()

        def t = Union2D.plus(token, token.rotate(180).dxy(CARD_WIDTH, CARD_HEIGHT))
        def main = new Levels2D()
                .add(tokenDepth, t.dy((bigSquareSize - t.height) / 2))
                .add(miniDepth - tokenDepth - oneLayerDepth, rsquare(CARD_WIDTH, CARD_HEIGHT, 2).dy((bigSquareSize - CARD_HEIGHT) / 2))
                .add(oneLayerDepth, rsquare(bigSquareSize, bigSquareSize, 2).dx(wall * 2))
                .extrude()
        def mini = rcube(totalWidth - CARD_WIDTH - wall * 3, 50, miniDepth)
                .dy(wall * 4)

        def storage = rcube(bigSquareSize - CARD_WIDTH - wall * 2, main.height - mini.maxY - wall, miniDepth)
                .dy(mini.maxY + wall)
        def holdCut = rcube(totalWidth - wall * 3 - bigSquareSize, storage.maxY - mini.minY - wall * 8, miniDepth)
                .dy(wall * 4)
        def extra = (mini + storage + holdCut.dx(storage.maxX + wall))
                .endX()
                .dx(totalWidth - wall * 2)


        def cut = main + extra
        def r = rcube(totalWidth, totalHeight, cut.depth + floor, 2) - cut.dxyz(wall, wall, floor)
        return r - cylinder(r.depth, 16).centerXY().dy(60) -
                cylinder(cut.depth, 14).centerXY().dxyz(70, 38, floor)
    }

    static moneyHolder(boolean putWall) {
        double width = 115
        double height = 80
        double thinWall = 0.8
        double innerWidth = width - WALL * 2
        double innerHeight = height - WALL * 2
        double innerDepth = 16
        double floor = thinWall
        def holder = new CardHolder(innerWidth, innerHeight, innerDepth)
                .tap {
                    caption = "Money"
                    floorThickness = floor
                }
                .renderThin(thinWall, false)
        if (!putWall) {
            return holder
        }

        holder + cube(thinWall, innerHeight, innerDepth).centerXY().dxyz(55, holder.centerY, floor) +
                cube(55, thinWall, innerDepth).centerY().dxyz(0, holder.centerY, floor)
    }

    static miniHolderMedium2() {
        double totalWidth = 115
        double totalHeight = 80

        double floor = 0.8

        def mid = new MiniHolder(totalHeight, totalWidth / 2, 48.5).tap {
            holdThickness = 1.6
            cutDelta = 2.6
            bridgeSize = 6
            bgHeight = floor
        }

        def r = mid.render(2)
        def out = minisOutline(r.width, r.height, 65)
        return (CardHolder.withTeeth(r + out.wall, 5, 2, 1.6, WALL * 2 + 2) & out.outline) -
                cube(totalHeight * 2, totalWidth * 0.8, 35).dy(totalWidth * 0.1).dz(30)
    }

    static miniHolder() {
        double totalWidth = 235
        double totalHeight = 180
        double floor = 0.8

        def small = new MiniHolder(totalWidth / 6, totalHeight / 5, 24.8).tap {
            holdThickness = 1.6
            cutDelta = 1
            bridgeSize = 3
            bgHeight = floor
        }

        def baseSmall = small.render(2).stripeX(6)
        def extraSmall = small.render().stripeX(5)

        def tiny = new MiniHolder(small.width, small.height, 20).tap {
            holdThickness = 1.6
            cutDelta = 1
            bridgeSize = 3
            bgHeight = floor
            strengthWallSize = small.strengthWall
            extraShift = 4
        }

        def extraTiny = tiny.render()

        def extra = extraSmall + extraTiny.dx(extraSmall.maxX)

        def base = baseSmall + extra.dy(baseSmall.height)
        def all = base + tiny.render(2, true).stripeX(6).dy(base.maxY)
        def outline = minisOutline(all.width, all.height, 48)
        return (all & outline.outline) + outline.wall -
                cube(totalHeight * 2, totalWidth * 0.8, 35).dy(totalWidth * 0.1).dz(20)
    }

    static cardHolders(boolean simplify = false) {
        def holder = new CardHolder(89.6, 64.4, 3).tap {
            borderThickness = 1.6
            floorThickness = 0.8
        }
        double z = 0
        List<CADObject3D> result = []

        def add = (boolean thin = true, @DelegatesTo(models.common.CardHolder.class) Closure<Void> cfg) -> {
            holder.with cfg
            result << (
                    simplify
                            ? cube(holder.totalWidth, holder.totalHeight, holder.totalDepth)
                            : (thin ? holder.renderThin(0.8) : holder.render())
            ).dz(z)
            z += holder.totalDepth + 0.2
        }

        holder.innerDepth = 12
        add { caption = "ENCOUNTER" }
        add { caption = "MONSTER" }
        add { caption = "TREASURE" }

        holder.innerDepth = 6
        holder.captionFontSize = 6
        add { caption = "SPELL" }

        holder.innerDepth = 4
        holder.captionFontSize = 4
        add(false) { caption = "TRAP" }
        add(false) { caption = "ELDER RUNE" }

        holder.innerDepth = 3
        holder.captionFontSize = 3
        add(false) { caption = "ADVENTURE" }

        result
    }

    static cardHoldersExtra(boolean simplify = false) {
        def holder = new CardHolder(89.6, 64.4, 3).tap {
            borderThickness = 1.6
            floorThickness = 0.8
            captionFontSize = 7
        }
        double z = 0
        List<CADObject3D> result = []

        def add = (boolean thin = true, @DelegatesTo(models.common.CardHolder.class) Closure<Void> cfg) -> {
            holder.with cfg
            result << (
                    simplify
                            ? cube(holder.totalWidth, holder.totalHeight, holder.totalDepth)
                            : (thin ? holder.renderThin(0.8) : holder.render())
            ).dz(z)
            z += holder.totalDepth + 0.2
        }

        add {
            innerDepth = 9
            caption = "MONSTER EXTRA"
        }
        holder.innerDepth = 4
        holder.captionFontSize = 4
        add(false) { caption = "ENCOUNTER EXTRA" }
        add(false) { caption = "TREASURE EXTRA" }

        holder.innerDepth = 3
        holder.captionFontSize = 3
        add(false) { caption = "TRAP EXTRA" }

        result
    }

    static trapCardHolder() {
        def floor = 0.8
        def traps = 6
        def pad = 2
        def wall = 1.6
        def holder = new CardHolder(89.6, 64.4, 6.4).tap {
            borderThickness = wall
            floorThickness = floor + traps
            caption = "TRAPS"
        }
        def base = holder.renderThin(0.8, false) -
                holder.with { makeCut(cutR, cornerCutR, wall, totalDepth - traps).dx(totalWidth / 2).dz(traps) } -
                holder.tap { cutRadius = 7 }.with { makeCut(cutR, cornerCutR, wall + pad, traps).dx(totalWidth / 2) }

        def cut = cube(base.width - (pad + wall) * 2, base.height - (pad + wall) * 2, traps)
                .centerXY()
                .dxy(base.centerX, base.centerY)
                .dz(floor)
        return base - cut
    }

    static bigSquareHolder() {
        double wall = 0.8
        double floor = 0.4
        double oneLayer = 2.4
        return thinBigSquareHolder(wall, floor, oneLayer * 3 + 0.4)
    }

    static trapsHolder() {
        double wall = 0.8
        double floor = 0.4
        double oneLayer = 2.4
        def cut = cube(BIG_SQUARE + wall * 4)
        def holder = rcube(cut.width + wall * 2, cut.height + wall * 2, oneLayer * 3 + 0.4)
        return (holder.centerXY() - cut.centerXY().dz(floor)).start() - cylinder(holder.depth, 6, 10).centerXY()
    }

    static bigTilesHolder() {
        double wall = 0.8
        double floor = 0.4
        double tilesDepth = 15


        def tilesStroke = extrude(bigTilesHolderStroke(), tilesDepth)
        def tilesHolder = outlineXYCube(tilesStroke, wall, 4).dxy(wall).start()
        return rcube(tilesHolder.width, tilesHolder.height, floor, 4) +
                tilesHolder.dz(floor) -
                cylinder(tilesDepth + floor * 8, 24).center().dzBy(-0.5).dz(floor + tilesDepth)
    }

    static double toothD = 16
    static double toothW = 8

    static Outline minisOutline(double width, double height, double depth) {
        Outline.draw(0.8, depth) {
            smooth(8).dx(width)
            smooth(8).dy(height)
            smooth(8).dx(-width)
            smooth(8).close()
        }
    }

    static CADObject3D miniHolderMedium() {
        double wall = 0.8
        double floor = 0.8
        double totalWidth = totalWidthTilesHolderStroke(wall) + (wall + wall + BIG_SQUARE_NO_PAD + wall + wall)
        double totalHeight = 60

        def mid = new MiniHolder(totalHeight, totalWidth / 3.4, 48.5).tap {
            holdThickness = 1.6
            cutDelta = 2.6
            bridgeSize = 6
            bgHeight = floor
            extraShift = 8
            strengthWallSize = 18
        }
        def small = new MiniHolder(totalHeight, (totalWidth - mid.height * 2) / 2, 24.8).tap {
            holdThickness = 1.6
            cutDelta = 1
            bridgeSize = 3
            bgHeight = floor
            extraShift = 12
            strengthWallSize = 16
        }
        def r = mid.render(2) + small.render(2).dy(mid.height * 2)
        def out = minisOutline(r.width, r.height, 65)
        return (CardHolder.withTeeth(r + out.wall, toothW, 2, 1.6, toothD) & out.outline) -
                cube(totalHeight * 2, totalWidth * 0.8, 35).dy(totalWidth * 0.1).dz(30)
    }

    static CADObject3D fixer() {
        double layer = 2
        double floor = layer / 2
        double d = 22
        CADObject3D s = sphere(d / 2).center()
        return s - (
                cube(layer, d, d).centerXY() +
                        cube(d, layer, d).centerXY()
        ).dz(floor) -
                cube(d, d, d).centerXY().endZ() -
                cube(d, d, d).dxy(layer) -
                cube(d, d, d).endXY().dxy(-layer) -
                cube(d, d, d).endX().dx(-layer).dy(layer) -
                cube(d, d, d).endY().dx(layer).dy(-layer) -
                cube(d, d, d).centerXY().dz(d / 4 + floor)
    }
}
