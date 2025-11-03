package models.carcassonne

import com.github.artyomcool.cadabro.RenderCollection
import models.common.CardHolder

class Carcassonne {

    static render() {
        new RenderCollection().tap {
            def tower = new Tower()
            // add tower.render()
            // add tower.bottom()

            //def grid = new Grid()
            //add grid.render()

            add new CardHolder(65, 40, 18).tap {borderThickness = 0.8}.withTeeth()
        }
    }

}
