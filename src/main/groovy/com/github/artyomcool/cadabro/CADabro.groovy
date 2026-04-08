package com.github.artyomcool.cadabro

import com.github.artyomcool.cadabro.d3.BSPTree
import com.github.artyomcool.cadabro.d3.CADObject3D
import com.github.artyomcool.cadabro.d3.Triangle
import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Point3D
import javafx.scene.DepthTest
import javafx.scene.Group
import javafx.scene.PerspectiveCamera
import javafx.scene.Scene
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.Box
import javafx.scene.shape.DrawMode
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh

import javafx.stage.Stage
import models.common.CardHolder
import models.organizer.Organizer

import java.nio.file.Path

import static com.github.artyomcool.cadabro.d2.CADObject2D.text
import static com.github.artyomcool.cadabro.d3.CADObjects.extrude

class CADabro extends Application {

    static CADObject3D txt(String t, Color color) {
        extrude(text(t), 1).color(color).rx(-90)
    }

    RenderCollection collection = Organizer.render().tap {
        render txt("X", Color.RED).dx(150)
        render txt("Y", Color.BLUE).rcz(90).dy(150)
        render txt("Z", Color.GREEN).dz(150)
    }

    final Group root = new Group()
    final Form axisGroup = new Form()
    final Form cadGroup = new Form()
    final Form world = new Form()
    final PerspectiveCamera camera = new PerspectiveCamera(true)
    final Form cameraXform = new Form()
    final Form cameraXform2 = new Form()
    final Form cameraXform3 = new Form()

    double mousePosX
    double mousePosY
    double mouseOldX
    double mouseOldY
    double mouseDeltaX
    double mouseDeltaY

    private void resetCamera() {
        cameraXform2.t.x = 0
        cameraXform2.t.y = 0
        camera.translateZ = -500
        cameraXform.ry.angle = 30
        cameraXform.rx.angle = 120
    }

    private void buildCamera() {
        root.getChildren().add(cameraXform)
        cameraXform.getChildren().add(cameraXform2)
        cameraXform2.getChildren().add(cameraXform3)
        cameraXform3.getChildren().add(camera)

        camera.tap {
            nearClip = 0.1
            farClip = 10000
        }
        cameraXform.tap {
            t.x = 50
            t.y = -100
        }
        resetCamera()
    }

