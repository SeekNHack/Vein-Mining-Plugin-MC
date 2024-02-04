package it.hns.veinmining;

import org.bukkit.Material;
import org.bukkit.block.Block;
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
    @EventHandler
    public boolean onBlockBreak(BlockBreakEvent event) {
        // Prendo il player che ha rotto il blocco
        Player player = event.getPlayer();
        if (!player.hasPermission("vein.mining")){
            plugin.getPlayers().remove(player);
        }
        // Controllo se il player ha il permesso e se ha un piccone in mano e se il blocco è nel config
        if (plugin.getPlayers().contains(player) && player.getInventory().getItemInMainHand().getType().toString().contains("PICKAXE") && plugin.getConfig().getStringList("blocks").contains(event.getBlock().getType().toString())){
            int radius = plugin.getConfig().getInt("radius");
            Block block = event.getBlock();
            veinmining(player, block, radius );
        }

        return true;
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


    private void veinmining(Player player, Block block, int depth) {
        ArrayList<Block> blocks = getAdjacentBlocks(block, depth);

        for (Block b : blocks) {
            if (b.getType().toString().contains("ORE") && plugin.getConfig().getBoolean("drop-experience")) {
                player.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience(5);
            }
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

                // Danneggio l'item se la durabilità non è a 0
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getItemMeta() instanceof Damageable) {
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