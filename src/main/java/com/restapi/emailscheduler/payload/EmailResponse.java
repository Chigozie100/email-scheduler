package com.restapi.emailscheduler.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailResponse {
    private Boolean success;
    private String message;
    private String jobId;
    private String jobGroup;

    public EmailResponse(Boolean success, String message){
        this.success= success;
        this.message = message;
    }

    public EmailResponse(Boolean success, String message, String jobId, String jobGroup) {
        this.success = success;
        this.message = message;
        this.jobId = jobId;
        this.jobGroup = jobGroup;
    }
}
