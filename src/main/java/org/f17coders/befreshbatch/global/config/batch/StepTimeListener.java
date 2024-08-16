package org.f17coders.befreshbatch.global.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;

@Slf4j
public class StepTimeListener extends StepExecutionListenerSupport {

    private long startTime;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        startTime = System.currentTimeMillis();  // 스텝 시작 시간 기록
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long endTime = System.currentTimeMillis();  // 스텝 종료 시간 기록
        long duration = endTime - startTime;
        log.info("Step [{}] took {} ms", stepExecution.getStepName(), duration);  // 실행 시간 로그 출력
        return null;
    }
}
