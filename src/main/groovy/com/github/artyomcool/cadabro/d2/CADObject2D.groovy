package com.github.artyomcool.cadabro.d2


import org.apache.commons.geometry.euclidean.twod.Bounds2D
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D
import org.apache.commons.geometry.euclidean.twod.Vector2D
import org.apache.commons.geometry.euclidean.twod.path.LinePath
import org.apache.commons.numbers.core.Precision

abstract class CADObject2D {
    public static final Precision.DoubleEquivalence e = Precision.doubleEquivalenceOfEpsilon(1e-8)

    private RegionBSPTree2D cachedTree
    private Bounds2D cachedBounds

    protected abstract RegionBSPTree2D toTree();

    Bounds2D bounds() {
        if (cachedBounds == null) {
            cachedBounds = asTree().getBounds()
        }
        return cachedBounds
    }

    RegionBSPTree2D asTree() {
        if (cachedTree == null) {
            cachedTree = toTree()
        }
        return cachedTree
    }

    static Drawer draw(double x = 0, double y = 0) {
        return new Drawer().tap { points.add(V.of(x, y)) }
    }

    static class Drawer {
        List<V> points = []

        Drawer l(double x, double y) {
            points.add(V.of(x, y))
            return this
        }

        Drawer lx(double x) {
            return l(x, points.last.v.y)
        }

        Drawer ly(double y) {
            return l(points.last.v.x, y)
        }

        Drawer dxy(double dx, double dy) {
            return l(points.last.v.x + dx, points.last.v.y + dy)
        }

        Drawer dx(double dx) {
            return dxy(dx, 0)
        }

        Drawer dy(double dy) {
            return dxy(0, dy)
        }

        Drawer smooth(double r = 10, int segments = 16) {
            points.last.r = r
            points.last.segments = segments
            return this
        }

        CADObject2D close() {
            List<Vector2D> result = new ArrayList<>()
            def add = (double x, double y) -> {
                result.add(Vector2D.of(x, y))
            }
            int i = 0
            for (final def vp in points) {
                if (vp.segments == 0) {
                    add(vp.v.x, vp.v.y)
                } else {
                    def next = points.get((i + 1) % points.size()).v
                    def current = vp.v
                    def prev = points[i - 1].v

                    double x1 = current.x
                    double y1 = current.y

                    double x2 = next.x
                    double y2 = next.y
                    double x3 = prev.x
                    double y3 = prev.y

                    double ABx = x2 - x1
                    double ABy = y2 - y1

                    double ACx = x3 - x1
                    double ACy = y3 - y1

                    double lenAB = Math.sqrt(ABx * ABx + ABy * ABy)
                    double lenAC = Math.sqrt(ACx * ACx + ACy * ACy)

                    double normABx = ABx / lenAB
                    double normABy = ABy / lenAB
                    double normACx = ACx / lenAC
                    double normACy = ACy / lenAC

                    double bisectX = normABx + normACx
                    double bisectY = normABy + normACy

                    double lenBisect = Math.sqrt(bisectX * bisectX + bisectY * bisectY)
                    double normBisectX = bisectX / lenBisect
                    double normBisectY = bisectY / lenBisect

                    double cosAngle = normABx * normACx + normABy * normACy
                    double sinHalfAngle = Math.sqrt((1 - cosAngle) / 2)

                    double distanceToCenter = vp.r / sinHalfAngle

                    double centerX = x1 + normBisectX * distanceToCenter
                    double centerY = y1 + normBisectY * distanceToCenter

                    double distanceToOrto = Math.sqrt(distanceToCenter * distanceToCenter - vp.r * vp.r)

                    double startX = x1 + normABx * distanceToOrto
                    double startY = y1 + normABy * distanceToOrto

                    double endX = x1 + normACx * distanceToOrto
                    double endY = y1 + normACy * distanceToOrto

                    double theta1 = Math.atan2(startY - centerY, startX - centerX)
                    double theta2 = Math.atan2(endY - centerY, endX - centerX)
                    // Корректировка угла для правильного обхода дуги
                    if (theta2 < theta1) {
                        theta2 += 2 * Math.PI;
                    }

                    // Рассчитываем угол дуги и проверяем необходимость корректировки на полный оборот
                    double dt = (theta2 - theta1);
                    if (dt > Math.PI) {
                        theta2 -= 2 * Math.PI;
                        dt = theta2 - theta1;
                    } else if (dt < -Math.PI) {
                        theta2 += 2 * Math.PI;
                        dt = theta2 - theta1;
                    }

                    System.out.println(Math.toDegrees(theta1) + " " + Math.toDegrees(theta2))
                    if (dt < 0) {
                        //dt += Math.PI * 2
                    }

                    System.out.println(Math.toDegrees(dt))
                    double deltaTheta = dt / vp.segments

                    for (int j = vp.segments; j >= 0; j--) {
                        double theta = theta1 + j * deltaTheta
                        double x = centerX + vp.r * Math.cos(theta)
                        double y = centerY + vp.r * Math.sin(theta)
                        add(x, y)
                    }
                }
                i++
            }
            return new CADObject2D() {
                @Override
                protected RegionBSPTree2D toTree() {
                    return LinePath.fromVertexLoop(result, e).toTree()
                }
            }
        }

    }

    private static class V {
        Vector2D v
        double r = 0
        int segments = 0

        V(Vector2D v) {
            this.v = v
        }

        static V of(double x, double y) {
            return new V(Vector2D.of(x, y))
        }
    }

}