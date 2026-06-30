package io.github.kimovoid.polished.mixin.client.inventory;

import io.github.kimovoid.polished.client.PolishedClient;
import io.github.kimovoid.polished.client.feature.inventorytweaks.CreativeIntegration;
import io.github.kimovoid.polished.client.feature.inventorytweaks.InventoryTweaks;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.menu.FurnaceScreen;
import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;
import net.minecraft.client.gui.screen.inventory.menu.SurvivalInventoryScreen;
import net.minecraft.crafting.SmeltingManager;
import net.minecraft.inventory.menu.InventoryMenu;
import net.minecraft.inventory.slot.CraftingResultSlot;
import net.minecraft.inventory.slot.InventorySlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(InventoryMenuScreen.class)
public abstract class InventoryMenuScreenMixin extends Screen implements InventoryTweaks {
    
    @Shadow public InventoryMenu menu;
    @Shadow protected abstract InventorySlot getSlot(int x, int y);

    @Unique private boolean enabled = true;
    @Unique private long currentTime;
    @Unique private InventorySlot slot;
    @Unique InventorySlot lastRMBSlot = null;
    @Unique InventorySlot lastLMBSlot = null;
    @Unique int lastRMBSlotId = -1;
    @Unique int lastLMBSlotId = -1;
    @Unique private ItemStack leftClickMouseTweaksPersistentStack = null;
    @Unique private ItemStack leftClickPersistentStack = null;
    @Unique private ItemStack rightClickPersistentStack = null;
    @Unique private boolean isLeftClickDragMouseTweaksStarted = false;
    @Unique private boolean isLeftClickDragStarted = false;
    @Unique private boolean isRightClickDragStarted = false;
    @Unique private final List<InventorySlot> leftClickHoveredSlots = new ArrayList<>();
    @Unique final List<InventorySlot> rightClickHoveredSlots = new ArrayList<>();
    @Unique Integer leftClickItemAmount;
    @Unique Integer rightClickItemAmount;
    @Unique final List<Integer> leftClickExistingAmount = new ArrayList<>();
    @Unique final List<Integer> rightClickExistingAmount = new ArrayList<>();
    @Unique List<Integer> leftClickAmountToFillPersistent = new ArrayList<>();

    /* True if either shift key is currently held. */
    @Unique private static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    protected void inventoryTweaks_mouseClicked(int mouseX, int mouseY, int button, CallbackInfo ci) {
        if (this.inventoryTweaks_isDisabled()) return;

        isLeftClickDragMouseTweaksStarted = false;

        /* Check if client is on a server */
        boolean isClientOnServer = minecraft.isMultiplayer();
        InventorySlot clickedSlot = this.getSlot(mouseX, mouseY);

        /* Craft maximum possible amount */
        if (isShiftDown()
                && clickedSlot instanceof CraftingResultSlot
                && clickedSlot.hasItem()
                && PolishedClient.CONFIG.craftAll.get()) {
            int itemId = clickedSlot.getItem().id;
            for (int craftingAttempts = 0; craftingAttempts < 256; craftingAttempts++) {
                if (clickedSlot.hasItem() && itemId == clickedSlot.getItem().id) {
                    this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, button, true, this.minecraft.player);
                } else {
                    break;
                }
            }
            ci.cancel();
            return;
        }

        /* Check special click behavior for current screen */
        if (minecraft.screen instanceof SurvivalInventoryScreen) {
            /* Handle shift click into armor slots */
            if (PolishedClient.CONFIG.shiftIntoArmor.get()) {
                if (inventoryTweaks_handleShiftClickIntoArmorSlots(button, clickedSlot)) {
                    /* Handle if a button was clicked */
                    super.mouseClicked(mouseX, mouseY, button);
                    ci.cancel();
                    return;
                }
            }
        } else if (minecraft.screen instanceof FurnaceScreen) {
            /* Handle shift click into furnace */
            if (PolishedClient.CONFIG.shiftIntoFurnace.get()) {
                if (inventoryTweaks_handleShiftClickIntoFurnace(button, clickedSlot)) {
                    /* Handle if a button was clicked */
                    super.mouseClicked(mouseX, mouseY, button);
                    ci.cancel();
                    return;
                }
            }
        }

        /* Right-click */
        if (button == 1) {
            /* Should click cancel Left-click + Drag */
            boolean exitFunction = inventoryTweaks_cancelLeftClickDrag(isClientOnServer)
                    || (PolishedClient.CONFIG.leftClickDrag.get() && inventoryTweaks_handleRightClick(mouseX, mouseY));

            if (exitFunction) {
                /* Handle if a button was clicked */
                super.mouseClicked(mouseX, mouseY, button);
                ci.cancel();
                return;
            }
        }

