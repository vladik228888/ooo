package dev.triumphteam.gui.kotlin

import dev.triumphteam.gui.builder.gui.BaseGuiBuilder
import dev.triumphteam.gui.builder.gui.SimpleBuilder
import dev.triumphteam.gui.builder.item.ItemBuilder

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class GuiDsl

class RowBuilder

@GuiDsl
inline fun gui(builder: SimpleBuilder.() -> Unit) {

}

inline fun BaseGuiBuilder<*, *>.row(builder: RowBuilder.() -> Unit) {

}

inline fun RowBuilder.item(itemBuilder: ItemBuilder.() -> Unit) {

}