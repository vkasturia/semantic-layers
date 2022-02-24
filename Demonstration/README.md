# Demonstration - Semantic Layer for the New York Times

A simple demo of the query capabilities of Semantic Layers. The triples dataset for New York Times are loaded in a [Virtuoso Triple Store](https://virtuoso.openlinksw.com/). [SemLayerFrontend](https://github.com/vkasturia/semantic-layers/tree/master/Demonstration/SemLayerFrontend) contains the code for the Front-end. A REST endpoint has been built on the Backend code with [Spark Java Microframework](https://sparkjava.com/). The class [SemanticLayerSparkApplication](https://github.com/vkasturia/semantic-layers/blob/master/Demonstration/SemLayerFunctions/src/main/java/l3s/de/spark/SemanticLayerSparkApplication.java) serves as entry-point for the Back-end. 

## Environment Configuration for Front-end

1. Install `pip`
```
curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
python3 get-pip.py
```

2. Install and activate `virtualenv`
```
pip install virtualenv
virtualenv -p python3 venv
source venv/bin/activate
```

3. Install `requests, flask, unicodedata` 
```
pip install requests
pip install flask
pip install unicodedata
```

4. Run the Application
```
python3 app.py
```



 
