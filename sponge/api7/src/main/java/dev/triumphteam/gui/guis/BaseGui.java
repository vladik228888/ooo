/**
 * MIT License
 * <p>
 * Copyright (c) 2021 TriumphTeam
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.triumphteam.gui.guis;

import dev.triumphteam.gui.TriumphPlugin;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.components.exception.GuiException;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.components.util.Legacy;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.player.EntityPlayerMP;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.api.item.inventory.property.InventoryCapacity;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Base class that every GUI extends.
 * Contains all the basics for the GUI to work.
 * Main and simplest implementation of this is {@link Gui}.
 */
public abstract class BaseGui extends AbstractInventoryProperty {

    // Main inventory.
    private Inventory inventory;

    // title
    private String title;

    // Gui filler.
    private final GuiFiller filler = new GuiFiller(this);

    private int rows = 1;

    // Gui type, defaults to chest.
    private GuiType guiType = GuiType.CHEST;

    // Contains all items the GUI will have.
    private final Map<Integer, GuiItem> guiItems;

    // Actions for specific slots.
    private final Map<Integer, GuiAction<ClickInventoryEvent>> slotActions;
    // Interaction modifiers.
    private final Set<InteractionModifier> interactionModifiers;
    // Action to execute when clicking on any item.
    private GuiAction<ClickInventoryEvent> defaultClickAction;
    // Action to execute when clicking on the top part of the GUI only.
    private GuiAction<ClickInventoryEvent> defaultTopClickAction;
    // Action to execute when clicking on the player Inventory.
    private GuiAction<ClickInventoryEvent> playerInventoryAction;
    // Action to execute when dragging the item on the GUI.
    private GuiAction<ClickInventoryEvent.Drag> dragAction;
    // Action to execute when GUI closes.
    private GuiAction<InteractInventoryEvent.Close> closeGuiAction;
    // Action to execute when GUI opens.
    private GuiAction<InteractInventoryEvent.Open> openGuiAction;
    // Action to execute when clicked outside the GUI.
    private GuiAction<ClickInventoryEvent.Drop.Outside> outsideClickAction;

    // Whether the GUI is updating.
    private boolean updating;

    // Whether should run the actions from the close and open methods.
    private boolean runCloseAction = true;
    private boolean runOpenAction = true;

    /**
     * The main constructor, using {@link String}.
     *
     * @param rows                 The amount of rows to use.
     * @param title                The GUI title using {@link String}.
     * @param interactionModifiers Modifiers to select which interactions are allowed.
     * @since 3.0.0.
     */
    public BaseGui(final int rows, @NotNull final String title, @NotNull final Set<InteractionModifier> interactionModifiers) {
        int finalRows = rows;
        if (!(rows >= 1 && rows <= 6)) finalRows = 1;
        this.rows = finalRows;
        this.interactionModifiers = safeCopyOf(interactionModifiers);
        this.title = title;
        int inventorySize = this.rows * 9;
        this.inventory = Inventory.builder().property("triumph", this).of(GuiType.CHEST.getInventoryType()).property(InventoryDimension.of(9, this.rows)).property(InventoryTitle.of(Text.of(title))).build(TriumphPlugin.getInstance());
        this.slotActions = new LinkedHashMap<>(inventorySize);
        this.guiItems = new LinkedHashMap<>(inventorySize);
    }

    /**
     * Alternative constructor that takes {@link GuiType} instead of rows number.
     *
     * @param guiType              The {@link GuiType} to use.
     * @param title                The GUI title using {@link String}.
     * @param interactionModifiers Modifiers to select which interactions are allowed.
     * @since 3.0.0
     */
    public BaseGui(@NotNull final GuiType guiType, @NotNull final String title, @NotNull final Set<InteractionModifier> interactionModifiers) {
        this.guiType = guiType;
        this.interactionModifiers = safeCopyOf(interactionModifiers);
        this.title = title;
        int inventorySize = this.rows * 9;
        this.inventory = Inventory.builder().property("triumph", this).of(GuiType.CHEST.getInventoryType()).property(InventoryDimension.of(9, this.rows)).property(InventoryTitle.of(Text.of(title))).build(TriumphPlugin.getInstance());
        this.slotActions = new LinkedHashMap<>(inventorySize);
        this.guiItems = new LinkedHashMap<>(inventorySize);
    }

