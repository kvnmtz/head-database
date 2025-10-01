package dev.kvnmtz.headdatabase.gui;

import dev.kvnmtz.headdatabase.data.HeadDataManager;
import dev.kvnmtz.headdatabase.util.SoundUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class HeadDatabaseMenu extends ChestMenu {
    
    public HeadDatabaseMenu(MenuType<?> menuType, int containerId, Inventory playerInventory, Container container, int rows) {
        super(menuType, containerId, playerInventory, container, rows);
    }
    
    @Override
    public void clicked(int slotIndex, int button, @NotNull ClickType clickType, @NotNull Player player) {
        // prevent taking items from the GUI
        if (slotIndex < this.getContainer().getContainerSize()) {
            if (!(button == 0 && clickType == ClickType.PICKUP)) {
                return;
            }

            handleCategoryClick(slotIndex, (ServerPlayer) player);
            return;
        }
        
        // allow normal inventory interactions for player inventory
        super.clicked(slotIndex, button, clickType, player);
    }
    
    private void handleCategoryClick(int slotIndex, ServerPlayer player) {
        var clickedItem = this.getContainer().getItem(slotIndex);
        if (clickedItem.isEmpty()) {
            return;
        }
        
        var tag = clickedItem.getTag();
        if (tag == null || !tag.contains("hdb_nav")) {
            handleHeadClick(clickedItem, player);
            return;
        }
        
        var hdbData = tag.getCompound("hdb_nav");
        var action = hdbData.getString("action");
        
        switch (action) {
            case "open_category":
                var categoryName = hdbData.getString("category");
                updateToCategoryBrowser(categoryName, 0);
                break;
            case "back_to_categories":
                updateToCategories();
                break;
            case "previous_page", "next_page":
                var prevPage = hdbData.getInt("page");
                var prevCategory = hdbData.getString("category");
                updateToCategoryBrowser(prevCategory, prevPage);
                break;
            case "previous_search_page", "next_search_page":
                var searchPage = hdbData.getInt("page");
                var searchQuery = hdbData.getString("query");
                updateToSearchResults(searchQuery, searchPage);
                break;
            default:
                break;
        }
    }
    
    private void handleHeadClick(ItemStack headItem, ServerPlayer player) {
        if (headItem.getItem() != Items.PLAYER_HEAD) {
            return;
        }

        if (!player.getInventory().add(headItem.copy())) {
            SoundUtils.playSoundForPlayer(player, SoundEvents.NOTE_BLOCK_BASS.value(), 0.5F, 0.7F);
        }
    }
    
    private void updateToCategoryBrowser(String category, int page) {
        for (var i = 0; i < this.getContainer().getContainerSize(); i++) {
            this.getContainer().setItem(i, ItemStack.EMPTY);
        }

        CategoryBrowserGui.populateContainer(this.getContainer(), category, page);

        this.broadcastChanges();
    }
    
    private void updateToCategories() {
        for (var i = 0; i < this.getContainer().getContainerSize(); i++) {
            this.getContainer().setItem(i, ItemStack.EMPTY);
        }

        HeadDatabaseGui.populateContainer(this.getContainer());

        this.broadcastChanges();
    }

    private void updateToSearchResults(String query, int page) {
        for (var i = 0; i < this.getContainer().getContainerSize(); i++) {
            this.getContainer().setItem(i, ItemStack.EMPTY);
        }

        var headManager = HeadDataManager.getInstance();
        var searchResults = headManager.searchHeads(query);

        HeadDatabaseGui.populateSearchResults(this.getContainer(), searchResults, query, page);

        this.broadcastChanges();
    }
    
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        // prevent shift-clicking items out of the GUI
        return ItemStack.EMPTY;
    }
}