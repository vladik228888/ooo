/**
 * MIT License
 *
 * Copyright (c) 2021 TriumphTeam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.triumphteam.gui.builder.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.triumphteam.gui.components.exception.GuiException;
import dev.triumphteam.gui.components.util.SkullUtil;
import dev.triumphteam.gui.components.util.VersionHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * New builder for skull only, created to separate the specific features for skulls
 * Soon I'll add more useful features to this builder
 */
public final class SkullBuilder extends BaseItemBuilder<SkullBuilder> {

    private static final int V1_12 = 1120;
    //private static final Field PROFILE_FIELD;

    static {
        Field field;

        /*try {
            final SkullMeta skullMeta = (SkullMeta) new ItemStack(SkullUtil.SKULL).getItemMeta();
            field = skullMeta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            field = null;
        }*/

        //PROFILE_FIELD = field;
    }

    SkullBuilder() {
        super(ItemStack.of(SkullUtil.SKULL));
    }

    SkullBuilder(final @NotNull ItemStack itemStack) {
        super(itemStack);
        if (itemStack.getType() != SkullUtil.SKULL) {
            throw new GuiException("SkullBuilder requires the material to be a PLAYER_HEAD/SKULL_ITEM!");
        }
    }

    /**
     * Sets the skull texture using a BASE64 string
     *
     * @param texture The base64 texture
     * @return {@link SkullBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public SkullBuilder texture(@NotNull final String texture) {
        if (getItemStack().getType() != SkullUtil.SKULL) return this;

        //if (PROFILE_FIELD == null) {
        //    return this;
        //}

        /*ItemStack.of(ItemTypes.SKULL).offer(Keys.SKULL_TYPE, )
        final SkullMeta skullMeta = (SkullMeta) getMeta();
        final GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));

        try {
            PROFILE_FIELD.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        setMeta(skullMeta);*/
        return this;
    }

    /**
     * Sets skull owner via bukkit methods
     *
     * @param player {@link OfflinePlayer} to set skull of
     * @return {@link SkullBuilder}
     */
    @NotNull
    @Contract("_ -> this")
    public SkullBuilder owner(@NotNull final User player) {
        if (getItemStack().getType() != SkullUtil.SKULL) return this;

        /*final SkullMeta skullMeta = (SkullMeta) getMeta();

        if (VersionHelper.IS_SKULL_OWNER_LEGACY) {
            skullMeta.setOwner(player.getName());
        } else {
            skullMeta.setOwningPlayer(player);
        }

        setMeta(skullMeta);*/
        return this;
    }

}
