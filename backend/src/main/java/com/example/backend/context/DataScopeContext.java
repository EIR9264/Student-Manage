package com.example.backend.context;

import java.util.Set;

public class DataScopeContext {
    private static final ThreadLocal<DataScopeInfo> CONTEXT = new ThreadLocal<>();

    public static void set(DataScopeInfo info) {
        CONTEXT.set(info);
    }

    public static DataScopeInfo get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static class DataScopeInfo {
        private Long userId;
        private Long departmentId;
        private String dataScope;
        private Set<Long> customDeptIds;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        public String getDataScope() { return dataScope; }
        public void setDataScope(String dataScope) { this.dataScope = dataScope; }

        public Set<Long> getCustomDeptIds() { return customDeptIds; }
        public void setCustomDeptIds(Set<Long> customDeptIds) { this.customDeptIds = customDeptIds; }
    }
}
