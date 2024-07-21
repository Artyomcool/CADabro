package com.github.artyomcool.cadabro

import com.github.artyomcool.cadabro.d3.Hull
import groovy.transform.AutoClone
import org.apache.commons.geometry.euclidean.twod.Vector2D

import static com.github.artyomcool.cadabro.d2.CADObject2D.draw
import static com.github.artyomcool.cadabro.d3.CADObjects.*

@AutoClone
class MiniHolder {

    double width
    double height
    double diameter

    double holdHeight = 2.4
    double holdThickness = 0.8
    double cutDelta = 1.8
    double extraShift = 1
    double extraCorners = 5
    double bgHeight = 1
    double trampoline = 0
    double cutAngle = 20
    double deltaX = 0
    double bridgeSize = 2

    Double strengthWallSize = null

    Vector2D leftSize
    Vector2D leftTopSize
    Vector2D rightSize
    Vector2D rightTopSize
    Vector2D topSize

    boolean leftForce
    boolean rightForce

    MiniHolder(double width, double height, double diameter) {
        this.width = width
        this.height = height
        this.diameter = diameter
    }

    MiniHolder call(@DelegatesTo(MiniHolder) Closure closure) {
        clone().tap(closure)
    }

    MiniHolder connectLeft(MiniHolder holder) {
        connectLeft(holder.strengthWall, holder.fullHeight - holder.bgHeight)
    }

    MiniHolder connectLeftTop(MiniHolder holder) {
        connectLeftTop(holder.strengthWall, holder.fullHeight - holder.bgHeight)
    }

    MiniHolder connectRight(MiniHolder holder) {
        connectRight(holder.strengthWall, holder.fullHeight - holder.bgHeight)
    }

    MiniHolder connectRightTop(MiniHolder holder) {
        connectRightTop(holder.strengthWall, holder.fullHeight - holder.bgHeight)
    }

    MiniHolder connectTop(MiniHolder holder) {
        connectTop(holder.strengthWall, holder.fullHeight - holder.bgHeight)
    }

    MiniHolder connectLeft(double x, double y, boolean force = false) {
        call {
            leftSize = Vector2D.of(x, y)
            leftForce = force
        }
    }

    MiniHolder connectLeftTop(double x, double y) {
        call {
            leftTopSize = Vector2D.of(x, y)
        }
    }

    MiniHolder connectRight(double x, double y, boolean force = false) {
        call {
            rightSize = Vector2D.of(x, y)
            rightForce = force
        }
    }

    MiniHolder connectRightTop(double x, double y) {
        call {
            rightTopSize = Vector2D.of(x, y)
        }
    }

    MiniHolder connectTop(double x, double y) {
        call {
            topSize = Vector2D.of(x, y)
        }
    }

    def render() {
        renderBg() + renderStrengthWall() - renderCuts() + renderHolder() + renderTrampoline()
    }

    def renderBg() {
        cube(width, height, bgHeight)
    }

    def renderHolder() {
        double radiusExternal = diameterExternal / 2
        double radiusTop = diameterTop / 2
        double radiusBottom = diameterBottom / 2

        dxy(deltaX + width / 2, extraShift + radiusExternal)[
                extrudeRotate(
                        360 - cutAngle,
                        draw(radiusTop, -(bgHeight + holdHeight))
                                .dy(-topSmoothRadius)
                                .smooth(topSmoothRadius, 4)
                                .dx(topSmoothRadius)
                                .dy(fullHeight)
                                .dx(radiusBottom - radiusExternal)
                                .dy(-bgHeight)
                                .close()
                ).rx(-90).rz(90 + cutAngle / 2)
        ]
    }

    def renderCuts() {
        def bottomCut = cylinder(bgHeight, diameterCut / 2)
                .dxBy(-0.5)
                .dxy(deltaX + width / 2, extraShift)
        def bottomHold = cylinder(bgHeight, diameterTop / 2)
                .dxBy(-0.5)
                .dxy(deltaX + width / 2, extraShift)
        def bottomBridge = cube(bridgeSize, diameterCut, bgHeight)
                .dxBy(-0.5)
                .dxy(deltaX + width / 2, extraShift)

        ~renderHolder() + bottomCut - bottomHold - bottomBridge - cube(width, strengthWall, bgHeight)
    }

