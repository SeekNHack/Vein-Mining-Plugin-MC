package it.hns.veinmining;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.enchantments.Enchantment.*;
import org.bukkit.Sound;

import java.util.*;

import org.bukkit.entity.ExperienceOrb;

public class BreakListener implements Listener {
    VeinMining plugin;

    public BreakListener(VeinMining instance) {
        plugin = instance;
    }

    private List<Block> getAdjacentBlocks(Block block) {
        List<Block> adjacentBlocks = new ArrayList<>();
        int[][] directions = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        for (int[] direction : directions) {
            Block adjacentBlock = block.getRelative(direction[0], direction[1], direction[2]);
            if (adjacentBlock.getType() == block.getType()) {
                adjacentBlocks.add(adjacentBlock);
            }
        }
        return adjacentBlocks;
    }

    @EventHandler
    public boolean onBlockBreak(BlockBreakEvent event) {
        // Prendo il player che ha rotto il blocco
        Player player = event.getPlayer();
        if (!player.hasPermission("vein.mining")) {
            plugin.getPlayers().remove(player);
        }
        // Controllo se il player ha il permesso
        if (plugin.getPlayers().contains(player))
            // Verifico per ogni arma in getWeapons se il nome dell'item è contenuto nel nome dell'item in mano
            for (String weapon : plugin.getConfigManager().getWeapons()) {
                FileConfiguration weaponConfig = plugin.getConfigManager().getWeaponConfig(weapon);
                if (weapon.equals("hand")) {
                    weapon = "air";
                }
                if (player.getInventory().getItemInMainHand().getType().toString().toLowerCase().contains(weapon)) {
                    int radius = weaponConfig.getInt("radius");
                    // Verifico che il blocco sia dentro la lista dei blocchi
                    if (weaponConfig.getStringList("blocks").contains(event.getBlock().getType().toString())) {
                        Block block = event.getBlock();
                        veinmining(player, block, radius, weaponConfig);
                        return true;
                    }
                }
            }
        return true;
    }
    // Algoritmo che trova tutti i blocchi adiacenti con raggio depth. Se il blocco non è adiacente non lo considero
    private ArrayList<Block> getAdjacentBlocks(Block block, int radius) {
        ArrayList<Block> blocks = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(block);

        while (!queue.isEmpty() && radius > 0) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Block currentBlock = queue.poll();
                visited.add(currentBlock);
                for (Block adjacentBlock : getAdjacentBlocks(currentBlock)) {
                    if (!visited.contains(adjacentBlock) && !blocks.contains(adjacentBlock)) {
                        blocks.add(adjacentBlock);
                        queue.add(adjacentBlock);
                    }
                }
            }
            radius--;
        }
        return blocks;
    }


    private void veinmining(Player player, Block block, int depth, FileConfiguration weaponConfig) {
        ArrayList<Block> blocks = getAdjacentBlocks(block, depth);

        for (Block b : blocks) {
            // Droppo la quantità di esperienza specificata nella config
            if (weaponConfig.getInt("experience") > 0)
                player.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience(weaponConfig.getInt("experience"));
            // Se l'item ha silk touch droppo il blocco intero
            if (player.getInventory().getItemInMainHand().containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH)) {
                b.breakNaturally(player.getInventory().getItemInMainHand());
            }
            else{
                b.breakNaturally();
            }
            // Se il player non è in creative mode danneggio l'item
            if (!player.getGameMode().toString().equals("CREATIVE")) {
                // Se sull'item ha unbreaking calcolo l'ammontare di danni in base al livello
                int amount;
                if (player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.DURABILITY)) {
                    int unbreakingLevel = player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.DURABILITY);
                    double probability = 100.0 / (unbreakingLevel + 1); // Calcola la probabilità
                    if (Math.random() * 100 < probability) {
                        // Sottrai vita all'arma solo se il risultato casuale è inferiore alla probabilità calcolata
                        amount = 1; // O qualsiasi altro valore desiderato
                    } else {
                        // Se il risultato casuale è maggiore o uguale alla probabilità, non sottrarre nulla
                        amount = 0;
                    }
                } else {
                    amount = 1;
                }
                // Se l'item è danneggiabile e non è al massimo di danni, danneggialo altrimenti non fare nulla
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getItemMeta() instanceof Damageable && item.getType().getMaxDurability() > 0) {
                    Damageable damageable = (Damageable) item.getItemMeta();
                    if (damageable.getDamage() < item.getType().getMaxDurability()) {
                        damageable.setDamage(damageable.getDamage() + amount);
                        item.setItemMeta(damageable);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 3, 1);
                    }
                }
            }
        }
    }
}
