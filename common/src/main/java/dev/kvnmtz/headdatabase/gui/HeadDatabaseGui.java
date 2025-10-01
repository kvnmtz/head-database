package dev.kvnmtz.headdatabase.gui;

import dev.kvnmtz.headdatabase.data.HeadDataManager;
import dev.kvnmtz.headdatabase.model.Head;
import dev.kvnmtz.headdatabase.util.HeadItemBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public abstract class HeadDatabaseGui {

    public static void openCategoryGui(ServerPlayer player) {
        var headManager = HeadDataManager.getInstance();
        var categories = headManager.getCategories();

        var menuProvider = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.literal("Head Database");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory,
                                                    @NotNull Player player) {
                var container = new SimpleContainer(54);
                populateCategoryContainer(container, headManager, categories);
                return new HeadDatabaseMenu(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6);
            }
        };

        player.openMenu(menuProvider);
    }

    public static void openSearchResultsGui(ServerPlayer player, List<Head> searchResults, String query) {
        var menuProvider = new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.literal("Head Database");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory,
                                                    @NotNull Player player) {
                var container = new SimpleContainer(54);
                populateSearchResultsContainer(container, searchResults, query, 0);
                return new HeadDatabaseMenu(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6);
            }
        };

        player.openMenu(menuProvider);
    }

    public static void populateContainer(Container container) {
        var headManager = HeadDataManager.getInstance();
        var categories = headManager.getCategories();
        populateCategoryContainer(container, headManager, categories);
    }

    public static void populateSearchResults(Container container, List<Head> searchResults, String query, int page) {
        populateSearchResultsContainer(container, searchResults, query, page);
    }

    private static void populateCategoryContainer(Container container, HeadDataManager headManager,
                                                  Set<String> categories) {
        var slot = 0;
        var maxSlots = container.getContainerSize();

        for (var category : categories) {
            if (slot >= maxSlots) {
                break;
            }

            var headsInCategory = headManager.getHeadsByCategory(category);
            if (headsInCategory.isEmpty()) {
                continue;
            }

            var representativeHead = headsInCategory.get(0);
            var categoryItem = HeadItemBuilder.createHeadItem(representativeHead, false);

            categoryItem.setHoverName(
                    Component.literal("§6" + category + " §7(" + headsInCategory.size() + " heads)"));

            addCategoryNBT(categoryItem, category);

            container.setItem(slot, categoryItem);
            slot++;
        }
    }

    private static void addCategoryNBT(ItemStack item, String category) {
        var tag = item.getOrCreateTag();
        var hdbData = new CompoundTag();
        hdbData.putString("action", "open_category");
        hdbData.putString("category", category);
        tag.put("hdb_nav", hdbData);
    }

    private static final int HEADS_PER_PAGE = 45; // 9x5 rows for heads, bottom row for navigation

    private static void populateSearchResultsContainer(Container container, List<Head> searchResults, String query, int page) {
        var startIndex = page * HEADS_PER_PAGE;
        var endIndex = Math.min(startIndex + HEADS_PER_PAGE, searchResults.size());

        var slot = 0;
        for (var i = startIndex; i < endIndex; i++) {
            var head = searchResults.get(i);
            var headItem = HeadItemBuilder.createHeadItem(head);
            container.setItem(slot, headItem);
            slot++;
        }

        addSearchNavigationItems(container, query, page, searchResults.size());
    }

    private static void addSearchNavigationItems(Container container, String query, int page, int totalHeads) {
        var filler = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        filler.setHoverName(Component.literal(""));
        for (var i = 9 * 5; i < 9 * 6; i++) {
            container.setItem(i, filler);
        }

        var totalPages = (int) Math.ceil((double) totalHeads / HEADS_PER_PAGE);

        var backButton = new ItemStack(Items.ARROW);
        backButton.setHoverName(Component.literal("§aBack to Categories"));
        addSearchNavigationNBT(backButton, "back_to_categories", "", 0);
        container.setItem(45, backButton);

        if (page > 0) {
            var prevButton = new ItemStack(Items.PAPER);
            prevButton.setHoverName(Component.literal("§ePrevious Page"));
            addSearchNavigationNBT(prevButton, "previous_search_page", query, page - 1);
            container.setItem(46, prevButton);
        }

        var pageInfo = new ItemStack(Items.BOOK);
        pageInfo.setHoverName(Component.literal("§7Page " + (page + 1) + " of " + totalPages + " §8(" + totalHeads + " results)"));
        container.setItem(49, pageInfo);

        if (page < totalPages - 1) {
            var nextButton = new ItemStack(Items.PAPER);
            nextButton.setHoverName(Component.literal("§eNext Page"));
            addSearchNavigationNBT(nextButton, "next_search_page", query, page + 1);
            container.setItem(52, nextButton);
        }
    }

    private static void addSearchNavigationNBT(ItemStack item, String action, String query, int page) {
        var tag = item.getOrCreateTag();
        var hdbData = new CompoundTag();
        hdbData.putString("action", action);
        hdbData.putString("query", query);
        hdbData.putInt("page", page);
        tag.put("hdb_nav", hdbData);
    }
}