    def renderStrengthWall() {
        if (leftSize == null && rightSize == null && topSize == null && leftTopSize == null && rightTopSize == null) {
            cube(width, strengthWall, fullHeight - bgHeight).dz(bgHeight)
        } else {
            double lX = 0
            double leX = extraCorners
            double rX = width
            double reX = width - extraCorners
            double tY = 0
            double teY = extraShift
            double bY = strengthWall
            double z = fullHeight - bgHeight

            double ltX = lX
            double ltY = tY
            double ltZ = leftForce ? leftSize.y : avg(z, leftSize?.y, topSize?.y, leftTopSize?.y)

            double lbX = lX
            double lbY = leftForce ? leftSize.x : avg(bY, leftSize?.x)
            double lbZ = leftForce ? leftSize.y : avg(z, leftSize?.y)

            double letX = leX
            double letY = teY
            double letZ = z

            double lebX = leX
            double lebY = bY
            double lebZ = z

            double rtX = rX
            double rtY = tY
            double rtZ = rightForce ? rightSize.y : avg(z, rightSize?.y, topSize?.y, rightTopSize?.y)

            double rbX = rX
            double rbY = rightForce ? rightSize.x : avg(bY, rightSize?.x)
            double rbZ = rightForce ? rightSize.y : avg(z, rightSize?.y)

            double retX = reX
            double retY = teY
            double retZ = z

            double rebX = reX
            double rebY = bY
            double rebZ = z

            def lh = Hull.of(
                    ltX, ltY, 0,
                    ltX, ltY, ltZ,
                    lbX, lbY, 0,
                    lbX, lbY, lbZ,

                    letX, letY, 0,
                    letX, letY, letZ,
                    lebX, lebY, 0,
                    lebX, lebY, lebZ,
            )

            def rh = Hull.of(
                    rtX, rtY, 0,
                    rtX, rtY, rtZ,
                    rbX, rbY, 0,
                    rbX, rbY, rbZ,

                    retX, retY, 0,
                    retX, retY, retZ,
                    rebX, rebY, 0,
                    rebX, rebY, rebZ,
            )

            def th = Hull.of(
                    ltX, ltY, 0,
                    ltX, ltY, ltZ,
                    letX, letY, 0,
                    letX, letY, letZ,

                    rtX, rtY, 0,
                    rtX, rtY, rtZ,
                    retX, retY, 0,
                    retX, retY, retZ,
            )

            return (lh + th + rh + cube(width - extraCorners * 2, strengthWall - extraShift, z).dxy(extraCorners, extraShift)).dz(bgHeight)
        }
    }

    def renderTrampoline() {
        if (trampoline) {
            dxy(deltaX + width / 2, extraShift + diameterExternal)[
                    ~(
                            cube(diameterTop, 0.01, bgHeight + trampoline)
                                    .center(true, false, false)
                                    .end(false, true, false) +
                            cube(diameterTop, 0.01, bgHeight)
                                    .center(true, false, false)
                                    .start(false, true, false)
                                    .dy(-diameterExternal)
                    ) & cylinder(bgHeight + trampoline, diameterTop / 2, diameterTop / 2 - 1).center(true, true, false).dy(-diameterExternal/2)
            ]
        } else {
            union()
        }
    }

    private static double avg(Double... doubles) {
        int c = 0
        double a = 0

        for (def d in doubles) {
            if (d != null) {
                c++
                a += d
            }
        }

        return a / c
    }

    private double getFullHeight() {
        bgHeight + holdHeight + topSmoothRadius
    }

    private double getTopSmoothRadius() {
        (diameterExternal - diameterTop) / 2
    }

    double getDiameterTop() { diameter - 0.5 }

    double getDiameterBottom() { diameter + 1 }

    double getDiameterExternal() { diameterBottom + holdThickness * 2 }

    double getDiameterCut() { diameterExternal + cutDelta * 2 }

    double getStrengthWall() { strengthWallSize ?: diameterExternal / 3.5 }

}
