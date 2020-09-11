package uk.gov.hmcts.cmc.claimstore.tests.helpers;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


public class RetryFailedFunctionalTests implements TestRule {


        private AtomicInteger count;

        public RetryFailedFunctionalTests (int count){
            super();
            this.count = new AtomicInteger(count);
        }

        @Override
        public Statement apply(final Statement statement, final Description desc) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    while (count.getAndDecrement() > 0) {
                        try {
                            statement.evaluate();
                            return;
                        } catch (Throwable throwable) {
                            if (count.get() > 0 && desc.getAnnotation(Retry.class)!= null) {
                                System.out.println("!!!============RETRYING FAILED TEST====================!!!");
                                System.out.println(desc.getDisplayName() + "failed");
                                System.out.println(count.toString() + " retries are remaining");
                                System.out.println("!!!================================!!!");
                            } else {
                                throw throwable;
                            }
                        }
                    }
                }
            };
        }
}
