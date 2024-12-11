package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.ApiMapResponse;
import artefact.apigateway.CoreRequestHandler;
import artefact.usecase.ArtefactJobServiceInterface;
import artefact.usecase.BatchServiceInterface;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ArtefactStatus;
import artefact.util.ConstantUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static artefact.apigateway.ApiResponseStatus.*;

public class ArtefactJobStatusReportHandler extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ArtefactJobStatusReportHandler.class);
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        logger.info("Event json Dump: " + gson.toJson(event));
        Map<String,Object> jobStatusResponseMap;
        try {
            String requestId = event.getPathParameters().get("requestId");
            if (requestId == null) {
                logger.warn("Missing 'requestId' parameter in path");
                return buildResponse(logger, SC_BAD_REQUEST, new ApiMapResponse(Map.of("message", "Missing 'requestId' parameter in path")));
            }

            try {
                ArtefactJobServiceInterface jobService = getAwsServices().jobService();
                jobStatusResponseMap = jobService.getAllJobStatusByRequestId(requestId);
                if (jobStatusResponseMap == null || jobStatusResponseMap.isEmpty()) {
                    logger.error("Job not found ");
                    return buildResponse(logger, SC_NOT_FOUND, new ApiMapResponse(Map.of("message", "Job Not found for the requestId " + requestId)));
                }
                // batchStatus will become INSERTED once all the jobs are UPLOADED : QuickFix MPD-419 , MPD-661
                String derivedBatchStatus = jobStatusResponseMap.get("batchStatus").toString();
                String batchSequence = jobStatusResponseMap.get("batchSequence").toString();
                if(ArtefactStatus.UPLOADED.getStatus().equalsIgnoreCase(derivedBatchStatus)){
                    BatchServiceInterface batchService = getAwsServices().batchService();
                    String statusByRequestType = batchService.findStatusByRequestType(batchSequence);
                    batchService.updateStatus(batchSequence, statusByRequestType);
                    jobStatusResponseMap.put("batchStatus",statusByRequestType);
                    filterResponseByBatchSequence(jobStatusResponseMap);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return buildResponse(logger, SC_ERROR, new ApiMapResponse(Map.of("message", "Failed to get job status report")));
            }

            return buildResponse(logger, SC_OK, new ApiMapResponse(jobStatusResponseMap));

        } catch (Exception e) {
            logger.error(e.getMessage());
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
                                                    "Failed to get job status report "+e.getMessage(), true);
        }
    }

    @SuppressWarnings("unchecked")
    private void filterResponseByBatchSequence(Map<String, Object> responseMap) {
        String batchSequence = (String) responseMap.get("batchSequence");
        String[] token = batchSequence.split("\\.");
        String afterDot;
        if(token.length >= 1){
            afterDot = token[token.length-1];
        } else {
            afterDot = null;
        }
        List<Map<String, Object>> jobs = (List<Map<String, Object>>) responseMap.get("jobs");

        List<Map<String, Object>> filteredJobs = jobs.stream()
                .filter(job -> ((String) job.get("artefactId")).contains(Objects.requireNonNull(afterDot)))
                .collect(Collectors.toList());
        filteredJobs.forEach(job -> {
            logger.info("filteredJobs Job: {}", job.toString());
        });
        responseMap.put("jobs", filteredJobs);
    }
}
