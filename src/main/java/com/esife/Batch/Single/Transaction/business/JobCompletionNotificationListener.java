package com.esife.Batch.Single.Transaction.business;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;


@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    // private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);


    private static final Logger logger = LogManager.getLogger(JobCompletionNotificationListener.class);


    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("Before Job Execution");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            logger.info("Batch Job Completed Successfully");
        } else {
            logger.error("Batch Job Failed with Status: {}", jobExecution.getStatus());
        }
    }
}