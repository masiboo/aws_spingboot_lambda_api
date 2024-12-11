import { unmarshall, NativeAttributeValue } from '@aws-sdk/util-dynamodb';
import { AttributeValue, DynamoDBRecord } from 'aws-lambda';

export interface Artefact {
  s3Key:        string;
  GSI1PK:       string;
  insertedDate: string;
  GSI1SK:       string;
  artefactId:   string;
  SK:           string;
  mirisDocId:   string;
  PK:           string;
  type:         string;
  userId:       string;
  s3Bucket:     string;
  status:       string;
}

export interface ArtefactEvent {
  artefactId: string,
  artefactType: string,
  artefact: Artefact,
  eventType: string,
  eventId: string,
}

export async function handler(records: DynamoDBRecord[]) {
  const newArtefactRaw = (records[0]?.dynamodb?.NewImage as { [key: string]: NativeAttributeValue }) || {};
  const newArtefact = unmarshall(newArtefactRaw);

  const { GSI1PK , GSI1SK, PK, SK, artefactId, insertedDate, mirisDocId, s3Bucket, s3Key,
          status, type, userId}  = newArtefact;
  const artefact:Artefact = {
    GSI1PK,
    GSI1SK,
    PK,
    SK,
    artefactId,
    insertedDate,
    mirisDocId,
    s3Bucket,
    s3Key,
    status,
    type,
    userId,
  };

  const artefacts = [artefact];
  console.log("artefacts " + JSON.stringify(artefacts));

  if (artefact.SK == "job") {
    // return empty array for Pipes so it can continue the enrichment and not trigger downstream consumer
    return [];
  } else if (artefact.SK == "document") {
    // Each event will be the `detail` of the EventBridge event.
    const events = artefacts.map((artefact: Artefact) => {

      return {
        artefactId: artefact?.artefactId,
        artefact: artefact!,
        artefactType: artefact?.SK,
        eventType: records[0]?.eventName,
        eventId: records[0]?.eventID,
        status: artefact?.status
      };
    });
    return events;
  } // #todo p2: batch uplaods handler


}
