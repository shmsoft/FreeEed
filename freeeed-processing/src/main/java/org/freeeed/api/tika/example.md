# curl commands
* (Run from freeeed-processing directory)
* command
```shell
curl -T test-data/02-loose-files/docs/spreadsheet/tti.xls http://localhost:9998/meta
```
* response
```text
* custom:_AdHocReviewCycleID,-1468521477
custom:_AuthorEmail,m-ethridge@tamu.edu
extended-properties:Application,Microsoft Excel
meta:last-author,Stephanie Leary
custom:_AuthorEmailDisplayName,"Ethridge, Marie"
X-TIKA:Parsed-By-Full-Set,org.apache.tika.parser.DefaultParser,org.apache.tika.parser.microsoft.OfficeParser
meta:print-date,2005-02-09T15:43:04Z
dcterms:created,2001-01-10T17:18:11Z
language,en
dcterms:modified,2005-02-09T15:43:07Z
custom:_EmailSubject,Delegations of Authority
X-TIKA:Parsed-By,org.apache.tika.parser.DefaultParser,org.apache.tika.parser.microsoft.OfficeParser
dc:title,Director's Delegation for Contract Administration - Texas Transportation Institute
extended-properties:TotalTime,0
Content-Length,26112
Content-Type,application/vnd.ms-excel
```
* function
```java
getMetadata()
```