        /* Left-click */
        if (button == 0) {
            boolean exitFunction = false;

            /* Should click cancel Right-click + Drag */
            if (inventoryTweaks_cancelRightClickDrag(isClientOnServer)) {
                exitFunction = true;
            } else {
                /* Handle Left-click */
                ItemStack cursorStack = minecraft.player.inventory.getCursorItem();
                if (cursorStack != null) {
                    /* Check for double left-click fill cursor stack (checking for second click close to time of first click) */
                    if (PolishedClient.CONFIG.doubleClick.get()) {
                        if (null != clickedSlot && !clickedSlot.hasItem() && null != minecraft.world) {
                            if (5 > (minecraft.world.getTime() - currentTime)) {
                                if (inventoryTweaks_handleDoubleClickEmptyCursor(clickedSlot)) {
                                    /* Handle if a button was clicked */
                                    super.mouseClicked(mouseX, mouseY, button);
                                    ci.cancel();
                                    return;
                                }
                            }
                        }
                    }

                    if (PolishedClient.CONFIG.leftClickDrag.get()) {
                        exitFunction = inventoryTweaks_handleLeftClickWithItem(cursorStack, clickedSlot, isClientOnServer);
                    }
                } else {
                    /* Begin double left-click fill cursor stack (first click registered) */
                    if (PolishedClient.CONFIG.doubleClick.get()) {
                        if (null != clickedSlot && clickedSlot.hasItem() && null != minecraft.world) {
                            currentTime = minecraft.world.getTime();
                        }
                    }

                    exitFunction = inventoryTweaks_handleLeftClickWithoutItem(clickedSlot);
                }
            }

            if (exitFunction) {
                /* Handle if a button was clicked */
                super.mouseClicked(mouseX, mouseY, button);
                ci.cancel();
            }
        }
    }

    @Unique
    private boolean inventoryTweaks_handleShiftClickIntoArmorSlots(int button, InventorySlot clickedSlot) {
        boolean isShiftKeyDown = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
        if (isShiftKeyDown) {

            if (null != clickedSlot && clickedSlot.hasItem()) {
                SurvivalInventoryScreen inventoryScreen = (SurvivalInventoryScreen) minecraft.screen;

                if (clickedSlot != inventoryScreen.menu.slots.get(5)
                        && (clickedSlot != inventoryScreen.menu.slots.get(6))
                        && (clickedSlot != inventoryScreen.menu.slots.get(7))
                        && (clickedSlot != inventoryScreen.menu.slots.get(8))
                ) {
                    ItemStack slotStack = clickedSlot.getItem();
                    int shiftToSlot = -1;
                    boolean isPumpkin = false;

                    if (slotStack.getItem() instanceof ArmorItem) {
                        int equipmentSlot = ((ArmorItem) slotStack.getItem()).slot;

                        if (0 == equipmentSlot) {
                            if (!(inventoryScreen.menu.slots.get(5)).hasItem()) {
                                shiftToSlot = 5;
                            }
                        } else if (1 == equipmentSlot) {
                            if (!(inventoryScreen.menu.slots.get(6)).hasItem()) {
                                shiftToSlot = 6;
                            }
                        } else if (2 == equipmentSlot) {
                            if (!(inventoryScreen.menu.slots.get(7)).hasItem()) {
                                shiftToSlot = 7;
                            }
                        } else if (3 == equipmentSlot) {
                            if (!(inventoryScreen.menu.slots.get(8)).hasItem()) {
                                shiftToSlot = 8;
                            }
                        }
                    } else if (Block.PUMPKIN.id == slotStack.id) {
                        if (!(inventoryScreen.menu.slots.get(5)).hasItem()) {
                            shiftToSlot = 5;
                            isPumpkin = true;
                        }
                    }

                    if (0 <= shiftToSlot) {
                        if (null != minecraft.player.inventory.getCursorItem()) {
                            this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, button, false, this.minecraft.player);
                            this.minecraft.interactionManager.clickSlot(this.menu.networkId, (inventoryScreen.menu.slots.get(shiftToSlot)).index, button, false, this.minecraft.player);
                            this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, button, false, this.minecraft.player);
                        } else {
                            this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, button, false, this.minecraft.player);
                            this.minecraft.interactionManager.clickSlot(this.menu.networkId, (inventoryScreen.menu.slots.get(shiftToSlot)).index, button, false, this.minecraft.player);

                            if (isPumpkin) {
                                this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, button, false, this.minecraft.player);
                            }
                        }

                        if (isPumpkin) {
                            this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, button, true, this.minecraft.player);
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Unique
    private boolean inventoryTweaks_handleShiftClickIntoFurnace(int button, InventorySlot clickedSlot) {
        boolean isShiftKeyDown = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
        if (isShiftKeyDown) {

            if (null != clickedSlot && clickedSlot.hasItem()) {
                FurnaceScreen furnaceScreen = (FurnaceScreen) minecraft.screen;

                if (clickedSlot != (furnaceScreen.menu.slots.get(0))
                        && clickedSlot != (furnaceScreen.menu.slots.get(1))
                        && clickedSlot != (furnaceScreen.menu.slots.get(2))) {
                    try {
                        ItemStack slotStack = clickedSlot.getItem();
                        int shiftToSlot = -1;

                        if (null != SmeltingManager.getInstance().getResult(slotStack.getItem().id)) {
                            if (0 <= this.inventoryTweaks_canItemFitInSlot(slotStack, (furnaceScreen.menu.slots.get(0)))) {
                                shiftToSlot = 0;
                            } else {
                                shiftToSlot = -2;
                            }
                        } else {
                            FurnaceBlockEntity furnace = furnaceScreen.furnace;

                            if (0 < (furnace.getFuelTime(slotStack))) {
                                if (0 <= this.inventoryTweaks_canItemFitInSlot(slotStack, (furnaceScreen.menu.slots.get(1)))) {
                                    shiftToSlot = 1;
                                } else {
                                    shiftToSlot = -2;
                                }
                            }
                        }

                        if (-2 == shiftToSlot) {
                            return true;
                        } else if (0 <= shiftToSlot) {
                            if (null != minecraft.player.inventory.getCursorItem()) {
                                this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, button, false, this.minecraft.player);
                                this.minecraft.interactionManager.clickSlot(this.menu.networkId, (furnaceScreen.menu.slots.get(shiftToSlot)).index, button, false, this.minecraft.player);
                                this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, button, false, this.minecraft.player);
                            } else {
                                this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, button, false, this.minecraft.player);
                                this.minecraft.interactionManager.clickSlot(this.menu.networkId, (furnaceScreen.menu.slots.get(shiftToSlot)).index, button, false, this.minecraft.player);
                                if (null != minecraft.player.inventory.getCursorItem()) {
                                    this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, button, false, this.minecraft.player);
                                }
                            }

                            return true;
                        }
                    } catch (Exception ex) {
                        /* Do nothing */
                    }
                }
            }
        }

        return false;
    }

    @Unique private boolean inventoryTweaks_handleDoubleClickEmptyCursor(InventorySlot clickedSlot) {
        if (null != clickedSlot) {
            InventoryMenuScreen inventory = (InventoryMenuScreen) minecraft.screen;
            ItemStack slotStack = minecraft.player.inventory.getCursorItem();
            boolean b = false;

            /* Shift item back into player inventory */
            for (int i = 0; i < inventory.menu.slots.size(); i++) {
                if (FabricLoader.getInstance().isModLoaded("creative")) {
                    if (CreativeIntegration.shouldSkipSlot((InventoryMenuScreen) (Object) this, i)) {
                        continue;
                    }
                }

                if (inventoryTweaks_isItemInSlot(slotStack, (inventory.menu.slots.get(i)))) {
                    b = true;
                    this.minecraft.interactionManager.clickSlot(this.menu.networkId, inventory.menu.slots.get(i).index, 0, false, this.minecraft.player);
                    this.minecraft.interactionManager.clickSlot(this.menu.networkId, inventory.menu.slots.get(i).index, 0, false, this.minecraft.player);

                    ItemStack itemStack = (inventory.menu.slots.get(i)).getItem();
                    if (null != itemStack && itemStack.size == itemStack.getItem().getMaxStackSize()) {
                        this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, 0, false, this.minecraft.player);
                        this.minecraft.interactionManager.clickSlot(this.menu.networkId, inventory.menu.slots.get(i).index, 0, false, this.minecraft.player);
                        this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, 0, false, this.minecraft.player);
                        this.minecraft.interactionManager.clickSlot(this.menu.networkId, inventory.menu.slots.get(i).index, 0, false, this.minecraft.player);
                        this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, 0, false, this.minecraft.player);

                        /* Stack is full */
                        break;
                    }
                }

                ItemStack cursorStack = minecraft.player.inventory.getCursorItem();
                if (null != cursorStack && cursorStack.size == cursorStack.getItem().getMaxStackSize()) {
                    /* Stack is full */
                    break;
                }
            }
            return b;
        }

        return false;
    }

    @Inject(method = "mouseReleased", at = @At("RETURN"))
    private void inventoryTweaks_mouseReleasedOrSlotChanged(int mouseX, int mouseY, int button, CallbackInfo ci) {
        if (this.inventoryTweaks_isDisabled()) return;

        slot = getSlot(mouseX, mouseY);

        /* Clear dragging variables */
        if (button == 0) {
            inventoryTweaks_resetLeftClickDragVariables();
        } else if (button == 1) {
            inventoryTweaks_resetRightClickDragVariables();
        }

        /* Do nothing if mouse is not over a slot */
        if (slot == null) {
            return;
        }

        /* Right-click + Drag logic = distribute one item from held items to each slot */
        if (button == -1
                && Mouse.isButtonDown(1)
                && !isLeftClickDragStarted
                && !isLeftClickDragMouseTweaksStarted
                && rightClickPersistentStack != null) {
            ItemStack slotItemToExamine = slot.getItem();

            /* Do nothing if slot item does not match held item or if the slot is full */
            if (slotItemToExamine != null
                    && (!slotItemToExamine.matchesItem(rightClickPersistentStack)
                    || slotItemToExamine.size == rightClickPersistentStack.getMaxSize())) {
                return;
            }

            /* Do nothing if there are no more items to distribute */
            ItemStack cursorStack = minecraft.player.inventory.getCursorItem();
            if (cursorStack == null) {
                return;
            }

            if (!rightClickHoveredSlots.contains(slot)) {
                inventoryTweaks_handleRightClickDrag(slotItemToExamine);
            } else if (PolishedClient.CONFIG.tweakRMB.get()) {
                inventoryTweaks_handleRightClickDragMouseTweaks();
            }
        } else {
            inventoryTweaks_resetRightClickDragVariables();
        }

        /* Left-click + Drag logic = evenly distribute held items over slots */
        if (button == -1 && Mouse.isButtonDown(0) && !isRightClickDragStarted) {
            if (isLeftClickDragMouseTweaksStarted) {
                inventoryTweaks_handleLeftClickDragMouseTweaks();
            } else if (leftClickPersistentStack != null) {
                if (inventoryTweaks_handleLeftClickDrag()) {
                    return;
                }
            } else {
                inventoryTweaks_resetLeftClickDragVariables();
            }
        } else {
            inventoryTweaks_resetLeftClickDragVariables();
        }
    }

    @Unique private boolean inventoryTweaks_handleRightClick(int mouseX, int mouseY) {
        /* Get held item */
        ItemStack cursorStack = minecraft.player.inventory.getCursorItem();
        if (cursorStack == null) {
            return false;
        }

        /* Ensure a slot was clicked */
        InventorySlot clickedSlot = this.getSlot(mouseX, mouseY);
        if (clickedSlot == null) {
            return false;
        }

        /* Record how many items are in the slot */
        if (clickedSlot.getItem() != null) {
            /* Let vanilla minecraft handle right click with an item onto a different item */
            if (!cursorStack.matchesItem(clickedSlot.getItem())) {
                return false;
            }
            rightClickExistingAmount.add(clickedSlot.getItem().size);
        } else {
            rightClickExistingAmount.add(0);
        }

        /* Begin Right-click + Drag */
        if (rightClickPersistentStack == null && !isRightClickDragStarted) {
            rightClickPersistentStack = cursorStack;
            rightClickItemAmount = rightClickPersistentStack.size;
            isRightClickDragStarted = true;
        }

        /* Handle initial Right-click */
        lastRMBSlotId = clickedSlot.index;
        lastRMBSlot = clickedSlot;
        if (PolishedClient.CONFIG.preferShiftRMB.get()) {
            boolean isShiftKeyDown = isShiftDown();
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, 1, isShiftKeyDown, this.minecraft.player);

            if (isShiftKeyDown) {
                inventoryTweaks_resetRightClickDragVariables();
            }
        } else {
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, 1, false, this.minecraft.player);
        }

        return true;
    }

    @Unique private void inventoryTweaks_handleRightClickDragMouseTweaks() {
        if (slot.index == lastRMBSlotId) {
            return;
        }
        ItemStack cursorStack = minecraft.player.inventory.getCursorItem();
        if (cursorStack != null) {
            /* Distribute one item to the slot */
            lastRMBSlotId = slot.index;
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 1, false, this.minecraft.player);
        }
    }

    @Unique private void inventoryTweaks_handleRightClickDrag(ItemStack slotItemToExamine) {
        /* First slot is handled instantly in mouseClicked function */
        if (slot.index == lastRMBSlotId) {
            return;
        }

        /* Add slot to item distribution */
        if (rightClickHoveredSlots.isEmpty()) {
            rightClickHoveredSlots.add(lastRMBSlot);
        }
        rightClickHoveredSlots.add(slot);

        /* Record how many items are in the slot */
        rightClickExistingAmount.add(slotItemToExamine != null ? slotItemToExamine.size : 0);

        /* Distribute one item to the slot */
        lastRMBSlotId = slot.index;
        this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 1, false, this.minecraft.player);
    }

    @Unique private boolean inventoryTweaks_cancelRightClickDrag(boolean isClientOnServer) {
        /* Cancel Right-click + Drag */
        if (!isRightClickDragStarted || rightClickHoveredSlots.size() <= 1) {
            return false;
        }

        /* Slots cannot return to normal on a server */
        if (!isClientOnServer) {
            /* Return all slots to normal */
            minecraft.player.inventory.setCursorItem(new ItemStack(rightClickPersistentStack.id, rightClickItemAmount, rightClickPersistentStack.getDamage()));
            for (int i = 0; i < rightClickHoveredSlots.size(); i++) {
                int existing = rightClickExistingAmount.get(i);
                rightClickHoveredSlots.get(i).setItem(existing != 0
                        ? new ItemStack(rightClickPersistentStack.id, existing, rightClickPersistentStack.getDamage())
                        : null);
            }
        }

        /* Reset Right-click + Drag variables and exit function */
        inventoryTweaks_resetRightClickDragVariables();
        return true;
    }

    @Unique private void inventoryTweaks_resetRightClickDragVariables() {
        rightClickExistingAmount.clear();
        rightClickHoveredSlots.clear();
        rightClickPersistentStack = null;
        rightClickItemAmount = 0;
        isRightClickDragStarted = false;
    }

    @Unique private boolean inventoryTweaks_handleLeftClickWithItem(ItemStack cursorStack, InventorySlot clickedSlot, boolean isClientOnServer) {
        /* Ensure a slot was clicked */
        if (clickedSlot == null) {
            return false;
        }

        /* Record how many items are in the slot and how many items are needed to fill the slot */
        if (clickedSlot.getItem() != null) {
            if (cursorStack != null) {
                /* Let vanilla minecraft handle left click with an item onto any item */
                if (isClientOnServer) {
                    return false;
                }
                /* Let vanilla minecraft handle left click with an item onto a different item */
                if (!cursorStack.matchesItem(clickedSlot.getItem())) {
                    return false;
                }

                leftClickAmountToFillPersistent.add(cursorStack.getMaxSize() - clickedSlot.getItem().size);
                leftClickExistingAmount.add(clickedSlot.getItem().size);
            }
        } else {
            leftClickAmountToFillPersistent.add(cursorStack.getMaxSize());
            leftClickExistingAmount.add(0);
        }

        /* Begin Left-click + Drag */
        if (leftClickPersistentStack == null && !isLeftClickDragStarted) {
            leftClickPersistentStack = cursorStack;
            leftClickItemAmount = leftClickPersistentStack.size;
            isLeftClickDragStarted = true;
        }

        /* Handle initial Left-click */
        lastLMBSlotId = clickedSlot.index;
        lastLMBSlot = clickedSlot;
        if (PolishedClient.CONFIG.preferShiftLMB.get()) {
            boolean isShiftKeyDown = isShiftDown();
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, 0, isShiftKeyDown, this.minecraft.player);

            if (isShiftKeyDown) {
                inventoryTweaks_resetLeftClickDragVariables();
                leftClickMouseTweaksPersistentStack = cursorStack;
                isLeftClickDragMouseTweaksStarted = true;
            }
        } else {
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, 0, false, this.minecraft.player);
        }

        return true;
    }

    @Unique private boolean inventoryTweaks_handleLeftClickWithoutItem(InventorySlot clickedSlot) {
        isLeftClickDragMouseTweaksStarted = true;

        /* Ensure a slot was clicked */
        if (clickedSlot == null) {
            /* Get info for MouseTweaks `Left-Click + Drag` mechanics */
            leftClickMouseTweaksPersistentStack = null;
            return false;
        }

        /* Get info for MouseTweaks `Left-Click + Drag` mechanics */
        leftClickMouseTweaksPersistentStack = clickedSlot.getItem();

        /* Handle initial Left-click */
        lastLMBSlotId = clickedSlot.index;
        lastLMBSlot = clickedSlot;
        this.minecraft.interactionManager.clickSlot(this.menu.networkId, clickedSlot.index, 0, isShiftDown(), this.minecraft.player);

        return true;
    }

    @Unique private void inventoryTweaks_handleLeftClickDragMouseTweaks() {
        if (slot.index == lastLMBSlotId) {
            return;
        }
        lastLMBSlotId = slot.index;

        ItemStack slotItemToExamine = slot.getItem();
        if (slotItemToExamine == null) {
            return;
        }

        if (PolishedClient.CONFIG.shiftClickAnyLMB.get() && isShiftDown()) {
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 0, true, this.minecraft.player);
        }

        if (leftClickMouseTweaksPersistentStack == null
                || !slotItemToExamine.matchesItem(leftClickMouseTweaksPersistentStack)) {
            return;
        }

        if (isShiftDown()) {
            if (PolishedClient.CONFIG.tweakLMBShiftClick.get()) {
                this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 0, true, this.minecraft.player);
            }
            return;
        }

        if (!PolishedClient.CONFIG.tweakLMBPickUp.get()) {
            return;
        }

        ItemStack cursorStack = minecraft.player.inventory.getCursorItem();

        if (cursorStack == null) {
            /* Pick up items from slot */
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 0, false, this.minecraft.player);
        } else if (cursorStack.size < leftClickMouseTweaksPersistentStack.getMaxSize()) {
            int amountAbleToPickUp = leftClickMouseTweaksPersistentStack.getMaxSize() - cursorStack.size;
            int amountInSlot = slotItemToExamine.size;

            /* Pick up items from slot */
            if (amountInSlot <= amountAbleToPickUp) {
                this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 0, false, this.minecraft.player);
                this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 0, false, this.minecraft.player);
            } else if (cursorStack.size == leftClickMouseTweaksPersistentStack.getMaxSize()) {
                slot.setItem(new ItemStack(leftClickMouseTweaksPersistentStack.id, cursorStack.size, leftClickMouseTweaksPersistentStack.getDamage()));
                minecraft.player.inventory.setCursorItem(new ItemStack(leftClickMouseTweaksPersistentStack.id, amountInSlot, leftClickMouseTweaksPersistentStack.getDamage()));
            } else {
                this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 0, false, this.minecraft.player);

                slotItemToExamine = slot.getItem();
                cursorStack = minecraft.player.inventory.getCursorItem();
                amountInSlot = slotItemToExamine.size;

                slot.setItem(new ItemStack(leftClickMouseTweaksPersistentStack.id, cursorStack.size, leftClickMouseTweaksPersistentStack.getDamage()));
                minecraft.player.inventory.setCursorItem(new ItemStack(leftClickMouseTweaksPersistentStack.id, amountInSlot, leftClickMouseTweaksPersistentStack.getDamage()));
            }
        }
    }

    @Unique private boolean inventoryTweaks_handleLeftClickDrag() {
        /* Do nothing if slot has already been added to Left-click + Drag logic */
        if (leftClickHoveredSlots.contains(slot)) {
            return false;
        }

        ItemStack slotItemToExamine = slot.getItem();
        boolean isClientOnServer = minecraft.isMultiplayer();

        /* Do nothing if slot item does not match held item */
        if (slotItemToExamine != null) {
            if (isClientOnServer) {
                return true;
            }
            if (!slotItemToExamine.matchesItem(leftClickPersistentStack)) {
                return true;
            }
        }

        /* Do nothing if there are no more items to distribute */
        if ((double) leftClickItemAmount / (double) leftClickHoveredSlots.size() == 1.0) {
            return true;
        }

        /* First slot is handled instantly in mouseClicked function */
        if (slot.index == lastLMBSlotId) {
            return false;
        }

        /* Add slot to item distribution */
        if (leftClickHoveredSlots.isEmpty()) {
            leftClickHoveredSlots.add(lastLMBSlot);
        }
        leftClickHoveredSlots.add(slot);

        /* Record how many items are in the slot and how many items are needed to fill the slot */
        if (slotItemToExamine != null) {
            leftClickAmountToFillPersistent.add(leftClickPersistentStack.getMaxSize() - slotItemToExamine.size);
            leftClickExistingAmount.add(slotItemToExamine.size);
        } else {
            leftClickAmountToFillPersistent.add(leftClickPersistentStack.getMaxSize());
            leftClickExistingAmount.add(0);
        }

        /* Slots cannot return to normal on a server */
        List<Integer> leftClickAmountToFill = new ArrayList<>();
        if (!isClientOnServer) {
            /* Return all slots to normal */
            minecraft.player.inventory.setCursorItem(new ItemStack(leftClickPersistentStack.id, leftClickItemAmount, leftClickPersistentStack.getDamage()));
            for (int i = 0; i < leftClickHoveredSlots.size(); i++) {
                leftClickAmountToFill.add(leftClickAmountToFillPersistent.get(i));
                int existing = leftClickExistingAmount.get(i);
                leftClickHoveredSlots.get(i).setItem(existing != 0
                        ? new ItemStack(leftClickPersistentStack.id, existing, leftClickPersistentStack.getDamage())
                        : null);
            }
        }

        /* Prepare to distribute over slots */
        int numberOfSlotsRemainingToFill = leftClickHoveredSlots.size();
        int itemsPerSlot = leftClickItemAmount / numberOfSlotsRemainingToFill;
        int leftClickRemainingItemAmount = leftClickItemAmount;
        boolean rerunLoop;

        /* Slots cannot return to normal on a server */
        if (!isClientOnServer) {
            /* Distribute fewer items to slots whose max stack size will be filled */
            do {
                rerunLoop = false;
                itemsPerSlot = leftClickRemainingItemAmount / numberOfSlotsRemainingToFill;

                if (itemsPerSlot != 0) {
                    for (int i = 0; i < leftClickAmountToFill.size(); i++) {
                        int toFill = leftClickAmountToFill.get(i);
                        if (toFill != 0 && toFill < itemsPerSlot) {
                            /* Just fill the slot and return */
                            for (int fillIndex = 0; fillIndex < toFill; fillIndex++) {
                                this.minecraft.interactionManager.clickSlot(this.menu.networkId, leftClickHoveredSlots.get(i).index, 1, false, this.minecraft.player);
                            }

                            leftClickRemainingItemAmount -= toFill;
                            leftClickAmountToFill.set(i, 0);
                            numberOfSlotsRemainingToFill--;
                            rerunLoop = true;
                        }
                    }
                }
            } while (rerunLoop && numberOfSlotsRemainingToFill > 0);
        } else {
            /* Return slots to normal on when client is on a server */
            for (int i = 0; i < leftClickHoveredSlots.size() - 1; i++) {
                if (leftClickHoveredSlots.get(i).hasItem() && leftClickHoveredSlots.size() > 1) {
                    if (minecraft.player.inventory.getCursorItem() != null) {
                        this.minecraft.interactionManager.clickSlot(this.menu.networkId, leftClickHoveredSlots.get(i).index, 0, false, this.minecraft.player);
                    }
                    this.minecraft.interactionManager.clickSlot(this.menu.networkId, leftClickHoveredSlots.get(i).index, 0, false, this.minecraft.player);
                }
            }
        }

        /* Distribute remaining items evenly over remaining slots that were not already filled to max stack size */
        for (int i = 0; i < leftClickHoveredSlots.size(); i++) {
            int remainingToFill = isClientOnServer ? leftClickAmountToFillPersistent.get(i) : leftClickAmountToFill.get(i);
            if (remainingToFill != 0) {
                for (int addIndex = 0; addIndex < itemsPerSlot; addIndex++) {
                    this.minecraft.interactionManager.clickSlot(this.menu.networkId, leftClickHoveredSlots.get(i).index, 1, false, this.minecraft.player);
                }
            }
        }

        return false;
    }

    @Unique private boolean inventoryTweaks_cancelLeftClickDrag(boolean isClientOnServer) {
        /* Cancel Left-click + Drag */
        if (!isLeftClickDragStarted || leftClickHoveredSlots.size() <= 1) {
            return false;
        }

        /* Check if client is running on a server or not */
        if (!isClientOnServer) {
            /* Return all slots to normal */
            minecraft.player.inventory.setCursorItem(new ItemStack(leftClickPersistentStack.id, leftClickItemAmount, leftClickPersistentStack.getDamage()));
            for (int i = 0; i < leftClickHoveredSlots.size(); i++) {
                int existing = leftClickExistingAmount.get(i);
                leftClickHoveredSlots.get(i).setItem(existing != 0
                        ? new ItemStack(leftClickPersistentStack.id, existing, leftClickPersistentStack.getDamage())
                        : null);
            }
        } else {
            /* Return slots to normal on when client is on a server */
            for (int i = 0; i < leftClickHoveredSlots.size() - 1; i++) {
                if (leftClickHoveredSlots.get(i).hasItem() && leftClickHoveredSlots.size() > 1) {
                    if (minecraft.player.inventory.getCursorItem() != null) {
                        this.minecraft.interactionManager.clickSlot(this.menu.networkId, leftClickHoveredSlots.get(i).index, 0, false, this.minecraft.player);
                    }
                    this.minecraft.interactionManager.clickSlot(this.menu.networkId, leftClickHoveredSlots.get(i).index, 0, false, this.minecraft.player);
                }
            }
            int lastIndex = leftClickHoveredSlots.size() - 1;
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, leftClickHoveredSlots.get(lastIndex).index, 0, false, this.minecraft.player);
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, leftClickHoveredSlots.get(lastIndex).index, 0, false, this.minecraft.player);
        }

        /* Reset Left-click + Drag variables and exit function */
        inventoryTweaks_resetLeftClickDragVariables();
        return true;
    }

    @Unique private void inventoryTweaks_resetLeftClickDragVariables() {
        leftClickExistingAmount.clear();
        leftClickAmountToFillPersistent.clear();
        leftClickHoveredSlots.clear();
        leftClickPersistentStack = null;
        leftClickMouseTweaksPersistentStack = null;
        leftClickItemAmount = 0;
        isLeftClickDragStarted = false;
        isLeftClickDragMouseTweaksStarted = false;
    }

    @Unique
    private boolean inventoryTweaks_isItemInSlot(ItemStack itemStack, InventorySlot slotToCheck) {
        ItemStack slotStack = slotToCheck.getItem();

        if (null == slotStack) {
            /* Slot does not have item */
            return false;
        } else return itemStack.matchesItem(slotStack);
    }

    @Unique
    private int inventoryTweaks_canItemFitInSlot(ItemStack itemStack, InventorySlot slotToCheck) {
        ItemStack slotStack = slotToCheck.getItem();

        if (null == slotStack) {
            /* Slot is open */
            return 1;
        } else if (itemStack.matchesItem(slotStack)) {
            if (slotStack.size == slotStack.getMaxSize()) {
                /* Slot is taken */
                return -1;
            } else {
                /* Slot is partially empty and item matches */
                return 0;
            }
        }

        /* Slot is taken */
        return -1;
    }

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemRenderer;renderGuiItemWithEnchantmentGlint(Lnet/minecraft/client/render/TextRenderer;Lnet/minecraft/client/render/texture/TextureManager;Lnet/minecraft/item/ItemStack;II)V"))
    private void inventoryTweaks_renderSlot(InventorySlot slot, CallbackInfo ci) {
        if (this.inventoryTweaks_isDisabled() || !PolishedClient.CONFIG.dragGraphics.get()) {
            return;
        }

        if (rightClickHoveredSlots.contains(slot) || leftClickHoveredSlots.contains(slot)) {
            fill(slot.x, slot.y, slot.x + 16, slot.y + 16, -2130706433);
        }
    }

    @Inject(method = "keyPressed", at = @At("RETURN"))
    private void inventoryTweaks_keyPressed(char chr, int key, CallbackInfo ci) {
        if (this.inventoryTweaks_isDisabled()) return;

        if (this.slot == null) {
            return;
        }

        if (PolishedClient.CONFIG.dropKeyInv.get() && key == Minecraft.INSTANCE.options.dropKey.keyCode) {
            if (this.minecraft.player.inventory.getCursorItem() != null) {
                return;
            }

            this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 0, false, this.minecraft.player);
            int dropButton = PolishedClient.CONFIG.ctrlDropStack.get() && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 0 : 1;
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, -999, dropButton, false, this.minecraft.player);
            this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 0, false, this.minecraft.player);
        }

        if (PolishedClient.CONFIG.hotkeySwap.get()) {
            for (int i = 1; i < 10; i++) {
                int keyCode = PolishedClient.INSTANCE.keyBindingHandler.getKeyFromCode(i + 1);
                if (keyCode == key) {
                    int s = (this.menu.slots.size() - 10) + i;
                    if (FabricLoader.getInstance().isModLoaded("creative")) {
                        s = CreativeIntegration.getSlot((InventoryMenuScreen) (Object) this, s);
                    }

                    if (this.minecraft.player.inventory.getCursorItem() == null) {
                        this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 0, false, this.minecraft.player);
                    }
                    this.minecraft.interactionManager.clickSlot(this.menu.networkId, s, 0, false, this.minecraft.player);
                    this.minecraft.interactionManager.clickSlot(this.menu.networkId, slot.index, 0, false, this.minecraft.player);
                }
            }
        }
    }

    @Unique
    private boolean inventoryTweaks_isDisabled() {
        return !PolishedClient.CONFIG.enableInventoryTweaks.get() || !this.enabled;
    }

    @Override
    public void inventoryTweaks_enableTweaks(boolean b) {
        this.enabled = b;
    }
}
