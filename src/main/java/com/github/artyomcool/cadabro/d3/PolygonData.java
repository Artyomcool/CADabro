package com.github.artyomcool.cadabro.d3;

import javafx.scene.paint.Color;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

import java.util.List;

public class PolygonData {
    public List<Vector3D> points;
    public Color color;
    public Vector3D normal;

    public PolygonData(List<Vector3D> points, Color color, Vector3D normal) {
        this.points = points;
        this.color = color;
        this.normal = normal;
    }
}
