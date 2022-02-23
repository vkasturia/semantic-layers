The PDF file "InformationNeeds.pdf" contains the list of information needs.
Each information need corresponds to one free-text query and to one SPARQL query. 

The folders contain the results returned by the SPARQL queries and the search systems (Google and HistDiv).
In each folder there are 3 files, one for each system (SPARQL, Google, HistDiv). 
The files contain the following columns (tab seperated):
- URL: Link to the New York Times (NYT) article   
- TITLE: Title of the NYT article
- RELEVANT?: Whether the result is relevant or not
- EXPLANATION: Explanation for relevance or irrelevance of result
In case the system did not return any result the file contains only the following line:
# NO RESULTS RETURNED BY <SYSTEM_NAME> FOR THIS QUERY

The PDF file "SPARQL_ExecTimes.pdf" contains the executime time of the SPARQL queries
(we run the queries 10 times within 3 days).