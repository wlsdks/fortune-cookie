package io.github.wlsdks.fortunecookie.properties;

public enum FortuneMode {

    FORTUNE("fortune", "포춘 모드(기본)"),
    JOKE("joke", "농담 모드"),
    QUOTE("quote", "명언 모드");

    private final String mode;
    private final String description;

    FortuneMode(String mode, String description) {
        this.mode = mode;
        this.description = description;
    }

    public String getMode() {
        return mode;
    }

    public String getDescription() {
        return description;
    }

}
