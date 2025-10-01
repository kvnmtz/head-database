package dev.kvnmtz.headdatabase.gui;

import dev.kvnmtz.headdatabase.data.HeadDataManager;
import dev.kvnmtz.headdatabase.model.Head;
import dev.kvnmtz.headdatabase.util.HeadItemBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public abstract class CategoryBrowserGui {

    private static final int HEADS_PER_PAGE = 45; // 9x5 rows for heads, bottom row for navigation

    public static void populateContainer(Container container, String category, int page) {
        var headManager = HeadDataManager.getInstance();
        var heads = headManager.getHeadsByCategory(category);
        populateCategoryBrowser(container, heads, category, page);
    }
    
    private static void populateCategoryBrowser(Container container, List<Head> heads, String category, int page) {
        var startIndex = page * HEADS_PER_PAGE;
        var endIndex = Math.min(startIndex + HEADS_PER_PAGE, heads.size());

        var slot = 0;
        for (var i = startIndex; i < endIndex; i++) {
            var head = heads.get(i);
            var headItem = HeadItemBuilder.createHeadItem(head);
            container.setItem(slot, headItem);
            slot++;
        }

        addNavigationItems(container, category, page, heads.size());
    }
    
    private static void addNavigationItems(Container container, String category, int page, int totalHeads) {
        var filler = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        filler.setHoverName(Component.literal(""));
        for (var i = 9 * 5; i < 9 * 6; i++) {
            container.setItem(i, filler);
        }

        var totalPages = (int) Math.ceil((double) totalHeads / HEADS_PER_PAGE);

        var backButton = new ItemStack(Items.ARROW);
        backButton.setHoverName(Component.literal("§aBack to Categories"));
        addNavigationNBT(backButton, "back_to_categories", "", 0);
        container.setItem(45, backButton);

        if (page > 0) {
            var prevButton = new ItemStack(Items.PAPER);
            prevButton.setHoverName(Component.literal("§ePrevious Page"));
            addNavigationNBT(prevButton, "previous_page", category, page - 1);
            container.setItem(46, prevButton);
        }

        var pageInfo = new ItemStack(Items.BOOK);
        pageInfo.setHoverName(Component.literal("§7Page " + (page + 1) + " of " + totalPages));
        container.setItem(49, pageInfo);

        if (page < totalPages - 1) {
            var nextButton = new ItemStack(Items.PAPER);
            nextButton.setHoverName(Component.literal("§eNext Page"));
            addNavigationNBT(nextButton, "next_page", category, page + 1);
            container.setItem(52, nextButton);
        }
    }
    
    private static void addNavigationNBT(ItemStack item, String action, String category, int page) {
        var tag = item.getOrCreateTag();
        var hdbData = new net.minecraft.nbt.CompoundTag();
        hdbData.putString("action", action);
        hdbData.putString("category", category);
        hdbData.putInt("page", page);
        tag.put("hdb_nav", hdbData);
    }
}