import { unmarshall, NativeAttributeValue } from '@aws-sdk/util-dynamodb';
import { AttributeValue, DynamoDBRecord } from 'aws-lambda';

export interface BatchItem {
  PK: string,
  SK: string,
  artefactId: string,
  batchStatus: string,
  batchSequence: string,
  requestId: string
}

export async function handler(records: DynamoDBRecord[]) {
  const rawItem = (records[0]?.dynamodb?.NewImage as { [key: string]: NativeAttributeValue }) || {};
  const oldrawItem = (records[0]?.dynamodb?.OldImage as { [key: string]: NativeAttributeValue }) || {};


  const unmarshalledItem = unmarshall(rawItem);
  const oldunmarshalledItem = unmarshall(oldrawItem);

  if (unmarshalledItem.SK === "job") {
    // return empty array for Pipes so it can continue the enrichment and not trigger downstream consumer
    return [];

  } else if (unmarshalledItem.SK === "document") {

    return [];
  } else if (unmarshalledItem.SK === "batch") {


    const { PK, SK, artefactId, batchStatus, batchSequence,
      requestId }  = unmarshalledItem;

    const batch: BatchItem = {
      PK, SK, artefactId, batchStatus, batchSequence,
      requestId
    }

    const batches = [batch];
    console.log("batches " + JSON.stringify(batches));

    // Each event will be the `detail` of the EventBridge event.
    const events = batches.map((batch: BatchItem) => {

      return {
        batchSequence: batch?.batchSequence,
        batch: batch!,
        type: batch?.SK,
        eventType: records[0]?.eventName,
        eventId: records[0]?.eventID,
        status: batch?.batchStatus
        // mirisDocID!!
      };
    });


    return events;


  }




}