    /**
     * Copy a set into an EnumSet, required because {@link EnumSet#copyOf(EnumSet)} throws an exception if the collection passed as argument is empty.
     *
     * @param set The set to be copied.
     * @return An EnumSet with the provided elements from the original set.
     */
    @NotNull
    private EnumSet<InteractionModifier> safeCopyOf(@NotNull final Set<InteractionModifier> set) {
        if (set.isEmpty()) return EnumSet.noneOf(InteractionModifier.class);
        else return EnumSet.copyOf(set);
    }

    /**
     * Legacy constructor that takes rows and title.
     *
     * @param rows  The amount of rows the GUI should have.
     * @param title The GUI title.
     * @deprecated In favor of {@link BaseGui#BaseGui(int, String, Set)}.
     */
    @Deprecated
    public BaseGui(final int rows, @NotNull final String title) {
        int finalRows = rows;
        if (!(rows >= 1 && rows <= 6)) finalRows = 1;
        this.rows = finalRows;
        this.interactionModifiers = EnumSet.noneOf(InteractionModifier.class);
        this.title = title;

        inventory = Inventory.builder().property("triumph", this).of(GuiType.CHEST.getInventoryType()).property(InventoryDimension.of(9, this.rows)).property(InventoryTitle.of(Text.of(title))).build(TriumphPlugin.getInstance());
        slotActions = new LinkedHashMap<>();
        guiItems = new LinkedHashMap<>();
    }

    /**
     * Alternative constructor that takes {@link GuiType} instead of rows number.
     *
     * @param guiType The {@link GuiType} to use.
     * @param title   The GUI title.
     * @deprecated In favor of {@link BaseGui#BaseGui(GuiType, String, Set)}.
     */
    @Deprecated
    public BaseGui(@NotNull final GuiType guiType, @NotNull final String title) {
        this.guiType = guiType;
        this.interactionModifiers = EnumSet.noneOf(InteractionModifier.class);
        this.title = title;

        inventory = Inventory.builder().property("triumph", this).of(this.guiType.getInventoryType()).property(InventoryDimension.of(this.guiType.getLimit(), this.rows)).property(InventoryTitle.of(Text.of(title))).build(TriumphPlugin.getInstance());
        slotActions = new LinkedHashMap<>();
        guiItems = new LinkedHashMap<>();
    }

    /**
     * Gets the GUI's title as string.
     *
     * @return The GUI's title.
     */
    @NotNull
    @Deprecated
    public String getTitle() {
        return title;
    }

    /**
     * Gets the GUI title as a {@link Component}.
     *
     * @return The GUI title {@link Component}.
     */
    @NotNull
    public Component title() {
        return Legacy.SERIALIZER.deserialize(title);
    }

    /**
     * Sets the {@link GuiItem} to a specific slot on the GUI.
     *
     * @param slot    The GUI slot.
     * @param guiItem The {@link GuiItem} to add to the slot.
     */
    public void setItem(final int slot, @NotNull final GuiItem guiItem) {
        validateSlot(slot);
        guiItems.put(slot, guiItem);
    }

    /**
     * Removes the given {@link GuiItem} from the GUI.
     *
     * @param item The item to remove.
     */
    public void removeItem(@NotNull final GuiItem item) {
        final Optional<Map.Entry<Integer, GuiItem>> entry = guiItems.entrySet()
                .stream()
                .filter(it -> it.getValue().equals(item))
                .findFirst();

        entry.ifPresent(it -> {
            guiItems.remove(it.getKey());
        });
    }

    /**
     * Removes the given {@link ItemStack} from the GUI.
     *
     * @param item The item to remove.
     */
    public void removeItem(@NotNull final ItemStack item) {
        final Optional<Map.Entry<Integer, GuiItem>> entry = guiItems.entrySet()
                .stream()
                .filter(it -> it.getValue().getItemStack().equals(item))
                .findFirst();

        entry.ifPresent(it -> {
            guiItems.remove(it.getKey());
        });
    }

