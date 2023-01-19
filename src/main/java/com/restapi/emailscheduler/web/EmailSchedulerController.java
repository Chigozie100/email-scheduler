package com.restapi.emailscheduler.web;

import com.restapi.emailscheduler.payload.EmailRequest;
import com.restapi.emailscheduler.payload.EmailResponse;
import com.restapi.emailscheduler.quartz.job.EmailJob;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
public class EmailSchedulerController {
    private final Scheduler scheduler;

    @PostMapping("/schedule/email")
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest){
        try{
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.getDateTime(), emailRequest.getTimeZone());
            if(dateTime.isBefore(ZonedDateTime.now())){
                EmailResponse emailResponse = new EmailResponse(false, "dateTime must be after current time");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(emailResponse);
            }
            JobDetail jobDetail = buildJobDetail(emailRequest);
            Trigger trigger = buildTrigger(jobDetail, dateTime);

            scheduler.scheduleJob(jobDetail, trigger);

            EmailResponse emailResponse = new EmailResponse(true,
                    jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email Scheduled Successfully");
            return ResponseEntity.ok(emailResponse);
        } catch (SchedulerException se){
            log.error("error while scheduling email:", se);
            EmailResponse emailResponse = new EmailResponse(false,
                    "Error while scheduling email, Please try again later");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(emailResponse);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<String> getApiTest(){
        return ResponseEntity.ok("Get API Test - Pass");
    }



    private JobDetail buildJobDetail(EmailRequest scheduleEmailRequest){
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email", scheduleEmailRequest.getEmail());
        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
        jobDataMap.put("body", scheduleEmailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("send email job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email triggers")
                .withDescription("send email Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
