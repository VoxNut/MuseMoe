package com.javaweb.enums;

import lombok.Getter;

@Getter
public enum RoleType implements BaseEnum {

    FREE("Free User"),

    PREMIUM("Premium User"),

    ADMIN("Admin"),

    ARTIST("Artist");


    private final String roleCode;

    RoleType(String roleCode) {
        this.roleCode = roleCode;
    }

    public static String[] getRoleNames() {
        return new String[]{FREE.roleCode, ADMIN.roleCode, PREMIUM.roleCode, ARTIST.roleCode};
    }

    public static String fromDisplayName(String name) {
        for (RoleType role : RoleType.values()) {
            if (role.getRoleCode().equals(name)) {
                return role.name();
            }
        }
        throw new IllegalArgumentException("No enum constant with display name " + name);
    }

    @Override
    public String getValue() {
        return this.roleCode;
    }
}