    /**
     * Removes the {@link GuiItem} in the specific slot.
     *
     * @param slot The GUI slot.
     */
    public void removeItem(final int slot) {
        validateSlot(slot);
        guiItems.remove(slot);
    }

    /**
     * Alternative {@link #removeItem(int)} with cols and rows.
     *
     * @param row The row.
     * @param col The column.
     */
    public void removeItem(final int row, final int col) {
        removeItem(getSlotFromRowCol(row, col));
    }

    /**
     * Alternative {@link #setItem(int, GuiItem)} to set item that takes a {@link List} of slots instead.
     *
     * @param slots   The slots in which the item should go.
     * @param guiItem The {@link GuiItem} to add to the slots.
     */
    public void setItem(@NotNull final List<Integer> slots, @NotNull final GuiItem guiItem) {
        for (final int slot : slots) {
            setItem(slot, guiItem);
        }
    }

    /**
     * Alternative {@link #setItem(int, GuiItem)} to set item that uses <i>ROWS</i> and <i>COLUMNS</i> instead of slots.
     *
     * @param row     The GUI row number.
     * @param col     The GUI column number.
     * @param guiItem The {@link GuiItem} to add to the slot.
     */
    public void setItem(final int row, final int col, @NotNull final GuiItem guiItem) {
        setItem(getSlotFromRowCol(row, col), guiItem);
    }

    /**
     * Adds {@link GuiItem}s to the GUI without specific slot.
     * It'll set the item to the next empty slot available.
     *
     * @param items Varargs for specifying the {@link GuiItem}s.
     */
    public void addItem(@NotNull final GuiItem... items) {
        this.addItem(false, items);
    }

    /**
     * Adds {@link GuiItem}s to the GUI without specific slot.
     * It'll set the item to the next empty slot available.
     *
     * @param items        Varargs for specifying the {@link GuiItem}s.
     * @param expandIfFull If true, expands the gui if it is full
     *                     and there are more items to be added
     */
    public void addItem(final boolean expandIfFull, @NotNull final GuiItem... items) {
        final List<GuiItem> notAddedItems = new ArrayList<>();

        for (final GuiItem guiItem : items) {
            for (int slot = 0; slot < rows * 9; slot++) {
                if (guiItems.get(slot) != null) {
                    if (slot == rows * 9 - 1) {
                        notAddedItems.add(guiItem);
                    }
                    continue;
                }

                guiItems.put(slot, guiItem);
                break;
            }
        }

        if (!expandIfFull || this.rows >= 6 ||
                notAddedItems.isEmpty() ||
                (this.guiType != null && this.guiType != GuiType.CHEST)) {
            return;
        }

        this.rows++;
        this.inventory = Inventory.builder().of(this.guiType.getInventoryType()).property(InventoryCapacity.of(this.rows * 9)).property(InventoryTitle.of(Text.of(title))).build(TriumphPlugin.getInstance());
        this.update();
        this.addItem(true, notAddedItems.toArray(new GuiItem[0]));
    }

    /**
     * Sets the {@link GuiAction} of a default click on any item.
     * See {@link ClickInventoryEvent}.
     *
     * @param defaultClickAction {@link GuiAction} to resolve when any item is clicked.
     */
    public void setDefaultClickAction(@Nullable final GuiAction<@NotNull ClickInventoryEvent> defaultClickAction) {
        this.defaultClickAction = defaultClickAction;
    }

    /**
     * Sets the {@link GuiAction} of a default click on any item on the top part of the GUI.
     * Top inventory being for example chests etc, instead of the {@link Player} inventory.
     * See {@link ClickInventoryEvent}.
     *
     * @param defaultTopClickAction {@link GuiAction} to resolve when clicking on the top inventory.
     */
    public void setDefaultTopClickAction(@Nullable final GuiAction<@NotNull ClickInventoryEvent> defaultTopClickAction) {
        this.defaultTopClickAction = defaultTopClickAction;
    }

