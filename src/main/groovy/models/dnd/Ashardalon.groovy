package models.dnd

import com.github.artyomcool.cadabro.MiniHolder
import com.github.artyomcool.cadabro.d3.CADObject3D
import com.github.artyomcool.cadabro.d3.CADObjects
import javafx.scene.paint.Color

class Ashardalon {
    static CADObject3D miniHolderBig() {
        def tw = 175
        def th = 175

        def big = new MiniHolder(84, 84, 72).tap {
            holdThickness = 2
            cutDelta = 1
            bridgeSize = 8
        }

        def mid = new MiniHolder(56, 57, 48.5).tap {
            holdThickness = 1.6
            cutDelta = 1
            bridgeSize = 6
        }

        def small = new MiniHolder(35, 35, 25).tap {
            bridgeSize = 3
            extraShift = 0.5
        }

        def tiny = new MiniHolder(28, 30, 20).tap {
            bridgeSize = 2
            extraShift = 0.5
        }

        def figures = []

        figures << [x: 0, y: 0, r: 0, m: big.connectRight(mid)]
        figures << [x: big.width, y: 0, r: 0, m: mid.connectLeft(big).connectRight(0, 0, true)]
        figures << [x: big.width, y: mid.height, r: 180, m: mid.connectRight(tiny).connectTop(small).connectLeft(0, 0, true)]

        figures << [x: tw - small.height, y: 0, r: 90, m: small]
        figures << [x: tw - small.height, y: small.height, r: 90, m: small]
        figures << [x: tw - small.height, y: small.height * 2, r: 90, m: small.connectRight(tiny)]
        figures << [x: tw - small.height, y: small.height * 3, r: 90, m: tiny {
            width = small.width
            height = small.height
        }.connectLeft(small).connectRight(0, 0, true)]

        figures << [x: 0, y: big.height, r: 180, m: tiny]
        figures << [x: tiny.width, y: big.height, r: 180, m: tiny]
        figures << [x: tiny.width * 2, y: big.height, r: 180, m: tiny { extraCorners = 3 }.connectLeft(mid)]

        figures << [x: 0, y: th - small.height, r: 180, m: small]
        figures << [x: small.height, y: th - small.height, r: 180, m: small]
        figures << [x: small.height * 2, y: th - small.height, r: 180, m: small]
        figures << [x: small.height * 3, y: th - small.height, r: 180, m: small]
        figures << [x: small.height * 4, y: th - small.height, r: 180, m: small]

        figures << [x: small.height * 0, y: mid.height * 2, r: 0, m: small { width *= 2 }]
        figures << [x: big.width - tiny.width, y: mid.height * 2, r: 0, m: small { width = tiny.width;
            extraCorners = 1 }.connectTop(tiny).connectRight(small).connectRightTop(mid)]
        figures << [x: big.width, y: mid.height * 2, r: 0, m: small { width = mid.width;
            deltaX = -7 }.connectTop(mid).connectRight(0, 0, true)]

        def move = (f, m) -> {
            m.dxy(-f.m.width / 2, -f.m.height / 2).rz(f.r).dxy(f.m.width / 2, f.m.height / 2).dxy(f.x, f.y)
        }

        (
                CADObjects.union {
                    add CADObjects.cube(tw, th, 1).color(Color.GREEN)
                    for (def f in figures) {
                        add move(f, f.m.renderStrengthWall())
                    }
                } - CADObjects.union {
                    for (def f in figures) {
                        add move(f, f.m.renderCuts())
                    }
                } + CADObjects.union {
                    for (def f in figures) {
                        add move(f, f.m.renderHolder())
                    }
                } + CADObjects.diff {
                    add CADObjects.extrude(CADObjects.rsquare(tw, th), 70)
                    add CADObjects.extrude(CADObjects.rsquare(tw - 2, th - 2), 70).dxy(1, 1)
                    add CADObjects.cube(tw, th, 70).dxyz(16, 16, 16)
                } & CADObjects.extrude(CADObjects.rsquare(tw, th), 70)
        ).tap {
            println bounds().size()
        }
    }
}
