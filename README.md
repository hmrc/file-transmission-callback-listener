# upscan-listener

### Overview

This is a helper service for testing and integration with the HMRC upload services [upscan-initiate](https://github.com/hmrc/upscan-initiate)
and [upscan-notify](https://github.com/hmrc/upscan-notify).

When POSTing a file to AWS for upload, a callback URL must be provided. When the file has been successfully virus scanned and is ready for download, upscan-notify will POST to this callback URL. If you do not have a URL to be called when you are developing, you can use the upscan-listener URL and check the Kibana logs to see the end-to-end flow of file upload.

By running this service locally, you can provide a callback URL for a locally running upscan-initiate, and see it pass through local upscan-notify to be written to your machine's standard out.

This service will also run in the pre-production environments (Development, QA, Staging). Services integrating with the upscan services can use the service's callback URL and check the Kibana logs to see end-to-end processing of
an uploaded file.

### Using the service
#### POST to the service
The service has a POST endpoint, which when hit will write to application logs, and will additionally calculate an MD5 hash of the downloadable file contents (this can be used to check that the file is as expected).

POST request:
URL: ```http://localhost:12345/upscan-listener/listen```

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
> File upload notification received on callback URL. File reference: my-reference, file download URL: http://my.download.url, file hash: e7e5955a9926ff43412fcb4ff4e65e68

#### GET list of recent callbacks
The service has a GET endpoint, which can be used to query the successful callbacks that the service has made since 00:00 on the day queried. These calls are stored in local memory, meaning that when the app is restarted, the log will be lost. It will ONLY log successful calls, not any instances where there has been an error with the callback.

GET request:
URL: ```http://localhost:12345/upscan-listener/poll```

Response body:
```
{
    "currentDate": "2018-03-19",
    "successfulResponses": [
        {
            "reference": "my-reference",
            "downloadUrl": "http://my.download.url",
            "hash": "e7e5955a9926ff43412fcb4ff4e65e68"
        },
    {
            "reference": "my-reference-2",
            "downloadUrl": "http://my.download.url-2",
            "hash": "e7e5955a9926ff43412fcb4ff4e65e68"
        }
    ],
    "quarantineResponses": [
        {
            "reference": "my-reference-4",
            "details": "This file had a nasty virus"
        },
        {
            "reference": "my-reference-4",
            "details": "And so did this one"
        }
    ]
}
```

### Running locally
Start your upscan-initiate service on port 12345 with the following command: ```sbt "run 12345"```

To use the service, use the following URL for callback in your service: http://localhost:12345/listen

### Using in the environments
You can use the upscan-listener URLs in the pre-production environments, and check in the Kibana logs to see that your file has been successfully uploaded to AWS, and the callback URL hit.

Use the following URLs for callback when sending details to upscan-initiate:

Development: https://upscan-listener.public.mdtp/upscan-listener/listen

QA: https://upscan-listener.public.mdtp/upscan-listener/listen

Staging: https://upscan-listener.public.mdtp/upscan-listener/listen

Links to the Kibana logs are available from the catalogue