    public void setPlayerInventoryAction(@Nullable final GuiAction<@NotNull ClickInventoryEvent> playerInventoryAction) {
        this.playerInventoryAction = playerInventoryAction;
    }

    /**
     * Sets the {@link GuiAction} to run when clicking on the outside of the inventory.
     * See {@link ClickInventoryEvent}.
     *
     * @param outsideClickAction {@link GuiAction} to resolve when clicking outside of the inventory.
     */
    public void setOutsideClickAction(@Nullable final GuiAction<ClickInventoryEvent.Drop.Outside> outsideClickAction) {
        this.outsideClickAction = outsideClickAction;
    }

    /**
     * Sets the {@link GuiAction} of a default drag action.
     * See {@link ClickInventoryEvent.Drag}.
     *
     * @param dragAction {@link GuiAction} to resolve.
     */
    public void setDragAction(@Nullable final GuiAction<ClickInventoryEvent.Drag> dragAction) {
        this.dragAction = dragAction;
    }

    /**
     * Sets the {@link GuiAction} to run once the inventory is closed.
     * See {@link InteractInventoryEvent.Close}.
     *
     * @param closeGuiAction {@link GuiAction} to resolve when the inventory is closed.
     */
    public void setCloseGuiAction(@Nullable final GuiAction<InteractInventoryEvent.Close> closeGuiAction) {
        this.closeGuiAction = closeGuiAction;
    }

    /**
     * Sets the {@link GuiAction} to run when the GUI opens.
     * See {@link InteractInventoryEvent.Open}.
     *
     * @param openGuiAction {@link GuiAction} to resolve when opening the inventory.
     */
    public void setOpenGuiAction(@Nullable final GuiAction<InteractInventoryEvent.Open> openGuiAction) {
        this.openGuiAction = openGuiAction;
    }

    /**
     * Adds a {@link GuiAction} for when clicking on a specific slot.
     * See {@link ClickInventoryEvent}.
     *
     * @param slot       The slot that will trigger the {@link GuiAction}.
     * @param slotAction {@link GuiAction} to resolve when clicking on specific slots.
     */
    public void addSlotAction(final int slot, @Nullable final GuiAction<ClickInventoryEvent> slotAction) {
        validateSlot(slot);
        slotActions.put(slot, slotAction);
    }

    /**
     * Alternative method for {@link #addSlotAction(int, GuiAction)} to add a {@link GuiAction} to a specific slot using <i>ROWS</i> and <i>COLUMNS</i> instead of slots.
     * See {@link ClickInventoryEvent}.
     *
     * @param row        The row of the slot.
     * @param col        The column of the slot.
     * @param slotAction {@link GuiAction} to resolve when clicking on the slot.
     */
    public void addSlotAction(final int row, final int col, @Nullable final GuiAction<ClickInventoryEvent> slotAction) {
        addSlotAction(getSlotFromRowCol(row, col), slotAction);
    }

    /**
     * Gets a specific {@link GuiItem} on the slot.
     *
     * @param slot The slot of the item.
     * @return The {@link GuiItem} on the introduced slot or {@code null} if doesn't exist.
     */
    @Nullable
    public GuiItem getGuiItem(final int slot) {
        return guiItems.get(slot);
    }

    /**
     * Checks whether or not the GUI is updating.
     *
     * @return Whether the GUI is updating or not.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isUpdating() {
        return updating;
    }

    /**
     * Sets the updating status of the GUI.
     *
     * @param updating Sets the GUI to the updating status.
     */
    public void setUpdating(final boolean updating) {
        this.updating = updating;
    }

    /**
     * Opens the GUI for a {@link Player}.
     *
     * @param player The {@link Player} to open the GUI to.
     */
    public void open(@NotNull final Player player) {
        if (player.get(Keys.IS_SLEEPING).isPresent() && player.get(Keys.IS_SLEEPING).get()) return;

        inventory.clear();
        populateGui();
        Sponge.getScheduler().createTaskBuilder().delayTicks(1).execute(() -> {
            player.openInventory(inventory);
        }).submit(TriumphPlugin.getInstance());
    }

