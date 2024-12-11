import type { SQSEvent, Context } from 'aws-lambda'

exports.handler = async (event: SQSEvent, context: Context) => {

  console.log(context.functionName)
  console.log(`record-------------------------- -> ${event.Records}`)
  console.log(JSON.stringify(event))

  const axios = require('axios');
  let data = JSON.stringify({
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
  });

  let config = {
    method: 'post',
    maxBodyLength: Infinity,
    url: 'http://dbaccess-1486360352.eu-central-1.elb.amazonaws.com/db-init-access/api/v1/artefacts',
    headers: {
      'Content-Type': 'application/json'
    },
    data : data
  };

  axios.request(config)
    .then((response: any) => {
      console.log(JSON.stringify(response.data));
    })
    .catch((error: any) => {
      console.log(error);
    });


}
