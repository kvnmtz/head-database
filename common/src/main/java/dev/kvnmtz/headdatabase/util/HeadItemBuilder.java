package dev.kvnmtz.headdatabase.util;

import dev.kvnmtz.headdatabase.model.Head;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Base64;

public abstract class HeadItemBuilder {

    public static ItemStack createHeadItem(Head head) {
        return createHeadItem(head, true);
    }

    public static ItemStack createHeadItem(Head head, boolean withLore) {
        var playerHead = new ItemStack(Items.PLAYER_HEAD);

        var nbt = new CompoundTag();
        {
            var skullOwner = new CompoundTag();
            {
                var properties = new CompoundTag();
                {
                    var textures = new ListTag();
                    {
                        var texture = new CompoundTag();

                        var texturePayload = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/"
                                + head.texture() + "\"}}}";
                        texture.putString("Value", Base64.getEncoder().encodeToString(texturePayload.getBytes()));
                        textures.add(texture);
                    }
                    properties.put("textures", textures);
                }
                skullOwner.put("Properties", properties);
                skullOwner.put("Id", new IntArrayTag(new int[]{0, 0, 0, 0}));
            }
            nbt.put("SkullOwner", skullOwner);
        }

        playerHead.setTag(nbt);

        var displayName = Component.literal(head.name()).withStyle(style -> style.withItalic(false));
        playerHead.setHoverName(displayName);

        if (withLore) {
            var lore = new ArrayList<Component>();

            var categoryLine = Component.literal("§7Category: " + head.category());
            lore.add(categoryLine);

            if (head.tags() != null && !head.tags().isEmpty()) {
                var tagsText = String.join(", ", head.tags());
                var tagsLine = Component.literal("§7Tags: " + tagsText);
                lore.add(tagsLine);
            }

            var displayTag = playerHead.getOrCreateTagElement("display");
            var loreTag = new ListTag();
            for (var loreLine : lore) {
                loreTag.add(StringTag.valueOf(Component.Serializer.toJson(loreLine)));
            }
            displayTag.put("Lore", loreTag);
        }

        return playerHead;
    }
}