package artefact.aws.dynamodb;

import artefact.dto.ArtefactIndexDto;
import artefact.dto.input.ArtefactInput;
import artefact.dto.input.ArtefactItemInput;
import artefact.dto.input.BatchInput;
import artefact.dto.output.ArtefactInfo;
import artefact.mapper.AttributeValueToArtefactInfoMapper;
import artefact.mapper.AttributeValueToArtefactMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.entity.*;
import artefact.usecase.ArtefactServiceInterface;
import artefact.usecase.MirisDocIdValidatorService;
import artefact.util.ArtefactStatus;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.StringUtils;
import artefact.util.DateUtils;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static artefact.aws.dynamodb.DDBHelper.buildAttrValueUpdate;
import static artefact.aws.dynamodb.SiteIdKeyGenerator.createDatabaseKey;
import static artefact.util.AppConstants.*;

/** Implementation of the {@link ArtefactServiceInterface}. */
public class DocumentServiceImpl implements ArtefactServiceInterface, DbKeys {

  private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

  /** {@link DateTimeFormatter}. */
  private DateTimeFormatter yyyymmddFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
  /** {@link SimpleDateFormat} YYYY-mm-dd format. */
  private SimpleDateFormat yyyymmddFormat;

  /** {@link SimpleDateFormat} in ISO Standard format. */
  private SimpleDateFormat df;

  /** Documents Table Name. */
  private String documentTableName;

  /** {@link DynamoDbClient}. */
  private final DynamoDbClient dynamoDB;

  private MirisDocIdValidatorService validatorService =  new MirisDocIdValidatorService();

  private String[] mimeContentType = {
    "application/pdf",
    "image/gif",
    "image/png",
    "image/tiff",
    "image/x-tiff",
    "image/jpeg",
    "image/pjpeg",
    "image/bmp",
    "audio/mpeg",
    "audio/wav",
    "video/mp4",
    "application/mp4",
    "audio/mp4",
  };

  private enum contentType {
	  PDF, 
	  GIF, 
	  JPG, 
	  TIF
  }
  
  public enum classType {
    CERTIFICATE,
    DOCUMENT,
    BWLOGO,
    COLOURLOGO,
    SOUND,
    MULTIMEDIA,
    PART
  }
  
  /**
   * constructor.
   *
   * @param builder {@link DynamoDbConnectionBuilder}
   * @param documentsTable {@link String}
   */
  public DocumentServiceImpl(final DynamoDbConnectionBuilder builder, final String documentsTable) {
    if (documentsTable == null) {
      throw new IllegalArgumentException("Table name is null");
    }

    this.dynamoDB = builder.build();
    this.documentTableName = documentsTable;

    this.yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd");
    this.df = new SimpleDateFormat(DATETIME_FORMAT);

    TimeZone tz = TimeZone.getTimeZone("UTC");
    this.yyyymmddFormat.setTimeZone(tz);
    this.df.setTimeZone(tz);
  }

  @Override
  public void addTags(final String documentId,
      final Collection<ArtefactTag> tags, final String timeToLive) {

    if (tags != null) {
      Predicate<ArtefactTag> predicate = tag -> DocumentTagType.SYSTEMDEFINED.equals(tag.getType())
          || !SYSTEM_DEFINED_TAGS.contains(tag.getKey());

      DocumentTagToAttributeValueMap mapper =
          new DocumentTagToAttributeValueMap(this.df, PREFIX_DOCS, documentId);

      List<Map<String, AttributeValue>> valueList = tags.stream().filter(predicate).map(mapper)
              .flatMap(List::stream).collect(Collectors.toList());

      if (timeToLive != null) {
        valueList.forEach(v -> addN(v, "TimeToLive", timeToLive));
      }

      List<Put> putitems = valueList.stream()
          .map(values -> Put.builder().tableName(this.documentTableName).item(values).build())
          .collect(Collectors.toList());

      List<TransactWriteItem> writes = putitems.stream()
          .map(i -> TransactWriteItem.builder().put(i).build()).collect(Collectors.toList());

      if (!writes.isEmpty()) {
        this.dynamoDB
            .transactWriteItems(TransactWriteItemsRequest.builder().transactItems(writes).build());
      }
    }
  }

