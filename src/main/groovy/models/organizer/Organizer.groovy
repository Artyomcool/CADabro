package models.organizer

import com.github.artyomcool.cadabro.RenderCollection

import static com.github.artyomcool.cadabro.d3.CADObjects.*

class Organizer {
    static double thickness = 1.2
    static double lockersOffset = 5

    static double oneDepth = 120
    static double oneWidth = 120
    static double oneHeight = 60
    static double totalHeight = 240

    static render() {
        return new RenderCollection().tap {
            add cableRing()
            //add box(1, 1, 1, 1, 1)
            //add container(oneWidth, oneHeight)
            //add box(2, 2, 1, 1)
//            add complexBox(
//                    [1, 1, 1],
//                    [1, 1, 1],
//                    [1, 1, 1],
//                    [1, 1, 1],
//            )
            //add container(oneWidth * 2, oneHeight)
            //add container(oneWidth, oneHeight - thickness)
            //add container(oneWidth, oneHeight * 2)
        }
    }

    static cableRing() {
        double r = 8
        double rc = 6
        double thickness = 2
        double h = 10
        double cut = 0.4
        diff {
            add cylinder(h, r).centerXY()
            add cylinder(h, r - thickness).centerXY()
            add cube(cut, r, h).centerX()
            add (rcut(rc, r).rx(90).centerY().dx(-rc).dy(r).dz(h - rc).rcz(12) & cube(r, r, h).dx(-r))
        }
    }

    static box(int w, int... hs) {

        int hPseudoSectionsCount = (int)(totalHeight/oneHeight)
        double totalWidth = oneWidth * w
        double totalDepth = oneDepth

        diff {
            add union {
                add cube(totalWidth, totalHeight, totalDepth) -
                        cube(totalWidth - thickness * 2, totalHeight - thickness * 2, totalDepth - thickness).dxyz(thickness)
                int th = 0
                add sphere(thickness).center().dx(thickness).dy(th * oneHeight + thickness).dz(thickness + lockersOffset)
                add sphere(thickness).center().dx(totalWidth - thickness).dy(th * oneHeight + thickness).dz(thickness + lockersOffset)
                for (int i = 0; i < hs.length - 1; i++) {
                    int h = hs[i]
                    th += h
                    add cube(totalWidth, thickness, totalDepth).dy(th * oneHeight)
                    add sphere(thickness).center().dx(thickness).dy(th * oneHeight + thickness).dz(thickness + lockersOffset)
                    add sphere(thickness).center().dx(totalWidth - thickness).dy(th * oneHeight + thickness).dz(thickness + lockersOffset)
                }
            }

            for (int i = 0; i < hPseudoSectionsCount; i++) {
                double hp = i * oneHeight + oneHeight / 2
                add rcut(4, totalWidth).ry(90).rx(180).dy(hp+4).dz(totalDepth-4)
                add rcut(4, totalWidth).ry(90).rx(90).dy(hp-4).dz(totalDepth-4)
                add cube(totalWidth, 2, 5).dy(hp - 1).dz(totalDepth - 5)
                add cylinder(totalWidth, 1.2).dy(hp-1.2).ry(90).dz(totalDepth - 2)

                add rcut(4, totalWidth).ry(90).rx(0).dy(hp-4).dz(thickness + 4)
                add rcut(4, totalWidth).ry(90).rx(-90).dy(hp+4).dz(thickness + 4)
                add cube(totalWidth, 2, 5).dy(hp - 1).dz(thickness)
                add cylinder(totalWidth, 1.2).dy(hp-1.2).ry(90).dz(2.4 + 2 + thickness)

                add cylinder(thickness, 4).dx(totalWidth - 4).dy(hp-4)
                add cylinder(thickness, 4).dx(-4).dy(hp-4)
            }
        }
    }

