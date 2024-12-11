package artefact.aws.dbaccess;

import artefact.dto.ArtefactIndexDto;
import artefact.dto.ArtefactsDTO;
import artefact.dto.output.ArtefactInfo;
import artefact.entity.ArtefactTag;
import artefact.entity.IArtefact;
import artefact.mapper.ArtefactDBToArtefactMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import artefact.aws.dynamodb.*;
import artefact.dto.input.ArtefactInput;
import artefact.dto.input.ArtefactItemInput;
import artefact.dto.input.BatchInput;
import artefact.entity.*;
import artefact.usecase.*;
import artefact.util.ArtefactStatus;
import artefact.util.DateUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
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
public class  DBAccessServiceImpl implements ArtefactServiceInterface, DbKeys {

  private static final Logger logger = LoggerFactory.getLogger(DBAccessServiceImpl.class);

  /** {@link DateTimeFormatter}. */
  private DateTimeFormatter yyyymmddFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
  /** {@link SimpleDateFormat} YYYY-mm-dd format. */
  private SimpleDateFormat yyyymmddFormat;

  /** {@link SimpleDateFormat} in ISO Standard format. */
  private SimpleDateFormat df;

//  /** Documents Table Name. */
//  private String documentTableName;

//  /** {@link DynamoDbClient}. */
//  private final DynamoDbClient dynamoDB;

  private MirisDocIdValidatorService validatorService =  new MirisDocIdValidatorService();

  private DBAccessService dbAccessService = new DBAccessService();

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
   */
  public DBAccessServiceImpl() {
    this.yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd");
    this.df = new SimpleDateFormat(DATETIME_FORMAT);

    TimeZone tz = TimeZone.getTimeZone("UTC");
    this.yyyymmddFormat.setTimeZone(tz);
    this.df.setTimeZone(tz);
  }

  public void addTags(final String documentId,
                      final Collection<ArtefactTag> tags, final String timeToLive) {

      // not Implemented
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

      // return null
      return null;
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
        return null;
	}

	@Override
	public List<List<ArtefactTag>> getAllArtefactItemTags() {
		List<List<ArtefactTag>> listArtefactsTags = new ArrayList<>();
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
//        GetItemRequest r = GetItemRequest.builder().key(keysDocument(artefactId))
//                .tableName(this.documentTableName).build();
//
//        Map<String, AttributeValue> result = this.dynamoDB.getItem(r).item();
//
//        DDBHelper.updateTableItem(this.dynamoDB, this.documentTableName, keysDocument(artefactId),  "status", statusValue);

    }

    /**
     * @return
     */
    @Override
    public List<Artefact> getAllArtefacts(String date,String status) {
        Map<String, String> conditions = new HashMap<>();
        conditions.put("date", date);
        conditions.put("status", status);
        List<Artefact> artefacts = new ArrayList<>();

        return artefacts;
    }

    /**
     * @param artefactId
     * @return
     */
    @Override
    public Artefact getArtefactById(String artefactId) {

        try {
            ArtefactsDTO artefactDBAccess = dbAccessService.getArtefactById(artefactId);
            logger.info("getArtefactById artefactDBAccess {} by artefactId {}",artefactDBAccess, artefactId);
            if (artefactDBAccess != null) {
                return new ArtefactDBToArtefactMapper().apply(artefactDBAccess);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void saveDocumentDate(final IArtefact document) {
        // not implemented
    }

    /**
     * @param mirisDocId
     * @return
     */
    @Override
    public List<Artefact> getArtefactbyMirisDocId(String mirisDocId) {
        return getArtefactList(mirisDocId);

    }

    private List<Artefact> getArtefactList(String mirisDocId) {
        logger.info("get types artefact by class types");

        List<Artefact> artefacts = new ArrayList<>();
        try {
            DBAccessResponse response = dbAccessService.getArtefactsByMirisDocId(mirisDocId);
            List<ArtefactsDTO> results = response.getArtefactDBAccesses();

            for (ArtefactsDTO result : results) {
                logger.info(result.toString());
                Artefact artefact = new ArtefactDBToArtefactMapper().apply(result);

                assert artefact != null;
                // Filter for indexed only
                if (Objects.equals(artefact.getStatus(), ArtefactStatus.INDEXED.getStatus())
                        && !classType.PART.name().equalsIgnoreCase(artefact.getArtefactClassType())) {
                    artefacts.add(artefact);
                }
            }

            return artefacts;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Artefact> getArtefactListByType(String mirisDocId, List<String> derivedTypeList){
        return getArtefactList(mirisDocId);
    }

    @Override
    public List<Artefact> getArtefactbyMirisDocIdAndType(String mirisDocId, List<String> typeList) {
        List<String> derivedTypeList = typeList.stream().map(this::getTypeKey).collect(Collectors.toList());

        return getArtefactListByType(mirisDocId, derivedTypeList);
//        return DynamoDbPartiQ.getArtefactsByMirisDocidAndType(this.dynamoDB, this.documentTableName, mirisDocId, derivedTypeList);
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
        // not implemeted
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
        // not implemented
    }

    @Override
    public ArtefactInfo getArtefactInfoById(String artefactId) {
        // not implemented
        return null;
    }

    public boolean hasFileWithSameDocId(String mirisDocId, String artefactClassType) {
        List<String> onlyOneArtefactClassTypes = List.of(
                DocumentServiceImpl.classType.SOUND.name(),
                DocumentServiceImpl.classType.MULTIMEDIA.name(),
                DocumentServiceImpl.classType.BWLOGO.name(),
                DocumentServiceImpl.classType.COLOURLOGO.name()
        );

        if (onlyOneArtefactClassTypes.contains(artefactClassType.toUpperCase())) {
            List<Artefact> existingArtefacts = getArtefactbyMirisDocId(mirisDocId);

            if (DocumentServiceImpl.classType.BWLOGO.name().equalsIgnoreCase(artefactClassType) ||
                    DocumentServiceImpl.classType.COLOURLOGO.name().equalsIgnoreCase(artefactClassType)) {
                return replaceExistingLogo(existingArtefacts);
            } else if (DocumentServiceImpl.classType.MULTIMEDIA.name().equalsIgnoreCase(artefactClassType)) {
                return replaceExistingMultimedia(existingArtefacts);
            }
        }
        return false;
    }

    private boolean replaceExistingLogo(List<Artefact> existingArtefacts) {
        List<Artefact> logoArtefacts = existingArtefacts.stream()
                .filter(artefact -> artefact.getArtefactClassType().equals(DocumentServiceImpl.classType.BWLOGO.name())
                        || artefact.getArtefactClassType().equals(DocumentServiceImpl.classType.COLOURLOGO.name()))
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
                .filter(artefact -> artefact.getArtefactClassType().equals(DocumentServiceImpl.classType.MULTIMEDIA.name())
                        || artefact.getArtefactClassType().equals(DocumentServiceImpl.classType.SOUND.name()))
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