    private static void processResults(ExecuteStatementResponse executeStatementResult) {
//        System.out.println("ExecuteStatement successful: "+ executeStatementResult.toString());

        logger.error("#@@@@## Message - ExecuteStatement successful start");
        logger.error("ExecuteStatement successful: " + executeStatementResult.toString());
        logger.error("#@@@@## Message - ExecuteStatement successful message ends");

    }

  /**
   * Save Record to DB.
   *
   * @param values {@link Map} {@link AttributeValue}
   * @return {@link Map} {@link AttributeValue}
   */
  private Map<String, AttributeValue> save(final Map<String, AttributeValue> values) {

      PutItemRequest put =
              PutItemRequest.builder().tableName(this.documentTableName).item(values).build();
      Map<String, AttributeValue> output = this.dynamoDB.putItem(put).attributes();
      return output;
  }

    // TODO: persist mirisDocId
    public void saveDocument(final IArtefact document, final Collection<ArtefactTag> tags) {
        Map<String, AttributeValue> keys = keysDocument(document.getArtefactiTemId());

        saveDocumentWithTags(keys, document, tags, true, null);
    }

    /**
     * Save Document.
     *
     * @param keys       {@link Map}
     * @param document   {@link IArtefact}
     * @param tags       {@link Collection} {@link ArtefactTag}
     * @param saveGsi1   boolean
     * @param timeToLive {@link String}
     */
    private void saveDocumentWithTags(final Map<String, AttributeValue> keys, final IArtefact document,
                                      final Collection<ArtefactTag> tags, final boolean saveGsi1, final String timeToLive) {
// TODO save Document/Tags inside transaction.
        saveDocumentWithKeys(keys, document, saveGsi1, timeToLive);
//   addTags(document.getArtefactId(), tags, timeToLive);

        if (saveGsi1) {
            saveDocumentDate(document);
        }

    }

