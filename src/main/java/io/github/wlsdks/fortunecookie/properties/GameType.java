package io.github.wlsdks.fortunecookie.properties;

public enum GameType {

    UNSPECIFIED("unspecified", "미지정"),
    NUMBER("number", "숫자 맞히기"),
    QUIZ("quiz", "퀴즈"),
    WORD("word", "단어 맞히기");

    private final String type;
    private final String description;

    GameType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

}
