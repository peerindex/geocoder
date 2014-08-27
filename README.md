geocoder
==============

A quick and dirty library that extracts location from short free text. NO dependency to any geocoding services. 100% open source including Gazetteer data. No network call or disk IO. All necessary data is contained within the jar and will be stored in-memory (ca. 25MB). 

How to use
----
Code:
```java
// Geocoder object is expensive to create & thread-safe.
// Share a single instance per application!
Geocoder geocoder = new Geocoder();
Location londonOH = geocoder.resolve("Rancho Cordova, US");
Location moscow   = geocoder.resolve("Москва является удивительным");
```
Output:
```json
(Rancho Cordova, California, US)
{
  "geonameId" : 5385941,
  "featureCodeCategory" : "SUBADM",
  "defaultName" : "Rancho Cordova",
  "featureCode" : "PPL",
  "codes" : {
    "ADM2" : "067",
    "ADM1" : "CA",
    "PCL" : "US"
  },
  "names" : [ "Rancho Kordova", "Ранчо Кордова", ...],
  "population" : 64776,
  "lat" : 38.58907,
  "lng" : -121.30273
}

(Moscow, RU)
{
  "geonameId" : 524901,
  "featureCodeCategory" : "SUBADM",
  "defaultName" : "Moscow",
  "featureCode" : "PPLC",
  "codes" : {
    "ADM1" : "48",
    "ADM2" : "562331",
    "PCL" : "RU"
  },
  "names" : [ "mwskw", "Mosco"...],
  "population" : 10381222,
  "lat" : 55.75222,
  "lng" : 37.61556
}
```

Notes
----
####Input
 - Primarily meant for location entered as free text. It doesn't work well with longer texts (like articles).
 - Only works to town level (no support for street address)
 - Language agnostic (but your mileage may vary for non-English texts)
 - Best used for things on the web, like Twitter (it uses population data adjusted for online activity)

####Output
 - Here is an example output with comment
```json
{
  // The Geoname ID of this location (see http://www.geonames.org/)
  "geonameId" : 5385941,

  // The type of this location. Roughly matches Geonames' classification
  "featureCodeCategory" : "SUBADM",
  
  // The default, English name of this location
  "defaultName" : "Rancho Cordova",
  
  // The Geoname feature code of this location
  "featureCode" : "PPL",
  
  // Geoname aministration area codes
  "codes" : {
    // This stands for Sacramento county
    "ADM2" : "067",
    // This stands for the state of California
    "ADM1" : "CA",
    // This stands for USA
    "PCL" : "US"
  },
  
  // Alternative names this location is known by
  "names" : [ "Rancho Kordova", "Ранчо Кордова", ...],
  
  // Population of this location
  "population" : 64776,
  
  // Latitude & Longitude
  "lat" : 38.58907,
  "lng" : -121.30273
}
```

####Performance & accuracy
 - Performance (on my 4-core Macbook pro)
   - Avg. response time: 0.01 ms
   - Throughput: 300K calls / sec
 - Accuracy
   - Both precision and recall were > 0.95 but the test was done with a VERY limited dataset
   - It will heavily depend on your data  

####Acknowledgment  
This library uses Gazetteer by GeoNames (http://www.geonames.org/) licensed under a [Creative Commons Attribution 3.0 License][3]

[3]: http://creativecommons.org/licenses/by/3.0/
