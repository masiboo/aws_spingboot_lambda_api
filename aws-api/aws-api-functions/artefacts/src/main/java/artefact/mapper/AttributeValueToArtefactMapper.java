package artefact.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.entity.Artefact;
import artefact.entity.ArtefactBuilder;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Function;

import static artefact.aws.dynamodb.DbKeys.TAG_DELIMINATOR;
import static artefact.util.AppConstants.DATETIME_FORMAT;

public class AttributeValueToArtefactMapper  implements Function<Map<String, AttributeValue>, Artefact> {

    private DateTimeFormatter yyyymmddFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    private static final Logger logger = LoggerFactory.getLogger(AttributeValueToArtefactMapper.class);

    /**
     * Applies this function to the given argument.
     *
     * @param valueMap the function argument
     * @return the function result
     */
    @Override
    public Artefact apply(Map<String, AttributeValue> valueMap) {

        logger.info("fileName {} artefact {} ",valueMap.get("fileName").s(), valueMap.get("artefactId").s()); // filename
        logger.info("ArtefactClassType {} present for the artefact {} ",valueMap.get("type").s(), valueMap.get("artefactId").s()); //type
        logger.info("status {} present for the artefact {} ",valueMap.get("status").s(), valueMap.get("artefactId").s()); //status

        String artefactId = valueMap.get("artefactId").s();
        Artefact artefact = new ArtefactBuilder()
                .setId(artefactId)
                .setArtefactClassType(classTypeDecode(valueMap.get("type").s()))
                .createArtefact();

        if (valueMap.containsKey("fileName")) {
            artefact.setArtefactName(valueMap.get("fileName").s());
        }

        if (valueMap.containsKey("s3Bucket")) {
            artefact.setS3Bucket(valueMap.get("s3Bucket").s());
        }

        if (valueMap.containsKey("s3Key")) {
            artefact.setS3Key(valueMap.get("s3Key").s());

        }

        if (valueMap.containsKey("status")) {
            artefact.setStatus (valueMap.get("status").s());
        }

        if (valueMap.containsKey("mirisDocId")) {
            artefact.setMirisDocId (valueMap.get("mirisDocId").s());
        }

        if (valueMap.containsKey("insertedDate")) {
            logger.info("insertedDate {} present for the artefact {} ",valueMap.get("insertedDate").s(), artefact.getId());
            String formattedDate = valueMap.get("insertedDate").s();
            artefact.setInsertedDate(formattedDate);

        }

        if (valueMap.containsKey("contentLength")) {
//            logger.info("contentLength {} present for the artefact {} ",valueMap.get("contentLength").s(), artefact.getId());
            if(valueMap.get("contentLength").n() != null) {
                artefact.setContentLength(valueMap.get("contentLength").n());
            } else {
                artefact.setContentLength("1L");
            }
        }

        if (valueMap.containsKey("sizeWarning")) {
            artefact.setSizeWarning(valueMap.get("sizeWarning").bool());
        }


        return artefact;
    }

    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies this function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before}
     * function and then applies this function
     * @throws NullPointerException if before is null
     * @see #andThen(Function)
     */
    @Override
    public <V> Function<V, Artefact> compose(Function<? super V, ? extends Map<String, AttributeValue>> before) {
        return Function.super.compose(before);
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     * @see #compose(Function)
     */
    @Override
    public <V> Function<Map<String, AttributeValue>, V> andThen(Function<? super Artefact, ? extends V> after) {
        return Function.super.andThen(after);
    }

    private String classTypeDecode(String valueTypeAtt) {
        return valueTypeAtt.substring(valueTypeAtt.lastIndexOf(TAG_DELIMINATOR) + 1);
    }
}
