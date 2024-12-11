#!/bin/bash

set -e

# Build and run the Docker container
mvn compile dependency:copy-dependencies -DincludeScope=runtime
docker build --platform linux/amd64 -t docker-Aws-api:test .
docker run --platform linux/amd64 -p 9000:8080 docker-Aws-api:test &

# Wait for the container to start
sleep 20

# Function to make an asynchronous request
make_request_search_mirisdoc() {
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
          "artefactId": "12345678"
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

make_request_artefactid() {
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
          "artefactId": "1"
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


# Number of concurrent requests
NUM_REQUESTS=1

# Make multiple asynchronous requests
for i in $(seq 1 $NUM_REQUESTS); do
    make_request_artefactid $i
done

# Wait for all requests to complete
wait

echo "All requests completed. Check response_*.json files for results."

# Stop the Docker container
docker stop $(docker ps -q --filter ancestor=docker-Aws-api-v2:test)