package com.github.maxopoly.network;

import com.github.maxopoly.OreLogger;
import com.github.maxopoly.logging.StoneBreakCounter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CPacketPlayerDigging;

public class BlockBreakHandler extends ChannelOutboundHandlerAdapter {

	private StoneBreakCounter sbCounter;

	public BlockBreakHandler(StoneBreakCounter sbCounter) {
		this.sbCounter = sbCounter;
	}

	/**
	 * Called whenever a packet is passed to this handler through the pipe. We use the super classes behavior to pass the
	 * packet on after doing what we need to do
	 */
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		dissectPacket(msg);
		super.write(ctx, msg, promise);
	}

	public void dissectPacket(Object msg) {
		if (!(msg instanceof CPacketPlayerDigging)) {
			return;
		}
		CPacketPlayerDigging packet = (CPacketPlayerDigging) msg;
		if (packet.getAction() != CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
			// even though the name may suggest otherwise, STOP_DESTROY_BLOCK means the player successfully broke the block
			return;
		}
		// at this point we have confirmed that some kind of block was broken, so we check what kind. We can use forge
		// events for just any kind of interaction in general, but not for block breaks specificially, so we pull the item
		// the player used to mine and the block he mined from the interact event
		if (OreLogger.instance.getListener().getCachedMaterial() == Material.ROCK
				&& OreLogger.instance.getListener().getCachedVariant() == 0) {
			// confirmed stone break
			sbCounter.breakOccured(OreLogger.instance.getListener().getCachedBlockPos());
		}
	}
}