    /**
     * Save {@link ArtefactDynamoDb}.
     *
     * @param keys       {@link Map}
     * @param document   {@link IArtefact}
     * @param saveGsi1   boolean
     * @param timeToLive {@link String}
     */
    private void saveDocumentWithKeys(final Map<String, AttributeValue> keys,
                                      final IArtefact document, final boolean saveGsi1, final String timeToLive) {

        ZonedDateTime insertedDate = document.getInsertedDate();
        String shortDate = insertedDate != null ? DateUtils.getShortDateFromZonedDateTime(insertedDate) : DateUtils.getCurrentDateShortStr();
        String fullDate = insertedDate != null ? DateUtils.getFullDateFromZonedDateTime(insertedDate) : DateUtils.getCurrentUtcDateTimeStr();

        Map<String, AttributeValue> pkvalues = new HashMap<>(keys);

        if (saveGsi1) {
            addS(pkvalues, GSI1_PK, createDatabaseKey(PREFIX_DOCUMENT_DATE_TS + shortDate));
            addS(pkvalues, GSI1_SK, fullDate + TAG_DELIMINATOR + document.getArtefactiTemId());
        }

        addS(pkvalues, "artefactId", document.getArtefactiTemId());

        if (document.getFileName() != null){
            addS(pkvalues, "fileName", document.getFileName());
        }


        if (fullDate != null) {
            addS(pkvalues, "insertedDate", fullDate);
        }

        if (document.getUserId() != null){
            addS(pkvalues, "userId", document.getUserId());
        }

        if (document.getPath()!= null) {
            addS(pkvalues, "path", document.getPath());
        }

        if (document.getContentType()!= null) {
            addS(pkvalues, "contentType", document.getContentType());
        }

        if (document.getBatchSequenceId()!= null) {
            addS(pkvalues, "BatchSequenceId", "" + document.getBatchSequenceId());
        }

        if (document.getContainerId() != null) {
            addS(pkvalues, "containerId", document.getContainerId());  // artefact-id
        }

        if (document.getArtefactContainerName() != null) {
            addS(pkvalues, "artefactContainerName", document.getArtefactContainerName());
        }

        if (document.getContentLength() != null) {
              addN(pkvalues, "contentLength", "" + document.getContentLength());
        }

        if (document.getChecksum() != null) {
            String etag = document.getChecksum().replaceAll("^\"|\"$", "");
            addS(pkvalues, "etag", etag);
        }

        if (document.getBelongsToArtefactId() != null) {
            addS(pkvalues, "belongsToArtefactId", document.getBelongsToArtefactId());
        }

        if (document.getBucket() != null) {
            addS(pkvalues, "s3Bucket", document.getBucket());
        }

        if (document.getKey() != null) {
            addS(pkvalues, "s3Key", document.getKey());
        }

        if (timeToLive != null) {
            addN(pkvalues, "TimeToLive", timeToLive);
        }

        if (document.getMirisDocId() != null) {
            addS(pkvalues, "mirisDocId", document.getMirisDocId());
        }
        if (Boolean.TRUE.equals(document.getSizeWarning())) {
            pkvalues.put("sizeWarning", AttributeValue.builder().bool(document.getSizeWarning()).build());
        }

        if (document.getScannedType() != null){
            addEnum(pkvalues, "scanType", document.getScannedType().toString());
        }

        // TODO:  change type to composite of ArtefactClassType artefact#document, artefact#certificate, etc
        String valueType = "ARTEFACT" + TAG_DELIMINATOR + document.getArtefactClassType();
        if(document.isPart()){
            valueType = valueType + TAG_DELIMINATOR + "PART";
            addS(pkvalues,"pageNumber",document.getPageNumber());
            addS(pkvalues,"totalPages",document.getTotalPages());
        }

        if (document.getArtefactItemFileName() != null){
            addS(pkvalues, "artefaactItemFileName", document.getArtefactItemFileName());
        }

        if (document.getArtefactMergeId() != null){
            addS(pkvalues, "artefactMergeId", document.getArtefactMergeId());
        }

        String status = document.getStatus() == null ? ArtefactStatus.INIT.getStatus() : document.getStatus();

        addS(pkvalues, "type", valueType);
        addS(pkvalues, "status", status);

        // Persistence
        logger.info("18: pkvalues" + pkvalues);
        save(pkvalues);
    }


    @Override
    public Map<String, String> validateInputDocument(final ArtefactInput document) {

	Map <String, String> errorMessage = new HashMap<String, String>();
	contentType[] allContentTypes = contentType.values();
	classType[] allClassTypes = classType.values();

	boolean contentTypeB = false;
	boolean classTypeB = false;

	//String errorMessage = "";

    // single item construct for now
    for (ArtefactItemInput artefactItemInput : document.getItems()) {
        /*if (contentType.PDF.toString().equalsIgnoreCase(artefactItemInput.getContentType())) {
            contentTypeB = true;
        }*/
      for(String c: mimeContentType) {
        if(c.equalsIgnoreCase(artefactItemInput.getContentType())){
    			contentTypeB = true;    			
    		}
    	}
        if(artefactItemInput.getFilename()==null || artefactItemInput.getFilename().equals("")) {
            errorMessage.put("filename", "not valid");
        }
    }
    

		for(classType c: allClassTypes) {
			if(c.toString().equalsIgnoreCase(document.getArtefactClassType())){
				classTypeB = true;
			}
		}
		if(!contentTypeB) {
			errorMessage.put("contentType", "not valid");
		}
		if(!classTypeB) {
			errorMessage.put("classType", "not valid");
		}
		
		if(StringUtils.isBlank(document.getMirisDocId())) {
			errorMessage.put("mirisDocId", "not present");
		}

        if(StringUtils.isNotBlank(document.getMirisDocId())
                && Boolean.parseBoolean(System.getenv().get("MIRIS_CHECK_ENABLED"))
                && !isDocIdValid(document.getMirisDocId())){
            errorMessage.put("mirisDocId", "mirisDocId is Invalid");
        }
		return errorMessage;
	}

