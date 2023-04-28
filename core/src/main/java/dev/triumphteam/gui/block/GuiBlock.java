package dev.triumphteam.gui.block;

import dev.triumphteam.gui.GuiContainer;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface GuiBlock {

    void configure(final @NotNull GuiContainer container, final @NotNull GuiBlockController controller);
}
