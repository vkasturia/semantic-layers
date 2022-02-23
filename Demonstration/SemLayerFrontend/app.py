# Copyright 2017-2022 Vaibhav Kasturia <vbh18kas@gmail.com>
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
# and associated documentation files (the "Software"), to deal in the Software without restriction, 
# including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
# and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
# subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial 
# portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
# LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
# IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
# WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
# OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


import requests
import unicodedata
import math
from urllib.parse import unquote
from flask import Flask, jsonify, render_template, request

app = Flask(__name__)


@app.route('/')
def home():
    data = {}
    return render_template('index.html', flag='home', data=data)


@app.route('/query', methods=['GET'])
def query():
    #Example Query: http://localhost:8083/query?queryType=SINGLE&text=India&algorithm=STATISTICAL&decayFactor=1.0&fromDate=1987-01-01&toDate=1987-01-02&timePeriod=DAY
    if request.method == 'GET':
        algorithm = request.args.get('algorithm')
        queryType = request.args.get('queryType')
        fromDate = request.args.get('fromDate')
        toDate = request.args.get('toDate')
        timePeriod = request.args.get('timePeriod')
        decayFactor = request.args.get('decayFactor')
        text = request.args.get('text')

    payload = f"text={text}&algorithm={algorithm}&queryType={queryType}&fromDate={fromDate}&toDate={toDate}&timePeriod={timePeriod}&decayFactor={decayFactor}"
    query = f"{base_url}?{payload}"
    data = query_api(query)
    return render_template('index.html', data=data, flag='query')


def query_api(query):
    #data = {"articles":[{"date":"1989-02-13","score":0.007197525152708396,"title":"Lasting Faith Of Soviet Jews Moves Wiesel","articleUrl":"http://query.nytimes.com/gst/fullpage.html?res=950DE0D91139F930A25751C0A96F948260"},{"date":"1989-02-11","score":0.0057523787040139355,"title":"Review/Theater; Comic Doings in a Diner And Male Pillow Talk","articleUrl":"http://query.nytimes.com/gst/fullpage.html?res=950DE2D9123FF932A25751C0A96F948260"},{"date":"1989-02-11","score":0.004292227235893365,"title":"New York Date for Soviet Troupe","articleUrl":"http://query.nytimes.com/gst/fullpage.html?res=950DE7DD133FF932A25751C0A96F948260"},{"date":"1989-02-14","score":0.003975141254224989,"title":"Boston&apos;s Jewish Population Surges as Soviets Ease Emigration","articleUrl":"http://query.nytimes.com/gst/fullpage.html?res=950DE5DC1F3CF937A25751C0A96F948260"},{"date":"1989-02-09","score":0.003069442083967065,"title":"Another Soviet Taboo Is Broken: Journal Attacks Communist Party","articleUrl":"http://query.nytimes.com/gst/fullpage.html?res=950DE2D61E39F93AA35751C0A96F948260"},{"date":"1989-02-07","score":0.003057481049211156,"title":"Prisoner At the Window","articleUrl":"http://query.nytimes.com/gst/fullpage.html?res=950DE1DC103AF934A35751C0A96F948260"},{"date":"1989-02-09","score":0.0025746599051837877,"title":"Talking Deals; Joint Soviet Deal, But Based in U.S.","articleUrl":"http://query.nytimes.com/gst/fullpage.html?res=950DE0DA1130F93AA35751C0A96F948260"},{"date":"1989-02-09","score":0.002076494905797047,"title":"Soviet Composers Union Readmits Rostropovich","articleUrl":"http://query.nytimes.com/gst/fullpage.html?res=950DE7DA1230F93AA35751C0A96F948260"},{"date":"1989-02-09","score":0.0012976362152501036,"title":"Soviet Life Lives","articleUrl":"http://query.nytimes.com/gst/fullpage.html?res=950DEED81438F93AA35751C0A96F948260"}]}
    data = requests.get(query).json()
    totalScore = math.pow(1, -9)
    
    for article in data["articles"]:
        totalScore += article["score"]
        print("total Score " + str(totalScore))

    for x in range(len(data["articles"])):
        normalizedScore = (data["articles"][x]["score"] / totalScore) * 100
        normalizedScore = round(normalizedScore, 2)
        if normalizedScore == 0.00:
            normalizedScore = 0.01
        data["articles"][x]["score"] = normalizedScore

    print(data)
    return data


if __name__ == '__main__':
    base_url = "http://localhost:4567/query"
    app.run(host="127.0.0.1", port=8083, debug=True)
