package com.tantn.autoeat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AutoEat.MODID)
public final class AutoEatEvents {
    private static final int MAX_FOOD_LEVEL = 20;
    private static final int LOW_HEALTH_PERCENT = 85;
    private static final int LOW_HUNGER_EAT_LEVEL = MAX_FOOD_LEVEL * 70 / 100; // 70%
    private static final int EAT_CHECK_INTERVAL_TICKS = 80;
    private static final String MESSAGES_RESOURCE = "/autoeat_messages.txt";
    private static final List<String> EAT_MESSAGES = loadEatMessages();

    private AutoEatEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (serverPlayer.isCreative() || serverPlayer.isSpectator()) {
            return;
        }

        FoodData foodData = serverPlayer.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        boolean lowHealth = isHealthAtOrBelowPercent(serverPlayer, LOW_HEALTH_PERCENT);

        if (!shouldEat(foodData, foodLevel, lowHealth)) {
            return;
        }

        // Eat exactly one item every 80 ticks when the thresholds match.
        if (serverPlayer.tickCount % EAT_CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        Inventory inventory = serverPlayer.getInventory();
        boolean consumed = consumeSingleItemFromTop(serverPlayer, inventory, foodData);

        if (consumed) {
            inventory.setChanged();
            serverPlayer.containerMenu.broadcastChanges();
        }
    }

    private static boolean shouldEat(FoodData foodData, int foodLevel, boolean lowHealth) {
        if (lowHealth) {
            return foodData.needsFood();
        }

        return foodLevel < LOW_HUNGER_EAT_LEVEL;
    }

    private static boolean isHealthAtOrBelowPercent(Player player, int healthPercent) {
        return player.getHealth() <= player.getMaxHealth() * healthPercent / 100.0F;
    }

    private static boolean consumeSingleItemFromTop(ServerPlayer player, Inventory inventory, FoodData foodData) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            FoodProperties properties = stack.getFoodProperties(player);
            if (properties == null) {
                continue;
            }

            foodData.eat(properties.nutrition(), properties.saturation());
            stack.shrink(1);
            sendEatChat(player, stack.getCount());
            return true;
        }

        return false;
    }

    private static void sendEatChat(ServerPlayer player, int remainingInStack) {
        player.sendSystemMessage(Component.literal("[AutoEat] " + remainingInStack + " | " + randomEatMessage()));
    }

    private static String randomEatMessage() {
        return EAT_MESSAGES.get(ThreadLocalRandom.current().nextInt(EAT_MESSAGES.size()));
    }

    private static List<String> loadEatMessages() {
        try (InputStream stream = AutoEatEvents.class.getResourceAsStream(MESSAGES_RESOURCE)) {
            if (stream == null) {
                AutoEat.LOGGER.warn("Could not find {}. Falling back to default messages.", MESSAGES_RESOURCE);
                return List.of("AutoEat active.");
            }

            List<String> lines = new ArrayList<>(512);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        lines.add(trimmed);
                    }
                }
            }

            if (lines.isEmpty()) {
                AutoEat.LOGGER.warn("{} is empty. Falling back to default messages.", MESSAGES_RESOURCE);
                return List.of("AutoEat active.");
            }

            return Collections.unmodifiableList(lines);
        } catch (IOException exception) {
            AutoEat.LOGGER.warn("Failed reading {}. Falling back to default messages.", MESSAGES_RESOURCE, exception);
            return List.of("AutoEat active.");
        }
    }
}
