package artefact.entrypoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.apigateway.ApiMapResponse;
import artefact.apigateway.CoreRequestHandler;
import artefact.dto.output.BatchOutput;
import artefact.usecase.BatchServiceInterface;
import artefact.util.ApiGatewayResponseUtil;
import artefact.util.ConstantUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static artefact.apigateway.ApiResponseStatus.*;
import static artefact.util.AppConstants.KEY_MESSAGE;

public class ArtefactUnLockByBatchSeqHandler extends CoreRequestHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ArtefactUnLockByBatchSeqHandler.class);
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        Map<String,String> pathParamMap = Optional.ofNullable(event.getPathParameters()).orElse(Map.of());
        String batchSeq = pathParamMap.get("batchSeq");
        try{
            if (batchSeq == null) {
                logger.warn("Missing 'batchSeq' parameter in path");
                return buildResponse(logger, SC_BAD_REQUEST, new ApiMapResponse(Map.of(KEY_MESSAGE, "Missing 'batchSeq' parameter in path")));
            }

            BatchServiceInterface batchService = getAwsServices().batchService();
            BatchOutput batchDtl = batchService.getBatchDetail(batchSeq);

            if(batchDtl == null){
                String errorMessage = "The Batch is not found with batchSequence " + batchSeq;
                logger.error(errorMessage);
                return buildResponse(logger,SC_NOT_FOUND,new ApiMapResponse(Map.of(KEY_MESSAGE, errorMessage)));
            }

            if(!batchDtl.isLocked()){
                return buildResponse(logger,SC_BAD_REQUEST,new ApiMapResponse(Map.of(KEY_MESSAGE, "Batch is already in UNLOCKED")));
            }

            batchService.updateLockState(batchSeq,false);

            return buildResponse(logger,SC_OK,new ApiMapResponse(Map.of(KEY_MESSAGE,"Batch unlocked successfully")));
        } catch (IOException e) {
            String errorMessage = "Error updated the batch status ";
            logger.error(errorMessage,e);
            return ApiGatewayResponseUtil.getAPIGatewayV2HTTPResponse(ConstantUtil.STATUS_CODE_SERVER_ERROR,
                                                                        errorMessage+e.getMessage(), true);
        }
    }
}
