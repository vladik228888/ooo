package dev.triumphteam.gui.component.components;

import dev.triumphteam.gui.click.action.RunnableGuiClickAction;
import dev.triumphteam.gui.component.ReactiveGuiComponent;
import dev.triumphteam.gui.container.GuiContainer;
import dev.triumphteam.gui.item.GuiItem;
import dev.triumphteam.gui.item.items.SimpleGuiItem;
import dev.triumphteam.gui.layout.GuiLayout;
import dev.triumphteam.gui.slot.Slot;
import dev.triumphteam.gui.state.MutableState;
import dev.triumphteam.gui.state.State;
import dev.triumphteam.gui.state.builtin.SimpleMutableState;
import dev.triumphteam.gui.state.policy.StateMutationPolicy;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class PaginatedComponent<P, I> implements ReactiveGuiComponent<P, I> {

    private final Slot back;
    private final I backItem;
    private final Slot forward;
    private final I forwardItem;
    private final List<GuiItem<P, I>> items;
    private final GuiLayout layout;

    private final MutableState<Integer> pageState = new SimpleMutableState<>(0, StateMutationPolicy.StructuralEquality.INSTANCE);

    public PaginatedComponent(
        final @NotNull Slot back,
        final @NotNull I backItem,
        final @NotNull Slot forward,
        final @NotNull I forwardItem,
        final @NotNull List<GuiItem<P, I>> items,
        final @NotNull GuiLayout layout
    ) {
        this.back = back;
        this.backItem = backItem;
        this.forward = forward;
        this.forwardItem = forwardItem;
        this.items = items;
        this.layout = layout;
    }

    @Override
    public @NotNull List<@NotNull State> states() {
        return List.of(pageState);
    }

    @Override
    public void render(final @NotNull GuiContainer<P, I> container, @NotNull final P player) {
        final var page = pageState.getValue();

        container.set(back, new SimpleGuiItem<>(backItem, (RunnableGuiClickAction<P>) (ignored, context) -> pageState.setValue(page - 1)));
        container.set(forward, new SimpleGuiItem<>(forwardItem, (RunnableGuiClickAction<P>) (ignored, context) -> pageState.setValue(page + 1)));

        final var slots = layout.generatePositions();
        final var size = slots.size();

        final var offset = page * size;

        for (int i = 0; i < size; i++) {
            final var offsetSlot = offset + i;

            /*if (offsetSlot >= items.size() || offsetSlot < 0) {
                return;
            }*/

            final var slot = slots.get(i);
            final var item = items.get(offsetSlot);

            container.set(slot, item);
        }
    }
}