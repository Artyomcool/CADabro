module CADabro.main {
    requires org.apache.groovy;
    requires javafx.graphics;
    requires org.apache.commons.geometry.euclidean;
    requires org.apache.commons.numbers.core;
    requires org.apache.commons.geometry.core;
    requires com.github.quickhull3d;
    requires java.desktop;

    opens com.github.artyomcool.cadabro;
    opens com.github.artyomcool.cadabro.d2;
    opens com.github.artyomcool.cadabro.d3;
}