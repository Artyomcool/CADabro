/*
 * Copyright 2017 David Naramski.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *      http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.artyomcool.cadabro;

import com.github.artyomcool.cadabro.d3.BSPTree;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class STLWriter {

    public static void writeToFile(List<BSPTree.Triangle> solid, Path p) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(p, StandardCharsets.UTF_8))) {
            write(solid, writer);
        }
    }

    public static void write(List<BSPTree.Triangle> solid, PrintWriter output) {
        output.write("solid Solid\n");

        for (BSPTree.Triangle p : solid) {
            try {
                output.write("facet normal ");
                write(p.getNorm(), output);
                output.write("outer loop\n");

                output.write("vertex ");
                write(p.getP2(), output);
                output.write("vertex ");
                write(p.getP1(), output);
                output.write("vertex ");
                write(p.getP3(), output);

                output.write("endloop\nendfacet\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        output.write("endsolid Solid\n");
    }

    private static void write(Vector3D vector, PrintWriter output) {
        output.printf(Locale.US, "%.4f %.4f %.4f\n", vector.getX(), vector.getY(), vector.getZ());
    }

}
