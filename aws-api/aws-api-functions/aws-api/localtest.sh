#!/bin/bash

set -e

HANDLER=SQSDArtefactEventHandler

# Build and run the Docker container
mvn compile dependency:copy-dependencies -DincludeScope=runtime
docker build --platform linux/amd64 -t docker-Aws-api-v2:test .
docker run --platform linux/amd64\
            -e spring_cloud_function_definition=$HANDLER\
            -p 9000:8080 docker-Aws-api-v2:test &

# Wait for the container to start
sleep 20

# Function to make an asynchronous request
make_request() {
    curl --silent --request POST \
      --url http://localhost:9000/2015-03-31/functions/function/invocations \
      --data '{
        "version": "2.0",
        "routeKey": "$default",
        "rawPath": "/path/to/resource",
        "rawQueryString": "parameter1=value1&parameter1=value2&parameter2=value",
        "cookies": [
          "cookie1",
          "cookie2"
        ],
        "headers": {
          "Header1": "value1",
          "Header2": "value1,value2"
        },
        "queryStringParameters": {
          "mirisDocId": "12345678",
          "parameter2": "value"
        },
        "pathParameters": {
          "mirisDocId": "12345678"
        },
        "requestContext": {
          "accountId": "123456789012",
          "apiId": "api-id",
          "authentication": {
            "clientCert": {
              "clientCertPem": "CERT_CONTENT",
              "subjectDN": "www.example.com",
              "issuerDN": "Example issuer",
              "serialNumber": "a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1",
              "validity": {
                "notBefore": "May 28 12:30:02 2019 GMT",
                "notAfter": "Aug  5 09:36:04 2021 GMT"
              }
            }
          },
          "authorizer": {
            "jwt": {
              "claims": {
                "claim1": "value1",
                "claim2": "value2"
              },
              "scopes": [
                "scope1",
                "scope2"
              ]
            }
          },
          "domainName": "id.execute-api.us-east-1.amazonaws.com",
          "domainPrefix": "id",
          "http": {
            "method": "POST",
            "path": "/path/to/resource",
            "protocol": "HTTP/1.1",
            "sourceIp": "192.168.0.1/32",
            "userAgent": "agent"
          },
          "requestId": "id",
          "routeKey": "$default",
          "stage": "$default",
          "time": "12/Mar/2020:19:03:58 +0000",
          "timeEpoch": 1583348638390
        },
        "body": "eyJ0ZXN0IjoiYm9keSJ9",
        "isBase64Encoded": true,
        "stageVariables": {
          "stageVariable1": "value1",
          "stageVariable2": "value2"
        }
      }' > "response_$1.json" &
}

make_request_sqs() {
    curl --silent --request POST \
      --url http://localhost:9000/2015-03-31/functions/function/invocations \
      --data ' {
            "Records": [
            {
            "messageId": "28c04707-4c17-41c8-b5d5-cdd437fc9d47",
            "receiptHandle": "AQEBOi8hrFTlLP2X9ZIYNvwxQYtrvIvpZs4O24PU46xabat505cTKBgkRgoEI0VyprVOZNeLYZeXrttdWh4Iz1y+VwfFMPi4oFe6lwWNbrctsEqzYZzmmnWFjVf3CN23uqBtc0zCdOIGylFZqVczE7D1XmkbLIgBqrRecwsCwKPktRWGZ4H0xE+e5y8dWKpIMC7SN9xPAZcI9Ofe/5YS3ac9hl+kvfmM3lwEZb2F8hKCTFdV8VpxZqV6Yo/pmfmcllGTrAKVi3yErxhqc38BctGOvYhR5f1B9s0r9Wfn9d1rRrbSZTB07abJ3EfM3xsaKu9rQ/QF8Fjz+S2yMPGWy24x5PaoHGxoDs1pLoRiHOpaEj2DV6E11DmmJ4EU82FMCmWjTHvzGDuebvFyzCngNT5gNaYhzfYnbJTQ0RexJF0faPM=",
            "body": "{\"version\":\"0\",\"id\":\"06372da5-6088-7997-a468-4d9628bab6f8\",\"detail-type\":\"Object Created\",\"source\":\"aws.s3\",\"account\":\"551493771163\",\"time\":\"2024-10-15T10:00:06Z\",\"region\":\"eu-central-1\",\"resources\":[\"arn:aws:s3:::Aws-registry-bucket-dev-eu-central-1-551493771163\"],\"detail\":{\"version\":\"0\",\"bucket\":{\"name\":\"Aws-registry-bucket-dev-eu-central-1-551493771163\"},\"object\":{\"key\":\"Aws/2024-10-15/390e8ee9-2890-473f-a6a1-8ac58cafdf01/CA20240923.000/00310000.TIF\",\"size\":24376,\"etag\":\"b952fdba913fce13be84a2f17aa9c12c\",\"version-id\":\"DZobC7RRYWb3snOFeVltvs.ia7q_EbGm\",\"sequencer\":\"00670E3D2635C91E1A\"},\"request-id\":\"YH3MR7ZMZTRMC5J3\",\"requester\":\"551493771163\",\"source-ip-address\":\"3.126.58.85\",\"reason\":\"PutObject\"}}",
            "attributes": {
            "ApproximateReceiveCount": "1",
            "SentTimestamp": "1728986407161",
            "SenderId": "AIDAIP3MER2HFHNCCMVD4",
            "ApproximateFirstReceiveTimestamp": "1728986407225"
            },
            "messageAttributes": {},
            "md5OfBody": "75e2641f44f4f203a3681d308c456cf2",
            "eventSource": "aws:sqs",
            "eventSourceARN": "arn:aws:sqs:eu-central-1:551493771163:Aws-s3-object-created-queue",
            "awsRegion": "eu-central-1"
            }
            ]
            }
      ' > "response_$1.json" &
}
# Number of concurrent requests
NUM_REQUESTS=1

# Make multiple asynchronous requests
for i in $(seq 1 $NUM_REQUESTS); do
    make_request_sqs $i
done

# Wait for all requests to complete
wait

echo "All requests completed. Check response_*.json files for results."

# Stop the Docker container
docker stop $(docker ps -q --filter ancestor=docker-Aws-api-v2:test)