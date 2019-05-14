package io.flixion.misc;



import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.flixion.main.FAIOPlugin;

public class SilentTNT {
	public SilentTNT () {
		
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(FAIOPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
			public void onPacketSending(PacketEvent event) {
				if (event.getPacket().getStrings().read(0).equals("random.explode")) {
					event.setCancelled(true);
				}
			}
		
		});
	}
}
