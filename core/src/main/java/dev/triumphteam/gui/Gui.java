package dev.triumphteam.gui;

import org.jetbrains.annotations.NotNull;

public interface Gui<P> {

    void configure(final @NotNull P player, final @NotNull GuiBuilder builder);
}