    /**
     * Closes the GUI with a {@code 2 tick} delay (to prevent items from being taken from the {@link Inventory}).
     *
     * @param player The {@link Player} to close the GUI to.
     */
    public void close(@NotNull final Player player) {
        close(player, true);
    }

    /**
     * Closes the GUI with a {@code 2 tick} delay (to prevent items from being taken from the {@link Inventory}).
     *
     * @param player         The {@link Player} to close the GUI to.
     * @param runCloseAction If should or not run the close action.
     */
    public void close(@NotNull final Player player, final boolean runCloseAction) {
        Sponge.getScheduler().createTaskBuilder().delayTicks(2).execute(() -> {
            this.runCloseAction = runCloseAction;
            player.closeInventory();
            this.runCloseAction = true;
        }).submit(TriumphPlugin.getInstance());
    }

    /**
     * Updates the GUI for all the {@link Inventory} views.
     */
    public void update() {
        inventory.clear();
        populateGui();
        for (Player viewer : new ArrayList<>(inventory.<Container>query(Container.class).getViewers())) {
            ((EntityPlayerMP) viewer).sendContainerToPlayer(((EntityPlayerMP) viewer).openContainer);
        }
    }

    /**
     * Updates the title of the GUI.
     * <i>This method may cause LAG if used on a loop</i>.
     *
     * @param title The title to set.
     * @return The GUI for easier use when declaring, works like a builder.
     */
    @Contract("_ -> this")
    @NotNull
    public BaseGui updateTitle(@NotNull final String title) {
        updating = true;

        final List<Player> viewers = new ArrayList<>(inventory.<Container>query(Container.class).getViewers());

        this.inventory = Inventory.builder().of(this.guiType.getInventoryType()).property(InventoryDimension.of(this.guiType.getLimit(), this.rows)).property(InventoryTitle.of(Text.of(title))).build(TriumphPlugin.getInstance());

        for (final Player player : viewers) {
            open(player);
        }

        updating = false;
        this.title = title;
        return this;
    }

    /**
     * Updates the specified item in the GUI at runtime, without creating a new {@link GuiItem}.
     *
     * @param slot      The slot of the item to update.
     * @param itemStack The {@link ItemStack} to replace in the original one in the {@link GuiItem}.
     */
    public void updateItem(final int slot, @NotNull final ItemStack itemStack) {
        if (!guiItems.containsKey(slot)) return;
        final GuiItem guiItem = guiItems.get(slot);
        guiItem.setItemStack(itemStack);
        inventory.query(SlotIndex.of(slot)).set(guiItem.getItemStack());
    }

    /**
     * Alternative {@link #updateItem(int, ItemStack)} that takes <i>ROWS</i> and <i>COLUMNS</i> instead of slots.
     *
     * @param row       The row of the slot.
     * @param col       The columns of the slot.
     * @param itemStack The {@link ItemStack} to replace in the original one in the {@link GuiItem}.
     */
    public void updateItem(final int row, final int col, @NotNull final ItemStack itemStack) {
        updateItem(getSlotFromRowCol(row, col), itemStack);
    }

    /**
     * Alternative {@link #updateItem(int, ItemStack)} but creates a new {@link GuiItem}.
     *
     * @param slot The slot of the item to update.
     * @param item The {@link GuiItem} to replace in the original.
     */
    public void updateItem(final int slot, @NotNull final GuiItem item) {
        if (!guiItems.containsKey(slot)) return;
        guiItems.put(slot, item);
        inventory.query(SlotIndex.of(slot)).set(item.getItemStack());
    }

    /**
     * Alternative {@link #updateItem(int, GuiItem)} that takes <i>ROWS</i> and <i>COLUMNS</i> instead of slots.
     *
     * @param row  The row of the slot.
     * @param col  The columns of the slot.
     * @param item The {@link GuiItem} to replace in the original.
     */
    public void updateItem(final int row, final int col, @NotNull final GuiItem item) {
        updateItem(getSlotFromRowCol(row, col), item);
    }

