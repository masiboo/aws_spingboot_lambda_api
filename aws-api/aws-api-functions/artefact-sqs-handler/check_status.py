import boto3
import requests
import json
import os

baseUrl = os.environ['Aws_CORE_DB_ACCESS_API_URL']

def main_handler(event, context):
    # Return the handling result
    print('API event: ', event)

    for record in event['Records']:
        body = json.loads(record['body'])
        print(f"Received message: {json.dumps(body)}")

        url = f"{baseUrl}/db-init-access/api/v1/artefacts"
        artefact =  body["detail"]["artefact"]
        klassType = artefact["type"].split('#')
        print(f"class-type --------> {klassType[1]}")

        # missing info
        # user id
        # content-type
        # content-size
    

        payload = json.dumps({
            "status": artefact["status"],
            "artefactClass": klassType[1],
            "indexationDate": artefact["insertedDate"],
            "archiveDate": artefact['insertedDate'],
            "artefactUUID": artefact["artefactId"],
            "s3Bucket": artefact["s3Bucket"],
            "mirisDocId": artefact["mirisDocId"],
            "artefactItems": [
                {
                    "indexationDate": artefact["insertedDate"],
                    "contentType": "application/pdf",
                    "s3Key": artefact["s3Key"],
                    "totalPages": 1,
                },
            ],
        })

        print(f"payload ----> {payload}")
        headers = {
            'Content-Type': 'application/json'
        }

        response = requests.request("POST", url, headers=headers, data=payload)
        print(response.text)

    return []
