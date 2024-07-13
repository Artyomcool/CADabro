package com.github.artyomcool.cadabro

import com.github.artyomcool.cadabro.d3.BSPTree
import com.github.artyomcool.cadabro.d3.CADObject3D
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
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh
import javafx.stage.Stage

import java.nio.file.Path

import static com.github.artyomcool.cadabro.d3.CADObjects.*

class CADabro extends Application {

    static CADObject3D miniHolder() {
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
        figures << [x: big.width - tiny.width, y: mid.height * 2, r: 0, m: small { width = tiny.width; extraCorners = 1}.connectTop(tiny).connectRight(small).connectRightTop(mid)]
        figures << [x: big.width, y: mid.height * 2, r: 0, m: small { width = mid.width; deltaX = -7 }.connectTop(mid).connectRight(0, 0, true)]

        def move = (f, m) -> {
            m.dxy(-f.m.width / 2, -f.m.height / 2).rz(f.r).dxy(f.m.width / 2, f.m.height / 2).dxy(f.x, f.y)
        }

        (
                union {
                    add cube(tw, th, 1).color(Color.GREEN)
                    for (def f in figures) {
                        add move(f, f.m.renderStrengthWall())
                    }
                } - union {
                    for (def f in figures) {
                        add move(f, f.m.renderCuts())
                    }
                } + union {
                    for (def f in figures) {
                        add move(f, f.m.renderHolder())
                    }
                } + diff {
                    add extrude(rsquare(tw, th), 70)
                    add extrude(rsquare(tw - 2, th - 2), 70).dxy(1, 1)
                    add cube(tw, th, 70).dxyz(16, 16, 16)
                } & extrude(rsquare(tw, th), 70)
        ).tap {
            println bounds().size()
        }
    }

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
                        def tree = miniHolder().asTree()
                        STLWriter.writeToFile(tree.triangles(), Path.of("e.stl"))
                        break
                }
            }
        })
    }

    private void buildFigure() {
        def tree = miniHolder()

        Map<Color, Integer> colors = [:]

        def dc = Color.color(0.4, 0.1, 1, 1)
        colors[dc] = 0

        def triangles = tree.asTree().triangles()
        for (def tr in triangles) {
            if (tr.hasProperty("color") && tr.color != null) {
                colors.putIfAbsent(tr.color, colors.size())
            }
        }
        def image = new WritableImage(2 * colors.size(), 2)
        for (def ce in colors.entrySet()) {
            def i = ce.value
            def c = ce.key
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    image.pixelWriter.setColor(i * 2 + x, y, c)
                }
            }
        }

        TriangleMesh mesh = new TriangleMesh()
        int i = 0
        for (BSPTree.Triangle tr in triangles) {
            mesh.getPoints().addAll(
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
            mesh.getTexCoords().addAll(
                    (float) ((1 + ci * 2) / image.width), 0.5f,
                    (float) ((1 + ci * 2) / image.width), 0.5f,
                    (float) ((1 + ci * 2) / image.width), 0.5f,
            )
            mesh.getFaces().addAll(
                    i, i++,
                    i, i++,
                    i, i++
            )
        }
        MeshView oxygenSphere = new MeshView()
        PhongMaterial material = new PhongMaterial()
        material.setDiffuseMap(image)
        oxygenSphere.setMaterial(material)
        //oxygenSphere.drawMode = DrawMode.LINE
        oxygenSphere.mesh = mesh

        moleculeGroup.getChildren().add(oxygenSphere)

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