    static complexBox(List<Integer>... wh) {
        int hPseudoSectionsCount = (int)(totalHeight/oneHeight)
        int ttw = 0
        for (int i = 0; i < wh[0].size() - 1; i++) {
            ttw += wh[0][i]
        }
        double totalWidth = oneWidth * ttw
        double totalDepth = oneDepth

        diff {
            add union {
                add cube(totalWidth, totalHeight, totalDepth) -
                        cube(totalWidth - thickness * 2, totalHeight - thickness * 2, totalDepth - thickness).dxyz(thickness)
                int th = 0
                add sphere(thickness).center().dx(thickness).dy(th * oneHeight + thickness).dz(thickness + lockersOffset)
                add sphere(thickness).center().dx(totalWidth - thickness).dy(th * oneHeight + thickness).dz(thickness + lockersOffset)

                for (int i = 0; i < wh.length; i++) {
                    int h = wh[i].last
                    int tw = 0
                    for (int j = 0; j < wh[i].size() - 1; j++) {
                        add sphere(thickness).center().dx(thickness + oneWidth * tw).dy(th * oneHeight + thickness).dz(thickness + lockersOffset)
                        if (j > 0) {
                            double s = 3
                            add cube(thickness * 2, s, totalDepth).dx(oneWidth * tw - thickness).dy(th * oneHeight)
                            add cube(thickness * 2, s, totalDepth).dx(oneWidth * tw - thickness).dy((th + h) * oneHeight - s)

                            add cube(0.15, h * oneHeight, totalDepth).dx(oneWidth * tw).dy(th * oneHeight)
                        }
                        tw += wh[i][j]
                        add sphere(thickness).center().dx(oneWidth * tw - thickness).dy(th * oneHeight + thickness).dz(thickness + lockersOffset)
                    }
                    th += h
                    if (i < wh.length - 1) add cube(totalWidth, thickness, totalDepth).dy(th * oneHeight)
                }
            }

            for (int i = 0; i < hPseudoSectionsCount; i++) {
                double hp = i * oneHeight + oneHeight / 2
                add rcut(4, totalWidth).ry(90).rx(180).dy(hp+4).dz(totalDepth-4)
                add rcut(4, totalWidth).ry(90).rx(90).dy(hp-4).dz(totalDepth-4)
                add cube(totalWidth, 2, 5).dy(hp - 1).dz(totalDepth - 5)
                add cylinder(totalWidth, 1.2).dy(hp-1.2).ry(90).dz(totalDepth - 2)

                add rcut(4, totalWidth).ry(90).rx(0).dy(hp-4).dz(thickness + 4)
                add rcut(4, totalWidth).ry(90).rx(-90).dy(hp+4).dz(thickness + 4)
                add cube(totalWidth, 2, 5).dy(hp - 1).dz(thickness)
                add cylinder(totalWidth, 1.2).dy(hp-1.2).ry(90).dz(2.4 + 2 + thickness)

                add cylinder(thickness, 4).dx(totalWidth - 4).dy(hp-4)
                add cylinder(thickness, 4).dx(-4).dy(hp-4)
            }

            double hh = oneHeight / 2
            for (def l : wh) {
                double ww = oneWidth / 2
                for (def w : l.subList(0, l.size() - 1)) {
                    add cylinder(thickness, 16).centerXY().dxy(ww, hh)
                    ww += w * oneWidth
                }
                hh += l.last() * oneHeight
            }
        }
    }

    static container(double w, double h) {
        double m = 0.8
        w -= 0.2 + thickness * 2
        double d = h - 0.4 - thickness * 2
        h = oneDepth - 0.6 - thickness
        def c = rcube(w, h, d, 2)
        diff {
            add union {
                add c - rcube(w - thickness * 2, h - thickness * 2, d - thickness, 1).dxyz(thickness)
                for (double th = oneHeight / 2; th < d; th += oneHeight) {
                    add cylinder(h, 14).rx(-90).centerX().dx(-8).dz(14 + th)
                    add cylinder(h, 14).rx(-90).centerX().dx(w+8).dz(14 + th)
                }
                for (double tw = oneWidth - thickness; tw < w; tw += oneWidth) {
                    add cube(thickness * 2 + m * 2 * 2, h, 3 + m * 2).dx(tw - thickness - m * 2)
                }
            }
            for (double tw = oneWidth - thickness; tw < w; tw += oneWidth) {
                add cube(thickness * 2 + m * 2, h, 3 + m).dx(tw - thickness - m)
            }
            for (double th = oneHeight / 2; th < d; th += oneHeight) {
                add cylinder(h, 14 - thickness).rx(-90).centerX().dx(-8).dz(14 - thickness + th)
                add cylinder(h, 14 - thickness).rx(-90).centerX().dx(w+8).dz(14 - thickness + th)
            }

            add cube(thickness*2).center().dy(lockersOffset)
            add cube(thickness*2).center().dx(w).dy(lockersOffset)
            add cylinder(h, 10).rx(-90).centerX().dx(w/2).dz(d + 10)
        } & c
    }

}