	@Override
	public Artefact getArtefactByTags(final Collection<ArtefactTag> tags) {
		// TODO Auto-generated method stub
		Map<String,AttributeValue> keys = new HashMap<>();
	    keys = tagsToKeysDocument(keys, tags);
			    
		GetItemRequest get = GetItemRequest.builder().tableName(documentTableName).key(keys).build();
		Map<String,AttributeValue> returnedItem = dynamoDB.getItem(get).item();
		
		Artefact artefact;
		ObjectMapper objectMapper = new ObjectMapper();
		try{
			
			artefact = objectMapper.readValue(returnedItem.toString(), Artefact.class);
		}catch(Exception e) {
			logger.info("Exception: "+e);
			artefact = null;
		}
		
		return artefact;
	}

	@Override
	public List<List<ArtefactTag>> getAllArtefactItemTags() {
		List<List<ArtefactTag>> listArtefactsTags = new ArrayList<>();
		/*
		String partitionAlias = "a";
		String partitionKeyName = "artefact";
		String partitionKeyVal = "all";
		HashMap<String,String> attrNameAlias = new HashMap<String,String>();
        attrNameAlias.put(partitionAlias, partitionKeyName);

        // Set up mapping of the partition name with the value.
        HashMap<String, AttributeValue> attrValues = new HashMap<>();

        attrValues.put(":"+partitionKeyName, AttributeValue.builder()
            .s(partitionKeyVal)
            .build());

        QueryRequest queryReq = QueryRequest.builder()
	            .tableName(documentTableName)
            .keyConditionExpression(partitionAlias + " = :" + partitionKeyName)
            .expressionAttributeNames(attrNameAlias)
            .expressionAttributeValues(attrValues)
            .build();
	    
		QueryResponse response = dynamoDB.query(queryReq);
		
		ObjectMapper objectMapper = new ObjectMapper();
		try{
			if(response.hasItems()) {
				int i = 0;
				do {
					Artefact a = objectMapper.readValue(response.items().get(i).toString(), Artefact.class);
					listArtefactsTags.add(a.getArtefactItemTags());
				}while(i<response.items().size());
			}
		}catch(Exception e) {
			logger.info("Exception: "+e);
			listArtefactsTags = null;
		}
		
		*/
		return listArtefactsTags;
	}
	
	private Map<String, AttributeValue> tagsToKeysDocument(final Map<String, AttributeValue> keys, Collection<ArtefactTag> tags){
		
		Iterator<ArtefactTag> i = tags.iterator();
		while(i.hasNext()) {
			ArtefactTag tag = (ArtefactTag) i.next();
			addS(keys, tag.getKey(), tag.getKey());
		}
				
		return keys;
	}	
	@Override
	public Map<String, String> validateInputMirisDocid(final ArtefactInput document) {

		Map <String, String> errorMessage = new HashMap<String, String>();		
		
		if(document.getMirisDocId()==null || document.getMirisDocId().equals("")) {
			errorMessage.put("mirisDocId", "not valid");
		}
		return errorMessage;
	}


	

    /**
     * @param artefactId
     * @param statusValue
     */
    @Override
    public void updateArtefactWithStatus(String artefactId, String statusValue) {
        GetItemRequest r = GetItemRequest.builder().key(keysDocument(artefactId))
                .tableName(this.documentTableName).build();

        Map<String, AttributeValue> result = this.dynamoDB.getItem(r).item();

        DDBHelper.updateTableItem(this.dynamoDB, this.documentTableName, keysDocument(artefactId),  "status", statusValue);

    }

