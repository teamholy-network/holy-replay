package de.teamholy.replay.replaysystem.recording;

import java.io.Serializable;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityPose;

import de.teamholy.replay.replaysystem.utils.MetadataBuilder;
import de.teamholy.replay.utils.VersionUtil;
import de.teamholy.replay.utils.VersionUtil.VersionEnum;

public class PlayerWatcher implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5198365909032922108L;
	
	
	private boolean sneaking, burning, blocking, elytra, swimming;
	
	private String name;
	
	public PlayerWatcher(String name) {
		this.sneaking = false;
		this.burning = false;
		this.blocking = false;
		this.elytra = false;
		this.swimming = false;
		this.name = name;
	}
	
	public net.minecraft.server.v1_8_R3.DataWatcher getMetadata(MetadataBuilder builder) {

		if (isValueActive()) {
			byte sneakByte = (byte) (this.sneaking ? 0x02 : 0);
			byte burnByte = (byte) (this.burning ? 0x01 : 0);
			byte oldBlock = (byte) (this.blocking ? 0x10 : 0);
			byte elytraByte = (byte) (this.elytra ? 0x80 : 0);

			// Für 1.8: Keine Version-Checks mehr nötig
			byte value = (byte) (burnByte | sneakByte | oldBlock | elytraByte);
			builder.setValue(0, value);
		} else {
			builder.resetValue();
		}


		return builder.getData();
	}
	
	private String getActivePose() {
		if (this.sneaking) {

			return VersionUtil.isCompatible(VersionEnum.V1_14) ? "SNEAKING" : EntityPose.CROUCHING.toString();
		} else if (this.swimming) {
			
			return EntityPose.SWIMMING.toString();
		} else {
			
			return EntityPose.STANDING.toString();
		}
		
	}
	
	private boolean isValueActive() {
		return this.sneaking || this.blocking || this.burning || this.elytra || this.swimming;
	}
	
	public void setSneaking(boolean sneaking) {
		this.sneaking = sneaking;
	}
	
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}
	
	public void setBurning(boolean burning) {
		this.burning = burning;
	}
	
	public void setElytra(boolean elytra) {
		this.elytra = elytra;
	}
	
	public void setSwimming(boolean swimming) {
		this.swimming = swimming;
	}
	
	public boolean isBurning() {
		return burning;
	}
	
	public boolean isBlocking() {
		return blocking;
	}
	
	public boolean isElytra() {
		return elytra;
	}
	
	public boolean isSwimming() {
		return swimming;
	}
	
	public boolean isSneaking() {
		return sneaking;
	}
	
	public String getName() {
		return name;
	}
	
}
