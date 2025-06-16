package com.oracle.spring.json.duality.builder;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.jpa.dv")
public class DualityViewConfigurationProperties {
    private Boolean showSql;
    private String ddlAuto;

    public Boolean isShowSql() {
        return showSql;
    }

    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }

    public String getDdlAuto() {
        return ddlAuto;
    }

    public void setDdlAuto(String ddlAuto) {
        this.ddlAuto = ddlAuto;
    }
}