    private void buildAxes() {
        double axisLength = 250
        Box xAxis = new Box(axisLength, 1, 1).tap {
            material = new PhongMaterial().tap {
                diffuseColor = Color.DARKRED
                specularColor = Color.RED
            }
        }
        Box yAxis = new Box(1, axisLength, 1).tap {
            material = new PhongMaterial().tap {
                diffuseColor = Color.DARKGREEN
                specularColor = Color.GREEN
            }
        }
        Box zAxis = new Box(1, 1, axisLength).tap {
            material = new PhongMaterial().tap {
                diffuseColor = Color.DARKBLUE
                specularColor = Color.BLUE
            }
        }

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis)
        world.getChildren().addAll(axisGroup)
    }

    private void handleMouse(Scene scene) {
        scene.onMousePressed = { MouseEvent me ->
            mousePosX = me.getSceneX()
            mousePosY = me.getSceneY()
            mouseOldX = me.getSceneX()
            mouseOldY = me.getSceneY()
        }
        scene.onMouseDragged = { MouseEvent me ->
            mouseOldX = mousePosX
            mouseOldY = mousePosY
            mousePosX = me.getSceneX()
            mousePosY = me.getSceneY()
            mouseDeltaX = (mousePosX - mouseOldX)
            mouseDeltaY = (mousePosY - mouseOldY)

            if (me.isPrimaryButtonDown()) {
                if (me.isControlDown()) {
                    cameraXform.ry.angle -= mouseDeltaX
                    cameraXform.rx.angle -= mouseDeltaY
                } else {
                    Point3D cameraPos = camera.localToScene(0, 0, 0)
                    double distance = Math.sqrt(cameraPos.x ** 2 + cameraPos.y ** 2 + cameraPos.z ** 2)

                    double fov = Math.toRadians(camera.getFieldOfView())
                    double unitsPerPixel = 2 * distance * Math.tan(fov / 2) / scene.height

                    cameraXform2.t.x -= mouseDeltaX * unitsPerPixel
                    cameraXform2.t.y -= mouseDeltaY * unitsPerPixel
                }
            } else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ()
                double newZ = z + mouseDeltaX + mouseDeltaY
                camera.setTranslateZ(newZ)
            }
        }
    }

    private void handleKeyboard(Scene scene) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case KeyCode.Z:
                        resetCamera()
                        break
                    case KeyCode.X:
                        axisGroup.visible = !axisGroup.visible
                        break
                    case KeyCode.V:
                        cadGroup.visible = !cadGroup.visible
                        break
                    case KeyCode.T:
                        double dx = 0

                        List<Triangle> triangles = []
                        for (def render in collection.renders) {
                            if (!render.renderOnly) {
                                def obj = render.obj.center().dxyzBy(0.5).dx(dx)
                                triangles.addAll(obj.asTree().triangles())
                                dx = obj.bounds().max.x + 5
                            }
                        }

                        double[] r = new double[triangles.size() * 9]
                        int n = 0
                        for (def t in triangles) {
                            r[n++] = -t.p1.x
                            r[n++] = t.p1.y
                            r[n++] = t.p1.z
                            r[n++] = -t.p2.x
                            r[n++] = t.p2.y
                            r[n++] = t.p2.z
                            r[n++] = -t.p3.x
                            r[n++] = t.p3.y
                            r[n++] = t.p3.z
                        }

                        STLWriter.writeToFile(r, Path.of("e.stl"))
                        break
                }
            }
        })
    }

    private void buildFigure() {

        Map<Color, Integer> colors = [:]

        def dc = Color.color(0.4, 0.1, 1, 1)
        colors[dc] = 0

        def trianglesSolid = []
        def trianglesWires = []

        for (def r in collection.renders) {
            println r.name
            def t = r.obj.asTree().triangles()
            (r.wiresOnly ? trianglesWires : trianglesSolid).addAll(t)
            for (def tr in t) {
                {
                    if (tr.hasProperty("color") && tr.color != null) {
                        colors.putIfAbsent(tr.color, colors.size())
                    }
                }
            }
        }

        def image = new WritableImage(2 * colors.size(), 2)
        for (def ce in colors.entrySet()) {
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    image.pixelWriter.setColor(ce.value * 2 + x, y, ce.key)
                }
            }
        }

        TriangleMesh solidMesh = new TriangleMesh()
        TriangleMesh wiredMesh = new TriangleMesh()
        int i = 0
        for (Triangle tr in trianglesSolid) {
            solidMesh.getPoints().addAll(
                    (float) tr.p2.x,
                    (float) tr.p2.z,
                    (float) tr.p2.y,
                    (float) tr.p1.x,
                    (float) tr.p1.z,
                    (float) tr.p1.y,
                    (float) tr.p3.x,
                    (float) tr.p3.z,
                    (float) tr.p3.y
            )
            int ci = colors[tr.color ?: dc]
            solidMesh.getTexCoords().addAll(
                    (float) ((1 + ci * 2) / image.width), 0.5f,
                    (float) ((1 + ci * 2) / image.width), 0.5f,
                    (float) ((1 + ci * 2) / image.width), 0.5f,
            )
            solidMesh.getFaces().addAll(
                    i, i++,
                    i, i++,
                    i, i++
            )
        }

        i = 0
        for (Triangle tr in trianglesWires) {
            wiredMesh.getPoints().addAll(
                    (float) tr.p2.x,
                    (float) tr.p2.z,
                    (float) tr.p2.y,
                    (float) tr.p1.x,
                    (float) tr.p1.z,
                    (float) tr.p1.y,
                    (float) tr.p3.x,
                    (float) tr.p3.z,
                    (float) tr.p3.y
            )
            int ci = colors[tr.color ?: dc]
            wiredMesh.getTexCoords().addAll(
                    (float) ((1 + ci * 2) / image.width), 0.5f,
                    (float) ((1 + ci * 2) / image.width), 0.5f,
                    (float) ((1 + ci * 2) / image.width), 0.5f,
            )
            wiredMesh.getFaces().addAll(
                    i, i++,
                    i, i++,
                    i, i++
            )
        }
        MeshView solidMeshView = new MeshView()
        solidMeshView.setMaterial(new PhongMaterial().tap { diffuseMap = image })
        solidMeshView.mesh = solidMesh

        MeshView wiredMeshView = new MeshView()
        wiredMeshView.setMaterial(new PhongMaterial().tap { diffuseMap = image })
        wiredMeshView.mesh = wiredMesh
        wiredMeshView.drawMode = DrawMode.LINE

        cadGroup.getChildren().add(solidMeshView)
        cadGroup.getChildren().add(wiredMeshView)

        world.getChildren().addAll(cadGroup)
    }

    @Override
    public void start(Stage primaryStage) {

        root.getChildren().add(world)
        root.setDepthTest(DepthTest.ENABLE)

        buildCamera()
        buildAxes()
        buildFigure()

        Scene scene = new Scene(root, 1024, 768, true)
        scene.setFill(Color.GREY)
        handleKeyboard(scene)
        handleMouse(scene)

        primaryStage.setScene(scene)
        primaryStage.show()

        scene.setCamera(camera)
    }

    static void main(String[] args) {
        launch(CADabro.class, args)
    }
}
