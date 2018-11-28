package uk.gov.hmcts.cmc.claimstore.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

@Aspect
@Configuration
public class LoggerHandler {
    private static final String LOG_MESSAGE_FORMAT = "%s.%s execution time : %d ms";
    private static final Logger logger = LoggerFactory.getLogger(LoggerHandler.class);

    @Pointcut("execution(@uk.gov.hmcts.cmc.claimstore.stereotypes.LogMe * *(..))")
    public void isAnnotated() {
    }

    @Around("isAnnotated()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object retVal = joinPoint.proceed();
        stopWatch.stop();
        logExecutionTime(joinPoint, stopWatch);
        return retVal;
    }

    private void logExecutionTime(ProceedingJoinPoint joinPoint, StopWatch stopWatch) {
        String logMessage = String.format(LOG_MESSAGE_FORMAT,
            joinPoint.getTarget().getClass().getName(),
            joinPoint.getSignature().getName(),
            stopWatch.getLastTaskTimeMillis());
        logger.info(logMessage);
    }

}