    /**
     * Disable item placement inside the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui disableItemPlace() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_PLACE);
        return this;
    }

    /**
     * Disable item retrieval inside the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui disableItemTake() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_TAKE);
        return this;
    }

    /**
     * Disable item swap inside the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui disableItemSwap() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_SWAP);
        return this;
    }

    /**
     * Disable item drop inside the GUI
     *
     * @return The BaseGui
     * @since 3.0.3.
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui disableItemDrop() {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_DROP);
        return this;
    }

    /**
     * Disable other GUI actions
     * This option pretty much disables creating a clone stack of the item
     *
     * @return The BaseGui
     * @since 3.0.4
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui disableOtherActions() {
        interactionModifiers.add(InteractionModifier.PREVENT_OTHER_ACTIONS);
        return this;
    }

    /**
     * Disable all the modifications of the GUI, making it immutable by player interaction.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui disableAllInteractions() {
        interactionModifiers.addAll(InteractionModifier.VALUES);
        return this;
    }

    /**
     * Allows item placement inside the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui enableItemPlace() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_PLACE);
        return this;
    }

    /**
     * Allow items to be taken from the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui enableItemTake() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_TAKE);
        return this;
    }

    /**
     * Allows item swap inside the GUI.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui enableItemSwap() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_SWAP);
        return this;
    }

    /**
     * Allows item drop inside the GUI
     *
     * @return The BaseGui
     * @since 3.0.3
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui enableItemDrop() {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_DROP);
        return this;
    }

    /**
     * Enable other GUI actions
     * This option pretty much enables creating a clone stack of the item
     *
     * @return The BaseGui
     * @since 3.0.4
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui enableOtherActions() {
        interactionModifiers.remove(InteractionModifier.PREVENT_OTHER_ACTIONS);
        return this;
    }

    /**
     * Enable all modifications of the GUI, making it completely mutable by player interaction.
     *
     * @return The BaseGui.
     * @author SecretX.
     * @since 3.0.0.
     */
    @NotNull
    @Contract(" -> this")
    public BaseGui enableAllInteractions() {
        interactionModifiers.clear();
        return this;
    }