    /**
     * @return
     */
    @Override
    public List<Artefact> getAllArtefacts(String date,String status) {
        Map<String, String> conditions = new HashMap<>();
        conditions.put("date", date);
        conditions.put("status", status);
        return DynamoDbPartiQ.getAllArtefacts(this.dynamoDB, this.documentTableName,conditions);
    }

    /**
     * @param artefactId
     * @return
     */
    @Override
    public Artefact getArtefactById(String artefactId) {
        GetItemRequest r = GetItemRequest.builder().key(keysDocument(artefactId))
                .tableName(this.documentTableName).build();

        Map<String, AttributeValue> result = this.dynamoDB.getItem(r).item();
        logger.info("getArtefactById result {} by artefactId {}",result, artefactId);
        if (!result.isEmpty()) {
            return new AttributeValueToArtefactMapper().apply(result);
        }
        return null;
    }

    private void saveDocumentDate(final IArtefact document) {
        ZonedDateTime insertedDate = document.getInsertedDate();
        String shortDate = DateUtils.getShortDateFromZonedDateTime(insertedDate);

        Map<String, AttributeValue> values =
                Map.of(PK, AttributeValue.builder().s(PREFIX_DOCUMENT_DATE).build(), SK,
                        AttributeValue.builder().s(shortDate).build());
        String conditionExpression = "attribute_not_exists(" + PK + ")";
        PutItemRequest put = PutItemRequest.builder().tableName(this.documentTableName)
                .conditionExpression(conditionExpression).item(values).build();

        try {
            this.dynamoDB.putItem(put).attributes();
        } catch (ConditionalCheckFailedException e) {
            // Conditional Check Fails on second insert attempt
        }
    }

    /**
     * @param mirisDocId
     * @return
     */
    @Override
    public List<Artefact> getArtefactbyMirisDocId(String mirisDocId) {
        // get types artefact by class types
        return DynamoDbPartiQ.getArtefactsByMirisDocid(this.dynamoDB, this.documentTableName, mirisDocId, getDefaultTypeKey());
    }

    @Override
    public List<Artefact> getArtefactbyMirisDocIdAndType(String mirisDocId, List<String> typeList) {
        List<String> derivedTypeList = typeList.stream().map(this::getTypeKey).collect(Collectors.toList());
        return DynamoDbPartiQ.getArtefactsByMirisDocidAndType(this.dynamoDB, this.documentTableName, mirisDocId, derivedTypeList);
    }

    /**
     * This method is used to update the artefact status as DELETED
     * Note :- This will just update the status not a HARD DELETE
     *
     * @param artefactId
     * @return
     */
    @Override
    public void softDeleteArtefactById(String artefactId) {
        // marking the artefact status as deleted (soft delete)
        updateArtefactWithStatus(artefactId, ArtefactStatus.DELETED.getStatus());
    }

    @Override
    public void indexArtefact(String artefactId, ArtefactIndexDto artefactIndexDto, ArtefactStatus artefactStatus) {

        Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
        updatedValues.put(KEY_STATUS,buildAttrValueUpdate(artefactStatus.getStatus(), AttributeAction.PUT));
        updatedValues.put(KEY_MIRIS_DOCID,buildAttrValueUpdate(artefactIndexDto.getMirisDocId(),AttributeAction.PUT));
        DDBHelper.updateAttributes(this.dynamoDB, this.documentTableName, keysDocument(artefactId),updatedValues);
    }

    @Override
    public void saveArtefactWithItemsAndTags(ArtefactInput artefactInput) {


    }

    @Override
    public void saveBatchUploads(BatchInput batchInput) {

    }

    @Override
    public boolean isValidClassType(String type) {
        return Arrays.stream(classType.values()).anyMatch(val -> val.toString().equalsIgnoreCase(type));
    }

