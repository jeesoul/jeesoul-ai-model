package com.jeesoul.ai.model.constant;

/**
 * AI对话角色枚举
 *
 * @author dxy
 * @date 2025-06-10
 */
public enum AiRole {
    /**
     * 系统角色
     */
    SYSTEM("system"),
    /**
     * 用户角色
     */
    USER("user"),
    /**
     * AI助手角色
     */
    ASSISTANT("assistant");
    /**
     * value
     */
    private final String value;

    /**
     * AiRole
     *
     * @param value value
     */
    AiRole(String value) {
        this.value = value;
    }

    /**
     * get
     *
     * @return String
     */
    public String getValue() {
        return value;
    }
}
