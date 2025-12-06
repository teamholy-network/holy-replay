package de.teamholy.replay.replaysystem.data.types;

import java.util.UUID;





public class SpawnData extends PacketData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7896939862437693109L;
	
	private UUID uuid;
	
	private LocationData location;
	
	private SignatureData signature;
	
	private String displayName;

	public SpawnData(UUID uuid, LocationData location, SignatureData signature) {
		this.uuid = uuid;
		this.location = location;
		this.signature = signature;
	}
	
	public SpawnData(UUID uuid, LocationData location, SignatureData signature, String displayName) {
		this.uuid = uuid;
		this.location = location;
		this.signature = signature;
		this.displayName = displayName;
	}

	public UUID getUuid() {
		return uuid;
	}
	
	public LocationData getLocation() {
		return location;
	}
	
	public SignatureData getSignature() {
		return signature;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

}
