package org.iprosoft.trademarks.aws.artefacts.errorhandling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestServiceFaultException {

	private ServiceFault serviceFault;

}
