package com.github.artyomcool.cadabro

import com.github.artyomcool.cadabro.d2.CADObject2D
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.PathElement
import javafx.stage.Stage

class Plain extends Application {

    static void main(String[] args) {
        launch(Plain.class, args)
    }

    @Override
    void start(Stage stage) throws Exception {
        List<PathElement> elements = new ArrayList<>()

        def draw = CADObject2D.draw()
        def obj = draw
                .smooth()
                .dxy(200, 0)
                .smooth()
                .dxy(0, 200)
                .smooth()
                //.dxy(-200, -50)
                //.smooth()
        //.close()

        List<Path> paths = []

        double px=0, py=0
        for (final def c in obj.close().asTree().toConvex()) {
            for (def pi = 0; pi < c.vertices.size(); pi++) {
                def p = c.vertices.get(pi)
                def n = c.vertices.get((pi + 1) % c.vertices.size())

                def path = new Path(new MoveTo(p.x, p.y), new LineTo(n.x, n.y))
                path.setStroke(Color.hsb(360 * pi / c.vertices.size(), 1, 1))
                paths.add(path)
            }
        }

        def pane = new Pane(paths.toArray(new javafx.scene.Node[paths.size()]))
        Scene scene = new Scene(pane, 1024, 768, true);
        stage.setTitle("Molecule Sample Application");
        stage.setScene(scene);
        stage.show();
    }
}
