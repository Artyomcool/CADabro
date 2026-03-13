package models.dnd

import com.github.artyomcool.cadabro.MiniHolder
import com.github.artyomcool.cadabro.RenderCollection
import com.github.artyomcool.cadabro.d2.Levels2D
import com.github.artyomcool.cadabro.d2.Union2D
import com.github.artyomcool.cadabro.d3.CADObject3D
import models.common.CardHolder
import models.common.Outline

import static com.github.artyomcool.cadabro.d2.CADObject2D.draw
import static com.github.artyomcool.cadabro.d3.CADObjects.*
import static models.dnd.Common.*

class TombOfAnnihilation {

    static double TOTAL_WIDTH = 304

    static render() {
        new RenderCollection().tap {
            add(cube(TOTAL_WIDTH, TOTAL_WIDTH, 115), true, true)

            // add miniHolderSmall2()

            //add heroStash()
            //add tilesHolder(39, true, false, 0.8, 3.2)
            //add tilesHolder(52, true, false, 0.8, 3.2)
            //add cardHolders(false)
            //add cardHoldersExtra(false)
            //add trapCardHolder()
            //add advancedTokenHolder("PORT NYANZARU", "KIR SABAL", 6)
            //add advancedTokenHolder("FORT BELURIAN", "FORT BELURIAN", 4)
            //add circleTokens()
            //add bigSquareHolder()
            //add miniHolderSmall1()
            //add miniHolderMedium()
            //add diceHolder()
            //add tmpHolder()
            //add fixer()
            //add moneyHolder(true)
            //add trapsHolder()
            //add miniHolderMedium2()
            add miniHolder()
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

    static circleTokens() {
        def wall = 0.4
        def innerDepth = 2.4 * 3

        def totalWidth = CARD_HEIGHT + CardHolder.WALL * 2
        def totalHeight = CARD_WIDTH + CardHolder.WALL * 2

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
                roundCut(result, CardHolder.WALL + 2),
                5,
                holder.getTeethHeight(),
                CardHolder.WALL,
                CardHolder.WALL * 2 + 2
        )
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
        double innerWidth = width - CardHolder.WALL * 2
        double innerHeight = height - CardHolder.WALL * 2
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

    static trapsHolder() {
        double width = 115
        double height = 80
        double thinWall = 0.8
        double innerWidth = width - CardHolder.WALL * 2
        double innerHeight = height - CardHolder.WALL * 2
        double innerDepth = 8
        double floor = thinWall
        def holder = new CardHolder(innerWidth, innerHeight, innerDepth)
                .tap {
                    caption = "TRAPS"
                    floorThickness = floor
                    captionFontSize = 6
                }
                .renderThin(thinWall, false)
        return holder
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
        return (CardHolder.withTeeth(r + out.wall, 5, 2, 1.6, CardHolder.WALL * 2 + 2) & out.outline) -
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

        holder.innerDepth = 15
        add { caption = "ENCOUNTER" }
        add { caption = "MONSTER" }
        add { caption = "TREASURE" }

        holder.innerDepth = 4
        holder.captionFontSize = 4
        add(false) { caption = "SPELL" }

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

        holder.innerDepth = 8
        add { caption = "ENCOUNTER EXTRA" }
        add { caption = "MONSTER EXTRA" }
        add { caption = "TREASURE EXTRA" }
        add { caption = "TRAP EXTRA" }

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
        double floor = wall
        double oneLayer = 2.4
        return thinBigSquareHolder(wall, floor, oneLayer * 3)
    }

    static bigTilesHolder() {
        double internalWidth = BIG_SQUARE + BIG_SQUARE_NO_PAD
        double internalHeight = BIG_SQUARE
        double tokensDepth = 19
        double wall = 0.8
        double floor = wall
        double tilesDepth = 15

        double statusWidth = 29
        double statusHeight = 79
        def statuses = outlineXYCube(
                rcube(statusWidth, statusHeight, tokensDepth, 8, true, false, false, true),
                wall
        ) - cube(wall, statusHeight / 2, tokensDepth).dxy(statusWidth, -wall)

        double healingSurgeSize = 53
        def healingSurge = cube(healingSurgeSize + wall, healingSurgeSize + wall, tokensDepth) -
                rcube(healingSurgeSize, healingSurgeSize, tokensDepth, 16, false, false, true, true)

        def hp5Width = 40
        def hp5Height = 28
        def hp5 = outlineXYCube(rcube(hp5Width, hp5Height, tokensDepth, 8, false, false, true, true), wall) -
                cube(wall, 16, tokensDepth).dx(hp5Width)

        def dmgWidth = 23
        def dmgHeight = 18
        def dmg = outlineXYCube(cube(dmgWidth, dmgHeight, tokensDepth), wall)

        def stunWidth = 30
        def stunHeight = 18
        def stun = outlineXYCube(cube(stunWidth, stunHeight, tokensDepth), wall)

        def statuses1 = statuses
        def statuses2 = statuses.rcz(180).dxy(statusWidth + wall, internalHeight - statusHeight)
        def healing1 = healingSurge.dx((statusWidth + wall) * 2)

        def chestWidth = 20
        def chestHeight = 20
        def chest = outlineXYCube(cube(chestWidth, chestHeight, tokensDepth), wall) - cube(chestWidth * 2, 13, tokensDepth).dxy(-wall, -wall)

        def stun1 = stun.dxy(internalWidth - stunWidth, internalHeight - stunHeight)
        def stun2 = stun1.dx(-stunWidth - wall)
        def stunPair = (stun1 + stun2 -
                cube(stunWidth, stunHeight / 2, tokensDepth)
                        .dxyBy(-0.5)
                        .dxy(stun1.minX + wall / 2, stun1.minY + wall / 2)
        )
        def stunTrio = (stunPair + stun1.dx(stunWidth + wall)).endXY().dxy(internalWidth + wall, internalHeight + wall)

        def dmg1 = dmg.rz(90).dxy(internalWidth, internalHeight - dmgWidth)
        def dmg2 = dmg1.dy(-dmgWidth - wall)
        def dmgPair = (dmg1 + dmg2 -
                cube(dmgHeight / 2, dmgWidth, tokensDepth)
                        .dxyBy(-0.5)
                        .dxy(dmg1.minX + wall / 2, dmg1.minY + wall / 2)
        )
        def dmgPair1 = dmgPair.rz(-90).startXY().stripeX(2).dxy(healing1.maxX - wall, healing1.minY - wall)

        def tilesStroke = extrude(bigTilesHolderStroke(), tilesDepth)
        def tilesHolder = outlineXYCube(tilesStroke, wall, 4).dxy(wall).start()
        def tokensHolder = rcube(tilesHolder.width, tilesHolder.height, tokensDepth + floor, 4)
        def tokens = union {
            add statuses1
            add statuses2
            add healing1
            add hp5.dxy((statusWidth + wall) * 2, healingSurgeSize + wall)
            add stunTrio - cube(stunWidth / 2, stunHeight, tokensDepth).endX().dxy(stunTrio.maxX, stunTrio.minY)
            add chest.endY().dxy((statusWidth + wall) * 2, internalHeight)
            add dmgPair1
        }


        def bottom = tokensHolder - (cube(internalWidth, internalHeight, tokensDepth) - tokens).center().dzBy(0.5).dxyz(tokensHolder.centerX, tokensHolder.centerY, floor)
        return bottom + tilesHolder.dz(bottom.getMaxZ()) - cylinder(tilesDepth + floor * 8, 24).center().dzBy(-0.5).dz(floor + tilesDepth + tokensDepth)
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
