package models.dogfood

import com.github.artyomcool.cadabro.RenderCollection
import com.github.artyomcool.cadabro.d3.CADObjects

import static com.github.artyomcool.cadabro.d3.CADObjects.*

class DogFood {

    static double w = 60
    static double z = 60
    static double r = 8
    static double s = 1.2
    static double d = 0.8
    static double maxW = w * s
    static double dw = (maxW - w) / z * d + w - d * 2
    static double maxDw = maxW - d * 2

    static render() {
        return new RenderCollection().tap {
            add rcube(w, w, z, r, true, true, true, true, 1.2) -
                    rcube(dw, dw, z - d, r * 0.95, true, true, true, true, maxDw / dw).dxyz(d)
        }
    }

}
