package models.hex_race

import com.github.artyomcool.cadabro.RenderCollection
import com.github.artyomcool.cadabro.d3.CADObject3D

import static com.github.artyomcool.cadabro.d2.CADObject2D.text
import static com.github.artyomcool.cadabro.d3.CADObjects.*

class HexRace {

    static render() {
        return new RenderCollection().tap {
            //add tile(["0", "1", "1", "2", "1", "1", "0"])
            //add holderA()
            //add hex("1")

            add new Hexes()
                    .hex("0", -1, 1)
                    .hex("1", 0, 0)
                    .hex("2", 1, 0)
                    .hex("3", 2, -1)
                    .hex("2", 3, -1)
                    .hex("1", 4, -2)
                    .hex("0", 5, -2)
                    .build()
        }
    }

    static tile(List<String> texts) {
        hexhex([
                hex(texts[0], false, false, true, true, true, false),
                hex(texts[1], false, true, true, true, false, false),
                hex(texts[2], false, false, false, true, true, true),
                hex(texts[3], false, false, false, false, false, false),
                hex(texts[4], true, true, true, false, false, false),
                hex(texts[5], true, false, false, false, true, true),
                hex(texts[6], true, true, false, false, false, true)
        ])
    }

    static hexhex(List<CADObject3D> hexes) {
        def hexCenter = hexes[3]
        double w = hexCenter.w
        double h = hexCenter.h

        hexes[0].dy(-h) +
                hexes[1]
                        .dxy(-w * 0.75, -h / 2) +
                hexes[2]
                        .dxy(w * 0.75, -h / 2) +
                hexes[3] +
                hexes[4]
                        .dxy(-w * 0.75, h / 2) +
                hexes[5]
                        .dxy(w * 0.75, h / 2) +
                hexes[6].dy(h)
    }

    static holder() {
        def wall = 0.8
        def extra = 0.6
        def hex = _hex(8, 8, 1)
        def totalHeight = hex.height * 3

        def hh = hexhex([hex, hex, hex, hex, hex, hex, hex])

        hh.scale(1 + (wall + extra) * 2 / totalHeight) - hh.scale(1 + extra * 2 / totalHeight).dz(0.4)
    }

    static holderA() {
        def h = holder()
        h +
                h.dx(h.height / 6 * 5 + 1.4).dy(h.height / 6) +
                h.dx(-(h.height / 6 * 5 + 1.4)).dy(-h.height / 6) +
                h.dx((h.height / 6 * 5 + 1.4) * 2).dy((h.height / 6) * 2) +
                h.dx(-(h.height / 6 * 5 + 1.4) * 2).dy((-h.height / 6) * 2) +
                h.dx((h.height / 6 * 5 + 1.4) * 3).dy((h.height / 6) * 3) +
                h.dx(-(h.height / 6 * 5 + 1.4) * 3).dy((-h.height / 6) * 3)
    }

    private static hex(String _text, boolean ... locks) {
        double hh = 1.6
        def hex = _hex(10, 8, hh)
        def r = union(hex)

        def drawLock = { double diameter, double thickness, double depth = 2, double z ->
            cube(thickness, depth, z).center(true, false, false) +
                    cylinder(z, diameter / 2).center(true, true, false).dy(depth)
        }

        def lock = drawLock(3, 2, hh - 0.6)

        for (int i = 0; i < 6; i++) {
            if (locks[i]) r << lock.dxy(1.5, hex.maxY).rz(360 / 6 * i)
        }

        r = diff(r)
        lock = drawLock(3.4, 2.4, hh)
        for (int i = 0; i < 6; i++) {
            if (locks[i]) {
                r << lock.rz(180).dxy(-1.5, hex.maxY).rz(360 / 6 * i)
            } else {
                r << lock.rz(180).dxyz(-1.5, hex.maxY, hh - 0.6).rz(360 / 6 * i)
            }
        }
        r << cylinder(0.6, 6, 6)
                .center(true, true, false)
                .end(false, false, true)
                .dz(hh)

        if (_text != "") {
            def t = extrude(text(_text, null), 0.2)
            r += t.center(true, true, false).dz(hh - 0.6)
        }

        r.metaClass.w = hex.width
        r.metaClass.h = hex.height
        return r
    }

    private static _hex(double rb, double rt = rb, double h = 0.6) {
        def base = polygon(6, h - 0.6, rb)
        if (rt != rb) base += polygon(6, 0.6, rb, rt).dz(h - 0.6)
        return base
    }

    static class Hexes {
        Map<Long, String> hexes = [:]

        def hex(String name, int x, int y) {
            hexes[((long) x) << 32L | ((long) y) & 0xffffffffL] = name
            return this
        }

        def build() {
            def r = union()
            for (def e in hexes.entrySet()) {
                int x = (int) (e.key >> 32L)
                int y = (int) e.key
                boolean isOdd = (x & 1)
                def he = isOdd
                        ?
                        hex(
                                e.value,
                                !hexes.containsKey((long) (x + 0) << 32L | ((long) (y + 1) & 0xffffffffL)),
                                !hexes.containsKey((long) (x - 1) << 32L | ((long) (y + 0) & 0xffffffffL)),
                                !hexes.containsKey((long) (x - 1) << 32L | ((long) (y - 1) & 0xffffffffL)),
                                !hexes.containsKey((long) (x + 0) << 32L | ((long) (y - 1) & 0xffffffffL)),
                                !hexes.containsKey((long) (x + 1) << 32L | ((long) (y - 1) & 0xffffffffL)),
                                !hexes.containsKey((long) (x + 1) << 32L | ((long) (y + 0) & 0xffffffffL))
                        )
                        :
                        hex(
                                e.value,
                                !hexes.containsKey((long) (x + 0) << 32L | ((long) (y + 1) & 0xffffffffL)),
                                !hexes.containsKey((long) (x - 1) << 32L | ((long) (y + 1) & 0xffffffffL)),
                                !hexes.containsKey((long) (x - 1) << 32L | ((long) (y + 0) & 0xffffffffL)),
                                !hexes.containsKey((long) (x + 0) << 32L | ((long) (y - 1) & 0xffffffffL)),
                                !hexes.containsKey((long) (x + 1) << 32L | ((long) (y + 0) & 0xffffffffL)),
                                !hexes.containsKey((long) (x + 1) << 32L | ((long) (y + 1) & 0xffffffffL)),
                        )
                double w = he.w
                double h = he.h

                r << he.dxy(x * w * 0.75, y * h - (x & 1) * h / 2)
            }
            return r
        }
    }

}
