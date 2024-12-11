package org.iprosoft.trademarks.aws.artefacts.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class DebugInfoUtil {

	public static void logExceptionDetails(Exception e) {
		if (e == null) {
			log.error("Exception is null, cannot log exception details.");
			return;
		}
		String className = "UnknownClass";
		String methodName = "UnknownMethod";
		int lineNumber = -1;
		String exceptionMessage = Optional.ofNullable(e.getMessage()).orElse("No message available");

		StackTraceElement[] stackTraceElements = e.getStackTrace();
		if (stackTraceElements != null && stackTraceElements.length > 0) {
			StackTraceElement element = stackTraceElements[0];
			className = Optional.of(element.getClassName()).orElse(className);
			methodName = Optional.of(element.getMethodName()).orElse(methodName);
			lineNumber = element.getLineNumber();
		}

		// Construct error details string
		String errorDetails = "ClassName=" + className + " | MethodName=" + methodName + " | LineNumber=" + lineNumber
				+ " | ExceptionMessage=" + exceptionMessage;

		// Build the stack trace safely with null checks
		StringBuilder stackTrace = new StringBuilder();
		if (stackTraceElements != null) {
			for (StackTraceElement el : stackTraceElements) {
				if (el != null) {
					stackTrace.append(el.toString()).append("\n");
				}
				else {
					stackTrace.append("Null stack trace element\n");
				}
			}
		}
		else {
			stackTrace.append("No stack trace available");
		}
		log.error("Error Details: {}", errorDetails);
		log.error("Stack Trace: {}", stackTrace);
	}

}
