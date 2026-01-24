package com.example.springboot002.demos.web.Entity.WorkAnalysisEntity;

// 2. 通勤模式枚举
public enum CommuteMode {
    DRIVING("驾车"),
    TRANSIT("公交/地铁"),
    WALKING("步行"),
    CYCLING("骑行"),
    MIXED("组合方式");

    private final String description;

    CommuteMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}