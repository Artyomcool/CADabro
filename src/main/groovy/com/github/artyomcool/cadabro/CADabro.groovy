package com.github.artyomcool.cadabro

import com.github.artyomcool.cadabro.d3.BSPTree
import javafx.application.Application
import javafx.event.EventHandler
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
import models.dnd.Drizzt

import java.nio.file.Path

class CADabro extends Application {

    RenderCollection collection = Drizzt.render()

    final Group root = new Group()
    final Form axisGroup = new Form()
    final Form moleculeGroup = new Form()
    final Form world = new Form()
    final PerspectiveCamera camera = new PerspectiveCamera(true)
    final Form cameraXform = new Form()
    final Form cameraXform2 = new Form()
    final Form cameraXform3 = new Form()
    private static final double CAMERA_INITIAL_DISTANCE = -450
    private static final double CAMERA_INITIAL_X_ANGLE = 120.0
    private static final double CAMERA_INITIAL_Y_ANGLE = 30.0
    private static final double CAMERA_NEAR_CLIP = 0.1
    private static final double CAMERA_FAR_CLIP = 10000.0
    private static final double AXIS_LENGTH = 250.0
    private static final double CONTROL_MULTIPLIER = 0.1
    private static final double SHIFT_MULTIPLIER = 10.0
    private static final double MOUSE_SPEED = 0.1
    private static final double ROTATION_SPEED = 2.0
    private static final double TRACK_SPEED = 1

    double mousePosX
    double mousePosY
    double mouseOldX
    double mouseOldY
    double mouseDeltaX
    double mouseDeltaY

    private void buildCamera() {
        System.out.println("buildCamera()")
        root.getChildren().add(cameraXform)
        cameraXform.getChildren().add(cameraXform2)
        cameraXform2.getChildren().add(cameraXform3)
        cameraXform3.getChildren().add(camera)

        camera.setNearClip(CAMERA_NEAR_CLIP)
        camera.setFarClip(CAMERA_FAR_CLIP)
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE)
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE)
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE)

        cameraXform.t.y = -20
        cameraXform.t.x = 50
    }

    private void buildAxes() {
        final PhongMaterial redMaterial = new PhongMaterial()
        redMaterial.setDiffuseColor(Color.DARKRED)
        redMaterial.setSpecularColor(Color.RED)

        final PhongMaterial greenMaterial = new PhongMaterial()
        greenMaterial.setDiffuseColor(Color.DARKGREEN)
        greenMaterial.setSpecularColor(Color.GREEN)

        final PhongMaterial blueMaterial = new PhongMaterial()
        blueMaterial.setDiffuseColor(Color.DARKBLUE)
        blueMaterial.setSpecularColor(Color.BLUE)

        final Box xAxis = new Box(AXIS_LENGTH, 1, 1)
        final Box yAxis = new Box(1, AXIS_LENGTH, 1)
        final Box zAxis = new Box(1, 1, AXIS_LENGTH)

        xAxis.setMaterial(redMaterial)
        yAxis.setMaterial(greenMaterial)
        zAxis.setMaterial(blueMaterial)

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis)
        world.getChildren().addAll(axisGroup)
    }

    private void handleMouse(Scene scene) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mousePosX = me.getSceneX()
                mousePosY = me.getSceneY()
                mouseOldX = me.getSceneX()
                mouseOldY = me.getSceneY()
            }
        })
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX
                mouseOldY = mousePosY
                mousePosX = me.getSceneX()
                mousePosY = me.getSceneY()
                mouseDeltaX = (mousePosX - mouseOldX)
                mouseDeltaY = (mousePosY - mouseOldY)

                double modifier = 1.0

                if (me.isControlDown()) {
                    modifier = CONTROL_MULTIPLIER
                }
                if (me.isShiftDown()) {
                    modifier = SHIFT_MULTIPLIER
                }
                if (me.isPrimaryButtonDown()) {
                    if (me.isAltDown()) {
                        cameraXform.t.setX(cameraXform.t.getX() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED)
                        cameraXform.t.setY(cameraXform.t.getY() - mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED)
                    } else {
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED)
                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED)
                    }
                } else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ()
                    double newZ = z + mouseDeltaX * MOUSE_SPEED * modifier
                    camera.setTranslateZ(newZ)
                }
            }
        })
    }

    private void handleKeyboard(Scene scene) {
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case KeyCode.Z:
                        cameraXform2.t.setX(0.0)
                        cameraXform2.t.setY(0.0)
                        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE)
                        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE)
                        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE)
                        break
                    case KeyCode.X:
                        axisGroup.setVisible(!axisGroup.isVisible())
                        break
                    case KeyCode.V:
                        moleculeGroup.setVisible(!moleculeGroup.isVisible())
                        break
                    case KeyCode.T:
                        double dx = 0
                        List<BSPTree.Triangle> triangles = []
                        for (def render in collection.renders) {
                            if (!render.renderOnly) {
                                def obj = render.obj.center().dxyzBy(0.5).dx(dx)
                                triangles.addAll(obj.asTree().triangles())
                                dx = obj.bounds().max.x + 5
                            }
                        }
                        STLWriter.writeToFile(triangles, Path.of("e.stl"))
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
        for (BSPTree.Triangle tr in trianglesSolid) {
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
        for (BSPTree.Triangle tr in trianglesWires) {
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

        moleculeGroup.getChildren().add(solidMeshView)
        moleculeGroup.getChildren().add(wiredMeshView)

        world.getChildren().addAll(moleculeGroup)
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
