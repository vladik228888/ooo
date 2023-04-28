package dev.triumphteam.gui;

import dev.triumphteam.gui.element.GuiElement;
import org.jetbrains.annotations.NotNull;

public interface GuiContainer {

    void set(final int slot, final @NotNull GuiElement element);

    void set(final int column, final int row, final @NotNull GuiElement element);
}
