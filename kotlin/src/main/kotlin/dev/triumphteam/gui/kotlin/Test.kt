package dev.triumphteam.gui.kotlin

import net.kyori.adventure.text.Component

fun test() {

    gui {
        title(Component.text("Gui Title"))
        disableAllInteractions()

        row {
            item {
                title(Component.text("Item Name"))
            }

            item {
                title(Component.text("Item Name 2"))
            }
        }
    }

}