    /**
     * Check if item placement is allowed inside this GUI.
     *
     * @return True if item placement is allowed for this GUI.
     * @author SecretX.
     * @since 3.0.0.
     */
    public boolean canPlaceItems() {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_PLACE);
    }

    /**
     * Check if item retrieval is allowed inside this GUI.
     *
     * @return True if item retrieval is allowed inside this GUI.
     * @author SecretX.
     * @since 3.0.0.
     */
    public boolean canTakeItems() {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_TAKE);
    }

    /**
     * Check if item swap is allowed inside this GUI.
     *
     * @return True if item swap is allowed for this GUI.
     * @author SecretX.
     * @since 3.0.0.
     */
    public boolean canSwapItems() {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_SWAP);
    }

    /**
     * Check if item drop is allowed inside this GUI
     *
     * @return True if item drop is allowed for this GUI
     * @since 3.0.3
     */
    public boolean canDropItems() {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_ITEM_DROP);
    }

    /**
     * Check if any other actions are allowed in this GUI
     *
     * @return True if other actions are allowed
     * @since 3.0.4
     */
    public boolean allowsOtherActions() {
        return !interactionModifiers.contains(InteractionModifier.PREVENT_OTHER_ACTIONS);
    }

    /**
     * Gets the {@link GuiFiller} that it's used for filling up the GUI in specific ways.
     *
     * @return The {@link GuiFiller}.
     */
    @NotNull
    public GuiFiller getFiller() {
        return filler;
    }

    /**
     * Gets an immutable {@link Map} with all the GUI items.
     *
     * @return The {@link Map} with all the {@link #guiItems}.
     */
    @NotNull
    public Map<@NotNull Integer, @NotNull GuiItem> getGuiItems() {
        return guiItems;
    }

    /**
     * Gets the main {@link Inventory} of this GUI.
     *
     * @return Gets the {@link Inventory} from the holder.
     */
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Gets the amount of {@link #rows}.
     *
     * @return The {@link #rows} of the GUI.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the {@link GuiType} in use.
     *
     * @return The {@link GuiType}.
     */
    @NotNull
    public GuiType guiType() {
        return guiType;
    }

    /**
     * Gets the default click resolver.
     */
    @Nullable
    GuiAction<ClickInventoryEvent> getDefaultClickAction() {
        return defaultClickAction;
    }

    /**
     * Gets the default top click resolver.
     */
    @Nullable
    GuiAction<ClickInventoryEvent> getDefaultTopClickAction() {
        return defaultTopClickAction;
    }

    /**
     * Gets the player inventory action.
     */
    @Nullable
    GuiAction<ClickInventoryEvent> getPlayerInventoryAction() {
        return playerInventoryAction;
    }

    /**
     * Gets the default drag resolver.
     */
    @Nullable
    GuiAction<ClickInventoryEvent.Drag> getDragAction() {
        return dragAction;
    }

    /**
     * Gets the close gui resolver.
     */
    @Nullable
    GuiAction<InteractInventoryEvent.Close> getCloseGuiAction() {
        return closeGuiAction;
    }

    /**
     * Gets the open gui resolver.
     */
    @Nullable
    GuiAction<InteractInventoryEvent.Open> getOpenGuiAction() {
        return openGuiAction;
    }

    /**
     * Gets the resolver for the outside click.
     */
    @Nullable
    GuiAction<ClickInventoryEvent.Drop.Outside> getOutsideClickAction() {
        return outsideClickAction;
    }

    /**
     * Gets the action for the specified slot.
     *
     * @param slot The slot clicked.
     */
    @Nullable
    GuiAction<ClickInventoryEvent> getSlotAction(final int slot) {
        return slotActions.get(slot);
    }

    /**
     * Populates the GUI with it's items.
     */
    void populateGui() {
        for (final Map.Entry<Integer, GuiItem> entry : guiItems.entrySet()) {
            inventory.query(SlotIndex.of(entry.getKey())).set(entry.getValue().getItemStack());
        }
    }

    boolean shouldRunCloseAction() {
        return runCloseAction;
    }

    boolean shouldRunOpenAction() {
        return runOpenAction;
    }

    /**
     * Gets the slot from the row and column passed.
     *
     * @param row The row.
     * @param col The column.
     * @return The slot needed.
     */
    int getSlotFromRowCol(final int row, final int col) {
        return (col + (row - 1) * 9) - 1;
    }

    /**
     * Sets the new inventory of the GUI.
     *
     * @param inventory The new inventory.
     */
    public void setInventory(@NotNull final Inventory inventory) {
        this.inventory = inventory;
    }

    /*
    TODO fix this part, find a better solution for using Paper
    protected Inventory createRowedInventory(@NotNull final Component title) {
        if (VersionHelper.IS_COMPONENT_LEGACY) {
            return Bukkit.createInventory(this, this.rows * 9, Legacy.SERIALIZER.serialize(title));
        }

        return inventory = Bukkit.createInventory(this, this.rows * 9, title);
    }

    private Inventory createTypedInventory(@NotNull final Component title) {
        final InventoryType inventoryType = guiType.getInventoryType();
        if (VersionHelper.IS_COMPONENT_LEGACY) {
            return Bukkit.createInventory(this, inventoryType, Legacy.SERIALIZER.serialize(title));
        }

        return Bukkit.createInventory(this, inventoryType, title);
    }*/

    /**
     * Checks if the slot introduces is a valid slot.
     *
     * @param slot The slot to check.
     */
    private void validateSlot(final int slot) {
        final int limit = guiType.getLimit();

        if (guiType == GuiType.CHEST) {
            if (slot < 0 || slot >= rows * limit) throwInvalidSlot(slot);
            return;
        }

        if (slot < 0 || slot > limit) throwInvalidSlot(slot);
    }

    /**
     * Throws an exception if the slot is invalid.
     *
     * @param slot The specific slot to display in the error message.
     */
    private void throwInvalidSlot(final int slot) {
        if (guiType == GuiType.CHEST) {
            throw new GuiException("Slot " + slot + " is not valid for the gui type - " + guiType.name() + " and rows - " + rows + "!");
        }

        throw new GuiException("Slot " + slot + " is not valid for the gui type - " + guiType.name() + "!");
    }

}
