package io.github.wlsdks.fortunecookie.dto;

public class FortuneWrapper<T> {

    private T data;          // 원본 DTO 혹은 객체
    private String fortune;  // 포춘 메시지

    public FortuneWrapper(T data, String fortune) {
        this.data = data;
        this.fortune = fortune;
    }

    public T getData() {
        return data;
    }

    public String getFortune() {
        return fortune;
    }

}
