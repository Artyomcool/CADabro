package models.mars

import com.github.artyomcool.cadabro.RenderCollection
import com.github.artyomcool.cadabro.d3.CADObject3D
import com.github.artyomcool.cadabro.d3.CADObjects
import com.github.artyomcool.cadabro.d3.Union
import javafx.scene.paint.Color
import models.common.CardHolder
import models.dnd.Drizzt

import static com.github.artyomcool.cadabro.d2.CADObject2D.text
import static com.github.artyomcool.cadabro.d3.CADObjects.*
import static com.github.artyomcool.cadabro.d3.CADObjects.polygon

class Mars {

    static render() {
        return new RenderCollection().tap {
            CADObject3D marker = rrcube(8.8, 0.4)
            double tinyBorder = 0.4
            double smallBorder = 0.8
            double bigBorder = 1.2

            def from_0_to_7 = markersHolder(marker, smallBorder, 8)
            def from_8_to_10 = markersHolder(marker, smallBorder, 3)
            def from_m5_to_2 = markersHolder(marker, smallBorder, 8)
            def from_3_to_10 = markersHolder(marker, smallBorder, 8)


            def moneyHolder = markersHolder(from_m5_to_2, from_3_to_10, from_m5_to_2.width, bigBorder, smallBorder)
            def steelHolder = markersHolder(from_0_to_7, from_8_to_10, from_0_to_7.width, bigBorder, smallBorder).dx(moneyHolder.maxX - bigBorder)
            def titanHolder = steelHolder.dx(steelHolder.width - bigBorder)
            def electricHolder = steelHolder.dx(0)
            def plantHolder = electricHolder.dx(-(steelHolder.width - bigBorder))
            def heatHolder = electricHolder.dx((steelHolder.width - bigBorder))

            def topUnion = union(moneyHolder, steelHolder, titanHolder)
            def bottomUnion = union(plantHolder, electricHolder, heatHolder).dy(moneyHolder.height - bigBorder)

            def player = union(topUnion, bottomUnion)

            def storageTotalHeight = 12
            def innerMarkers = markersHolderInner(storageTotalHeight, tinyBorder)
            def outerMarkers = hh(innerMarkers.width - tinyBorder * 2, innerMarkers.height - tinyBorder * 2, storageTotalHeight, bigBorder)
            def markersStorage = union(innerMarkers.dxy(bigBorder-tinyBorder), outerMarkers)
            markersStorage -= rcube(markersStorage.width - 20, 8, markersStorage.height, 4)
                    .rx(-90)
                    .dz(storageTotalHeight+0.4+4)
                    .centerX()
                    .dx(markersStorage.centerX)

            def hull = hull(markersStorage)
            def cut = hull.offset(0.5).dxyz(0.5)
            def cover = hull.offset(0.5 + bigBorder).dxyz(0.5 + bigBorder)

            def markersCover = perforate(cover - cut.dxy(bigBorder).dz(bigBorder) - cube(cover.width, cover.height, cover.depth - 6), 3, 0.8, 4)
            double markersStorageTotalDepth = markersCover.maxZ
            for (int i = 0; i < 1; i++) {
                add markersCover.dy(i.intdiv(3) * markersCover.height).dz((i%3) * markersStorageTotalDepth)
                add markersStorage.dxy(0.5 + bigBorder).dy(i.intdiv(3) * markersCover.height).dz((i%3) * markersStorageTotalDepth)
            }

            double cardWidth = 90
            double cardHeight = 65
            double cardDepth1 = 40
            double cardDepth2 = 21
            double cardDepth3 = 8
            def ch1 = ch(cardWidth, cardHeight, cardDepth1, bigBorder)
            def ch2 = ch(cardWidth, cardHeight, cardDepth2, bigBorder)
            def ch3 = ch(cardWidth, cardHeight, cardDepth3, bigBorder)

            add ch1.dy(markersCover.height * 2)
            add ch3.dy(markersCover.height * 2).dz(cardDepth1)
            add ch2.rz(90).dx(ch2.height).dy(markersCover.height).dz(markersStorageTotalDepth*2)

            for (int i = 0; i < 5; i++) {
                //add player.rz(90).dx(ch1.totalWidth + player.height).dz(i * (player.depth + 0.2))
            }

            def rules = cube(220, 260, 4).dz(60)
            //add rules, true

            add hexHolders().rz(90).start().endX().dx(280)
        }.tap {
            add(cube(280, 280, 65), true, true)
        }
    }

    private static CADObject3D hexHolders() {
        double hexInnerWidth = 42
        return union {
            add hexHolder(42, hexInnerWidth, 1, 0)
            add hexHolder(42, hexInnerWidth, 1, 60).dxy(32-0.66666+1, 21-0.5-2)
            add hexHolder(42, hexInnerWidth, 1, -120).dxy(32-0.66666+1, -(21-0.5-2))
            add hexHolder(42, hexInnerWidth, 1, 180).dx((32-0.66666+1)*2)
        }
    }

