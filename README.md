# Semantic Layers

What is a Semantic Layer?

A Semantic Layer is an RDF repository (RDF graph) of structured data about a collection of archived documents. Structured data includes not only metadata information about a document (like publication date), but also entity annotations, i.e., disambiguated entities mentioned in each document extracted using an [entity linking](https://en.wikipedia.org/wiki/Entity_linking) system.

The following figure shows an example of (a part of) a Semantic Layer describing metadata and annotation information for a news article (non-versioned). We see that the document was published on 6 January 2012 and mentions the entity name “Giuliani” at character position 512, which probably (with confidence score 0.9/1.0) corresponds to the known American lawyer and former politician Rudy Giuliani. By accessing [DBpedia](https://www.dbpedia.org/), we can now retrieve more information about this entity like its birth date, an image, a description in a specific language, etc.

The below figure shows an example of an archived web page containing versions. Now, each version has its own metadata, annotations and references to other web pages. We notice that the event name “Euro 2008” was identified in the first version of the archived document and was linked to the DBpedia resource corresponding to the soccer tournament UEFA Euro 2008. The archived document is also associated with metadata information related to its versions. We can see the date of its first capture, the date of its last capture and its total number of captures. In addition, by exploiting the same-as property of OWL Web ontology language, we can define that a specific version of a URL is the same as another version (e.g., versions 2 and 3 in our example). Thereby, we can avoid storing exactly the same data for two identical versions (redundancy is a common problem in web archives).

The RDF schema used for describing the data is called Open Web Archive and is depicted in the following figure:

The specification is available at: http://l3s.de/owa/

A semantic layer allows running advanced, entity-centric queries that can also directly integrate information from other knowledge bases. The below SPARQL query can be answered by a semantic layer over a collection of old news articles.

