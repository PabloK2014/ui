package com.myaddon.iuaddon.events;

import com.denfop.tiles.reactors.TileEntityBaseNuclearReactorElectric;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReactorFixHandler {

    private int tickCounter = 0;
    private static final int CHECK_INTERVAL = 100; // Check every 5 seconds (100 ticks)

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }

        // Run only periodically to save performance
        if (tickCounter++ % CHECK_INTERVAL != 0) {
            return;
        }

        fixReactors(event.world);
    }

    private void fixReactors(World world) {
        // Iterate over a copy of loaded tile entities to avoid CME during our own iteration
        // although loadedTileEntityList is usually safe to iterate in WorldTickEvent
        for (TileEntity te : new ArrayList<>(world.loadedTileEntityList)) {
            if (te instanceof TileEntityBaseNuclearReactorElectric) {
                TileEntityBaseNuclearReactorElectric reactor = (TileEntityBaseNuclearReactorElectric) te;
                
                // Check if the list is an ArrayList (unsafe) and replace it with CopyOnWriteArrayList (safe)
                if (reactor.reactorsItemList instanceof ArrayList) {
                    // System.out.println("IU Addon: Fixing reactor at " + reactor.getPos());
                    reactor.reactorsItemList = new CopyOnWriteArrayList<>(reactor.reactorsItemList);
                }
            }
        }
    }
}