    private static CADObject3D hexHolder(double h, double d, double border, double rotate) {
        def base = polygon(6, h + border, d / 2 + border).centerXY() -
                polygon(6, h, d/2).centerXY().dz(border)
        def originalBounds = base.bounds().min
        base -= cylinder(h + border, d / 2.5).centerX()

        return base.rz(rotate).dxy(-originalBounds.x, -originalBounds.y)
    }

    private static CADObject3D perforate(CADObject3D obj, double r, double minOffset, double borderOffset) {
        double sx = obj.width - borderOffset * 2
        double sy = obj.height - borderOffset * 2
        int xc = (int) ((sx + minOffset) / (r*2 + minOffset))
        int yc = (int) ((sy+ minOffset) / (r*2 + minOffset))
        double xOffset = (sx-r*2) / (xc-1)
        double yOffset = (sy-r*2) / (yc-1)

        return diff {
            add obj
            for (int x = 0; x < xc; x++) {
                for (int y = 0; y < yc; y++) {
                    add cylinder(obj.depth, r)
                            .dxy(borderOffset + xOffset * x, borderOffset + yOffset * y)
                            .dxyz(obj.minX, obj.minY, obj.minZ)
                }
            }
        }
    }

    private static CADObject3D markersHolderInner(double storageTotalHeight, double tinyBorder) {
        def smallMarkerSize = 7.5
        def mediumMarkerSize = 8.8
        def playerMarkerSize = mediumMarkerSize
        def bigMarkerSize = 10.7

        def smallMarkerCount = 33
        def mediumMarkerCount = 6
        def playerMarkerCount = 40
        def bigMarkerCount = 9

        def storageInnerHeight = storageTotalHeight / 3

        def playerMarkersHeight = 100
        def mediumMarkersHeight = 26
        def bigMarkers = markersContainer(
                11,
                playerMarkersHeight,
                storageInnerHeight,
                tinyBorder,
                bigMarkerSize,
                bigMarkerCount
        )
        def playerMarkers = markersContainer(
                36,
                playerMarkersHeight,
                storageInnerHeight,
                tinyBorder,
                playerMarkerSize,
                playerMarkerCount
        )
        def mediumMarkers = markersContainer(
                31,
                mediumMarkersHeight,
                storageInnerHeight,
                tinyBorder,
                mediumMarkerSize,
                mediumMarkerCount
        )
        def smallMarkers = markersContainer(
                31,
                playerMarkersHeight - mediumMarkersHeight - tinyBorder,
                storageInnerHeight,
                tinyBorder,
                smallMarkerSize,
                smallMarkerCount
        )
        bigMarkers = bigMarkers.dx(smallMarkers.maxX - tinyBorder)

        return CADObjects.union(
                bigMarkers,
                mediumMarkers.dy(smallMarkers.maxY - tinyBorder),
                playerMarkers.dx(bigMarkers.maxX - tinyBorder),
                smallMarkers
        )
    }

    static CADObject3D markersContainer(double width, double height, double depth, double border, double markerSize, int markersCount) {
        def r = [hh(width, height, depth, border)]
        double x = 0.1
        double y = 0.1
        for (int i = 0; i < markersCount; i++) {
            r += cube(markerSize).dxyz(x, y, 0.5).color(Color.color(0.2, 1, 0.7, 0.5))
            x += markerSize + 0.2
            if (x + markerSize >= width) {
                x = 0.1
                y += markerSize + 0.2
            }
        }
        return r[0] // union(r.toArray(CADObject3D[]::new))
    }

    static CADObject3D hh(double width, double height, double depth, double border) {
        return new CardHolder(
                width,
                height,
                depth
        ).tap {
            floorThickness = 0.4
            borderThickness = border
            outerRadius = 0.8
            innerRadius = 0.4
        }.base()
    }

    static CADObject3D ch(double width, double height, double depth, double border) {
        return new CardHolder(
                width,
                height,
                depth
        ).tap {
            floorThickness = 0.4
            borderThickness = border
            outerRadius = 0.8
            innerRadius = 0.4
        }.withCut()
    }

    static CADObject3D markersHolder(CADObject3D top, CADObject3D bottom, double width, double bigBorder, double tinyBorder) {
        def holderBase = hh(
                width - tinyBorder * 2,
                top.height * 5,
                top.depth - 0.4,
                bigBorder
        )

        return holderBase +
                top.dxy(bigBorder - tinyBorder) +
                bottom.dx(bigBorder - tinyBorder).dy(top.maxY - (bigBorder - tinyBorder))
    }

    static CADObject3D markersHolder(CADObject3D marker, double border, int count) {
        def markers = union {
            count.times { x->
                add marker.dx(x * (marker.width + border))
            }
        }
        def markersHolder = rcube(
                border + markers.width + border,
                border + markers.height + border,
                marker.depth * 0.45, 0.4
        )
        return markersHolder - markers.dxyz(border)
    }


}
