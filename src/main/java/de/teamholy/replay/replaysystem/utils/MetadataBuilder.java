package de.teamholy.replay.replaysystem.utils;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.server.v1_8_R3.DataWatcher;

public class MetadataBuilder {

    private DataWatcher dataWatcher;

    public MetadataBuilder() {
        this.dataWatcher = new DataWatcher(null);
    }
    /**
     * Erstellt MetadataBuilder aus WrappedDataWatcher (für Kompatibilität)
     */
    public MetadataBuilder(WrappedDataWatcher wrappedDataWatcher) {
        if (wrappedDataWatcher != null) {
            this.dataWatcher = (DataWatcher) wrappedDataWatcher.getHandle();
        } else {
            this.dataWatcher = new DataWatcher(null);
        }
    }

    public MetadataBuilder setValue(int index, Object value) {
        try {
            if (this.dataWatcher.c() != null) {
                this.dataWatcher.watch(index, value);
            } else {
                this.dataWatcher.a(index, value);
            }
        } catch (Exception e) {
            try {
                this.dataWatcher.a(index, value);
            } catch (Exception ignored) {
                this.dataWatcher.watch(index, value);
            }
        }
        return this;
    }

    public MetadataBuilder setByte(int index, byte value) {
        return setValue(index, value);
    }

    public MetadataBuilder setInteger(int index, int value) {
        return setValue(index, value);
    }

    public MetadataBuilder setInvisible() {
        return setValue(0, (byte) 0x20);
    }

    public MetadataBuilder setCrouched() {
        return setValue(0, (byte) 0x02);
    }

    public MetadataBuilder resetValue() {
        return setValue(0, (byte) 0);
    }

    public MetadataBuilder setArrows(int amount) {
        return setValue(9, amount);
    }

    public MetadataBuilder setGlowing() {
        return setValue(0, (byte) 0x20);
    }

    public MetadataBuilder setSilent() {
        return setValue(4, true);
    }

    public MetadataBuilder setNoGravity() {
        return setValue(5, true);
    }

    public MetadataBuilder setHealth(float amount) {
        return setValue(6, amount);
    }

    public MetadataBuilder setAir(int amount) {
        return setValue(1, amount);
    }

    public MetadataBuilder enableSkinParts() {
        byte skinFlags = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        return setValue(10, skinFlags);
    }

    /**
     * Gibt NMS DataWatcher zurück (wie NPCEntry verwendet)
     */
    public DataWatcher getData() {
        enableSkinParts();
        return this.dataWatcher;
    }
}

