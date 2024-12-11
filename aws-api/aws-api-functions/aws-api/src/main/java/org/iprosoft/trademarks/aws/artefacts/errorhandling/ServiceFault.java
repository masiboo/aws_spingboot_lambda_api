package org.iprosoft.trademarks.aws.artefacts.errorhandling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFault {

	private int code;

	private HttpStatus status;

	private String message;

	private String target;

	private ZonedDateTime datetime;

}
