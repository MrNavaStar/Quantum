package mrnavastar.quantum.util.datatypes;

public class Mod {
    private final String name;
    private final String version;
    private final String type;

    public Mod(String name, String version, String type) {
        this.name = name;
        this.version = version;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getVersionId() {
        return version;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " | " + version;
    }
}
