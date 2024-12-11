import boto3
import requests
import json


def on_event(event, context):
    print('The job is submitted successfully!')
    # Return the handling result
    print('API event: ', event)

    url = "http://dbaccess-1486360352.eu-central-1.elb.amazonaws.com/db-init-access/api/v1/artefacts"

    payload = json.dumps({
        "status": "INSERTED",
        "artefactClass": "CERTIFICATE",
        "indexationDate": "2023-04-04",
        "archiveDate": "2023-04-04",
        "s3Bucket": "documents/",
        "mirisDocId": "132354201",
        "artefactItems": [
            {
                "indexationDate": "2023-04-04",
                "contentType": "application/pdf",
                "s3Key": "documents.pdf",
                "totalPages": 2,
                "artefactItemNotes": [
                    {
                        "content": "legal",
                        "createdDate": "2023-04-04",
                        "author": "logothetis",
                        "modifiedDate": "2023-04-04"
                    },
                    {
                        "content": "legal2",
                        "createdDate": "2023-04-05",
                        "author": "logothetis",
                        "modifiedDate": "2023-04-05"
                    }
                ]
            },
            {
                "indexationDate": "2023-04-05",
                "contentType": "application/doc",
                "s3Key": "document.doc",
                "totalPages": 3,
                "artefactItemNotes": [
                    {
                        "content": "chancellery",
                        "createdDate": "2023-04-05",
                        "author": "don",
                        "modifiedDate": "2023-04-05"
                    }
                ]
            }
        ],
        "artefactItemTags": [
            {
                "key": "testing",
                "value": "tag-insertion",
                "insertedDate": "2023-04-06"
            },
            {
                "key": "testing-example",
                "value": "tag-insertion-example",
                "insertedDate": "2023-04-06"
            }
        ]
    })
    headers = {
        'Content-Type': 'application/json'
    }

    response = requests.request("POST", url, headers=headers, data=payload)
    print(response.text)

    return []
