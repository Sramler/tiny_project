package com.tiny.oauthserver.sys.enums;

public enum ResourceType {
    MENU(0),
    BUTTON(1),
    API(2);

    private final int code;

    ResourceType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ResourceType fromCode(Integer code) {
        if (code == null) return null;
        for (ResourceType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的资源类型 code: " + code);
    }
}