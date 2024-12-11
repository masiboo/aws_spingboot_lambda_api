package org.wipo.trademarks.aws.Aws.entrypoints;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class StandardOutLambdaLogger implements LambdaLogger {
    @Override
    public void log(String s) {
        System.out.println(s);
    }

    @Override
    public void log(byte[] bytes) {
        System.out.println(bytes);
    }
}
