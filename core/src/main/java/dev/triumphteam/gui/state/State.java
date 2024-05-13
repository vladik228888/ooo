package dev.triumphteam.gui.state;

import dev.triumphteam.gui.GuiView;
import dev.triumphteam.gui.state.builtin.SimpleMutableState;
import org.jetbrains.annotations.NotNull;

/**
 * A representation of a "state" in the GUI.
 * A state doesn't necessarily need to hold any value.
 * Its sole purpose is to trigger updates on the GUI.
 *
 * @see MutableState
 * @see AbstractMutableState
 * @see SimpleMutableState
 */
public interface State {

    /**
     * Trigger a component to re-render.
     */
    void trigger();

    /**
     * Adds a new listener to the state.
     * Avoid calling this method manually if you don't know what you are doing,
     * this is mostly done internally by the {@link GuiView}.
     * <p>
     * The listener is tied to the lifecycle of the {@link GuiView},
     * so avoid holding the view if it is no longer needed.
     *
     * @param view     The {@link GuiView} which will be handling this state.
     * @param listener The listener to be called when a state is triggered.
     */
    void addListener(final @NotNull GuiView<?, ?> view, final @NotNull Runnable listener);
}