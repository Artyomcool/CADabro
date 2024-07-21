package com.github.artyomcool.cadabro

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.PathElement
import javafx.stage.Stage
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D

import static com.github.artyomcool.cadabro.d2.CADObject2D.draw

class Plain extends Application {

    static void main(String[] args) {
        launch(Plain.class, args)
    }

    @Override
    void start(Stage stage) throws Exception {
        List<PathElement> elements = new ArrayList<>()

        double extra = 2
        double wall = 2
        double widthExact = 45
        double heightExact = 101

        double width = widthExact + extra
        double height = heightExact + extra

        def drawer = draw(0, 0).go(width)
                .cw().go(height)
                .cw().go(9)
                .r(45).go((width - 9) * Math.sqrt(2))

        def tree = drawer.close().asTree()
        def t2 = tree.copy().tap { transform(AffineTransformMatrix2D.createRotation(Math.toRadians(-90)).scale(1, -1).translate(0, height - width + 10)) }
        tree.transform(AffineTransformMatrix2D.createTranslation(width + 25, 0))
        tree.union(draw(0,0).go(64).cw().go(55).cw().go(64).close().asTree())
        tree.union(t2)
        tree.transform(AffineTransformMatrix2D.createTranslation(100, 100))


        List<Path> paths = []

        double px = 0, py = 0

        for (final def c in tree.boundaryPaths) {
            for (final def e in c.elements) {
                def path = new Path(new MoveTo(e.startPoint.x, e.startPoint.y), new LineTo(e.endPoint.x, e.endPoint.y))
                paths.add(path)
            }
        }

        def pp = 0
        for (def p in paths) {
            p.setStroke(Color.hsb(360 * pp / paths.size(), 1, 1))
            pp++
        }

        def pane = new Pane(paths.toArray(new javafx.scene.Node[paths.size()]))
        Scene scene = new Scene(pane, 1024, 768, true);
        stage.setTitle("Molecule Sample Application");
        stage.setScene(scene);
        stage.show();
    }
}
