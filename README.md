# file-transmission-callback-listener

### Overview

This is a helper service for testing and integration with the HMRC file transmission service [file-transmission](https://github.com/hmrc/file-transmission).

The file-transmission service uses asynchronous HTTP callbacks to notify it's clients (consuming services ) about the outcome of file transmission operations. 
In order to be able to perform acceptance tests of file-transmission we need to mock of the consuming service ability to consume callbacks.
By running this service locally, you can provide a callback URL for a locally running file-transmission.

This service will also run in the pre-production environments (Development, QA, Staging). 

### Using the service
#### POST to the service
The service has a POST endpoint, which when hit will write to application logs.

POST request:
URL: ```http://localhost:12345/file-transmission-callback-listener/listen```

Request body:
```
{
    "reference": "my-reference",
    "downloadUrl": "http://my.download.url"
}
```

Response body:
```
{
    "reference": "my-reference",
    "downloadUrl": "http://my.download.url"
    "hash": "e7e5955a9926ff43412fcb4ff4e65e68"
}
```

Once the file has been successfully transferred to the S3 bucket for download, a POST will be made by upscan-notify to upscan-listener, and this event will be written to the logs.

If the end-to-end process has worked as expected, you should see the following in the upscan-listener application logs:
> Callback has been received with file-reference: [file-reference]

#### GET list of recent callbacks
The service has a GET endpoint, which can be used to query the successful callbacks that the service has made since 00:00 on the day queried. These calls are stored in local memory, meaning that when the app is restarted, the log will be lost. It will ONLY log successful calls, not any instances where there has been an error with the callback.

GET request:
URL: ```http://localhost:12345/file-transmission-callback-listener/poll/{fileReference}```

Response body:
```
{
    "currentDate": "2018-03-19",
    "callbacks": [
         {
              "file-reference":"11370e18-6e24-453e-b45a-76d3e32ea33d",
              "batch-id":"32230e18-6e24-453e-b45a-76d3e32ea33d",
              "outcome":"SUCCESS"
         },
    {
          "fileReference":"11370e18-6e24-453e-b45a-76d3e32ea33d",
          "batchId":"32230e18-6e24-453e-b45a-76d3e32ea33d",
          "outcome":"FAILURE"
          "errorDetail":"text field from MDG"
       }
    ],
}
```

#### GET the list of callbacks for specific file id

GET request:
URL: ```http://localhost:12345/file-transmission-callback-listener/poll```

Response body:
```
{
    "currentDate": "2018-03-19",
    "callbacks": [
         {
              "file-reference":"11370e18-6e24-453e-b45a-76d3e32ea33d",
              "batch-id":"32230e18-6e24-453e-b45a-76d3e32ea33d",
              "outcome":"SUCCESS"
         },
    {
          "fileReference":"11370e18-6e24-453e-b45a-76d3e32ea33d",
          "batchId":"32230e18-6e24-453e-b45a-76d3e32ea33d",
          "outcome":"FAILURE"
          "errorDetail":"text field from MDG"
       }
    ],
}
```

### Running locally
Start your file-transmission-callback-listener service on port 12345 with the following command: ```sbt "run 12345"```

To use the service, use the following URL for callback in your service: http://localhost:12345/listen

### Using in the environments
You can use the file-transmission-callback-listener URLs in the pre-production environments, and check in the Kibana logs to see that your file has been successfully uploaded to AWS, and the callback URL hit.

Use the following URLs for callback when sending details to file-transmission:

Development: https://file-transmission-callback-listener.public.mdtp/file-transmission-callback-listener/listen

QA: https://file-transmission-callback-listener/.public.mdtp/file-transmission-callback-listener//listen

Staging: https:/file-transmission-callback-listener.public.mdtp/file-transmission-callback-listener/listen

Links to the Kibana logs are available from the [catalogue](https://catalogue.tax.service.gov.uk/service/file-transmission-callback-listener)