    public String getAllClassTypes(){
        return Arrays.toString(classType.values());
    }

    @Override
    public void updateArtefact(String artefactId, Map<String, String> attributMap) {
        Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
        attributMap.forEach((attrKey,attrVal) -> updatedValues.put(attrKey,buildAttrValueUpdate(attrVal,AttributeAction.PUT)));
        DDBHelper.updateAttributes(this.dynamoDB, this.documentTableName, keysDocument(artefactId),updatedValues);
    }

    @Override
    public ArtefactInfo getArtefactInfoById(String artefactId) {
        GetItemRequest r = GetItemRequest.builder().key(keysDocument(artefactId))
                .tableName(this.documentTableName).build();

        Map<String, AttributeValue> result = this.dynamoDB.getItem(r).item();

        if (!result.isEmpty()) {
            return new AttributeValueToArtefactInfoMapper().apply(result);
        }
        return null;
    }

    @Override
    public boolean hasFileWithSameDocId(String mirisDocId, String artefactClassType) {
        List<String> onlyOneArtefactClassTypes = List.of(
                classType.SOUND.name(),
                classType.MULTIMEDIA.name(),
                classType.BWLOGO.name(),
                classType.COLOURLOGO.name()
        );

        if (onlyOneArtefactClassTypes.contains(artefactClassType.toUpperCase())) {
            List<Artefact> existingArtefacts = getArtefactbyMirisDocId(mirisDocId);

            if (classType.BWLOGO.name().equalsIgnoreCase(artefactClassType) ||
                    classType.COLOURLOGO.name().equalsIgnoreCase(artefactClassType)) {
                return replaceExistingLogo(existingArtefacts);
            } else if (classType.MULTIMEDIA.name().equalsIgnoreCase(artefactClassType)) {
                return replaceExistingMultimedia(existingArtefacts);
            }
        }
        return false;
    }

    private boolean replaceExistingLogo(List<Artefact> existingArtefacts) {
        List<Artefact> logoArtefacts = existingArtefacts.stream()
                .filter(artefact -> artefact.getArtefactClassType().equals(classType.BWLOGO.name())
                        || artefact.getArtefactClassType().equals(classType.COLOURLOGO.name()))
                .toList();
        if (!logoArtefacts.isEmpty()) {
            List<String> existingLogoArtefactIds = logoArtefacts.stream()
                    .map(Artefact::getId)
                    .toList();
            existingLogoArtefactIds.forEach(existingArtefactId ->
                    updateArtefactWithStatus(existingArtefactId, ArtefactStatus.DELETED.toString()));
            return true;
        }
        return false;
    }

    private boolean replaceExistingMultimedia(List<Artefact> existingArtefacts) {
        List<Artefact> multimediaArtefacts = existingArtefacts.stream()
                .filter(artefact -> artefact.getArtefactClassType().equals(classType.MULTIMEDIA.name())
                        || artefact.getArtefactClassType().equals(classType.SOUND.name()))
                .toList();

        if (!multimediaArtefacts.isEmpty()) {
            List<String> existingMultimediaArtefactIds = multimediaArtefacts.stream()
                    .map(Artefact::getId)
                    .toList();

            existingMultimediaArtefactIds.forEach(existingArtefactId ->
                    updateArtefactWithStatus(existingArtefactId, ArtefactStatus.DELETED.toString()));
            return true;
        }
        return false;
    }

    @Override
    public boolean isDocIdValid(String mirisDocId) {
        boolean isDocIdValid = false;
        String mirisCheckApiUrl = System.getenv("Aws_CORE_MIRIS_CHECK_API_URL");
        if (StringUtils.isNotBlank(mirisDocId)
                && mirisCheckApiUrl != null) {
            isDocIdValid = validatorService.isValid(mirisDocId);
        }
        return isDocIdValid;
    }


}
