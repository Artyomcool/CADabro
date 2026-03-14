package com.github.artyomcool.cadabro;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class STLWriter {

    public static void writeToFile(double[] solid, Path p) throws IOException {
        try (Writer writer = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            writer.write("solid Solid\n");
            for (int i = 0; i < solid.length;) {
                writer.write("facet normal 0 0 0\n");
                writer.write("outer loop\n");
                for (int j = 0; j < 3; j++) {
                    writer.write("vertex " + solid[i++] + " " + solid[i++] + " " + solid[i++] + "\n");
                }
                writer.write("endloop\nendfacet\n");
            }
            writer.write("endsolid Solid\n");
        }
    }
}
