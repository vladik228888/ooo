package dev.triumphteam.gui;

import dev.triumphteam.gui.block.GuiBlock;
import org.jetbrains.annotations.NotNull;

public final class GuiBuilder {

    public GuiBuilder rows(final int rows) {
        return this;
    }

    public GuiBuilder block(final @NotNull GuiBlock block) {
        return this;
    }
}
