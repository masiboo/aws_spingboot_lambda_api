package org.iprosoft.trademarks.aws.artefacts.service.ssm;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;

@Service
@AllArgsConstructor
@Slf4j
public class SSMParamStoreServiceImpl implements SSMParamStoreService {

	private SsmClient ssmClient;

	@Override
	public String getParamValue(String key) {
		String paramValue = null;
		try {
			GetParameterRequest getParamReq = GetParameterRequest.builder().name(key).build();
			GetParameterResponse getParamResp = ssmClient.getParameter(getParamReq);
			paramValue = getParamResp.parameter().value();
		}
		catch (SsmException e) {
			log.error("Unable to read the value from SSMParameter store", e);
		}
		return paramValue;
	}

}
