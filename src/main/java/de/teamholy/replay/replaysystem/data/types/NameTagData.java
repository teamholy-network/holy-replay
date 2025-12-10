package de.teamholy.replay.replaysystem.data.types;

/**
 * Speichert NameTag-Informationen für einen Spieler.
 * Enthält TabList Prefix/Suffix und DisplayName Prefix/Suffix.
 */
public class NameTagData extends PacketData {

    private static final long serialVersionUID = 1234567890123456789L;

    private String tabListPrefix;
    private String tabListSuffix;
    private String displayNamePrefix;
    private String displayNameSuffix;

    /**
     * Erstellt neue NameTag-Daten.
     *
     * @param tabListPrefix Prefix für die TabList (kann null sein)
     * @param tabListSuffix Suffix für die TabList (kann null sein)
     * @param displayNamePrefix Prefix für den DisplayName (kann null sein)
     * @param displayNameSuffix Suffix für den DisplayName (kann null sein)
     */
    public NameTagData(String tabListPrefix, String tabListSuffix,
                       String displayNamePrefix, String displayNameSuffix) {
        this.tabListPrefix = tabListPrefix;
        this.tabListSuffix = tabListSuffix;
        this.displayNamePrefix = displayNamePrefix;
        this.displayNameSuffix = displayNameSuffix;
    }

    public String getTabListPrefix() {
        return tabListPrefix;
    }

    public String getTabListSuffix() {
        return tabListSuffix;
    }

    public String getDisplayNamePrefix() {
        return displayNamePrefix;
    }

    public String getDisplayNameSuffix() {
        return displayNameSuffix;
    }

    public boolean hasTabListPrefix() {
        return tabListPrefix != null;
    }

    public boolean hasTabListSuffix() {
        return tabListSuffix != null;
    }

    public boolean hasDisplayNamePrefix() {
        return displayNamePrefix != null;
    }

    public boolean hasDisplayNameSuffix() {
        return displayNameSuffix != null;
    }
}

