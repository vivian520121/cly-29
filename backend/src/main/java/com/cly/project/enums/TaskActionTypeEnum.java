package com.cly.project.enums;

import lombok.Getter;

@Getter
public enum TaskActionTypeEnum {

    CREATE("CREATE", "创建任务"),
    UPDATE("UPDATE", "更新任务"),
    STATUS_CHANGE("STATUS_CHANGE", "状态变更"),
    ASSIGNEE_CHANGE("ASSIGNEE_CHANGE", "负责人变更"),
    PRIORITY_CHANGE("PRIORITY_CHANGE", "优先级变更"),
    PROGRESS_CHANGE("PROGRESS_CHANGE", "进度变更"),
    TAG_CHANGE("TAG_CHANGE", "标签变更"),
    FILE_UPLOAD("FILE_UPLOAD", "上传附件"),
    COMMENT("COMMENT", "添加评论"),
    SUBTASK_ADD("SUBTASK_ADD", "添加子任务"),
    SUBTASK_DELETE("SUBTASK_DELETE", "删除子任务"),
    WORKLOG_ADD("WORKLOG_ADD", "填报工时");

    private final String code;
    private final String desc;

    TaskActionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
