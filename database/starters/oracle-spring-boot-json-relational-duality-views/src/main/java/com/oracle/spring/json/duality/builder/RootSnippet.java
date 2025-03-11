package com.oracle.spring.json.duality.builder;

public enum RootSnippet {
    NONE(null),
    VALIDATE(null),
    CREATE("create force editionable json relational duality view"),
    CREATE_DROP(CREATE.snippet),
    UPDATE("create or replace force editionable json relational duality view");
    private final String snippet;

    RootSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getSnippet() {
        return snippet;
    }

    public static RootSnippet fromDdlAuto(String ddlAuto) {
        if (ddlAuto == null) {
            return NONE;
        }
        // none, validate, update, create, and create-drop
        return switch (ddlAuto) {
            case "none" -> NONE;
            case "validate" -> VALIDATE;
            case "create" -> CREATE;
            case "create_drop" -> CREATE_DROP;
            case "update" -> UPDATE;
            default -> throw new IllegalStateException("Unexpected value: " + ddlAuto);
        };
    }
}
