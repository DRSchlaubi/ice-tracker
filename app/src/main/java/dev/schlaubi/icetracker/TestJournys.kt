package dev.schlaubi.icetracker

import dev.schlaubi.icetracker.fetcher.Journey
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language

@Language("JSON")
val json = """
    {
      "name": "123",
      "number": "123",
      "id": "randomid-1-2-2-2-2-5-8",
      "createdAt": "2022-11-23T20:47:35Z",
      "trainInfo": {
        "type": "ICE",
        "buildingSeries": "412",
        "tzn": "9050"
      },
      "stations": [
        {
          "evaNr": "8000261",
          "name": "München Hbf Gl. 27-16",
          "geocoordinates": {
            "latitude": 48.1414646698815,
            "longitude": 11.555653810501099
          }
        },
        {
          "evaNr": "8004129",
          "name": "München-Hackerbrücke",
          "geocoordinates": {
            "latitude": 48.14193706130171,
            "longitude": 11.548529863357546
          }
        },
        {
          "evaNr": "8004128",
          "name": "München-Donnersbergerbrücke",
          "geocoordinates": {
            "latitude": 48.142681426838884,
            "longitude": 11.536545753479004
          }
        }
      ],
      "tracks": [
        {
          "start": "8000261",
          "end": "8004129",
          "segments": [
            {
              "points": [
                {
                  "latitude": 48.1414646698815,
                  "longitude": 11.555653810501099,
                  "timestamp": "2022-11-22T16:35:00.908210Z",
                  "speed": 95,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14154849862464,
                  "longitude": 11.554389627347948,
                  "timestamp": "2022-11-22T16:35:03.075715400Z",
                  "speed": 81,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14154849862464,
                  "longitude": 11.554389627347948,
                  "timestamp": "2022-11-22T16:35:05.100753700Z",
                  "speed": 151,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14163232723091,
                  "longitude": 11.553125444194798,
                  "timestamp": "2022-11-22T16:35:07.121064200Z",
                  "speed": 74,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14163232723091,
                  "longitude": 11.553125444194798,
                  "timestamp": "2022-11-22T16:35:09.130020200Z",
                  "speed": 101,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.141716155700266,
                  "longitude": 11.551861261041648,
                  "timestamp": "2022-11-22T16:35:11.140570400Z",
                  "speed": 160,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.141716155700266,
                  "longitude": 11.551861261041648,
                  "timestamp": "2022-11-22T16:35:13.153841900Z",
                  "speed": 167,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14179998403275,
                  "longitude": 11.550597077888495,
                  "timestamp": "2022-11-22T16:35:15.163903200Z",
                  "speed": 79,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14179998403275,
                  "longitude": 11.550597077888495,
                  "timestamp": "2022-11-22T16:35:17.182481800Z",
                  "speed": 70,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14188381222832,
                  "longitude": 11.549332894735345,
                  "timestamp": "2022-11-22T16:35:19.202593700Z",
                  "speed": 82,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14188381222832,
                  "longitude": 11.549332894735345,
                  "timestamp": "2022-11-22T16:35:21.213290100Z",
                  "speed": 197,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14193706130171,
                  "longitude": 11.548529863357546,
                  "timestamp": "2022-11-22T16:35:23.225945500Z",
                  "speed": 152,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14193706130171,
                  "longitude": 11.548529863357546,
                  "timestamp": "2022-11-22T16:35:25.240017500Z",
                  "speed": 167,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                }
              ]
            }
          ]
        },
        {
          "start": "8004129",
          "end": "8004128",
          "segments": [
            {
              "points": [
                {
                  "latitude": 48.14196569094509,
                  "longitude": 11.548465490341188,
                  "timestamp": "2022-11-22T16:35:27.254079400Z",
                  "speed": 0,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14196569094509,
                  "longitude": 11.548465490341188,
                  "timestamp": "2022-11-22T16:35:29.265041500Z",
                  "speed": 81,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14204166780604,
                  "longitude": 11.547200193246459,
                  "timestamp": "2022-11-22T16:35:31.274218800Z",
                  "speed": 110,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14204166780604,
                  "longitude": 11.547200193246459,
                  "timestamp": "2022-11-22T16:35:33.290176Z",
                  "speed": 2,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14211764455458,
                  "longitude": 11.545934896151723,
                  "timestamp": "2022-11-22T16:35:35.296787800Z",
                  "speed": 112,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14211764455458,
                  "longitude": 11.545934896151723,
                  "timestamp": "2022-11-22T16:35:37.303528700Z",
                  "speed": 13,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14219362119063,
                  "longitude": 11.54466959905699,
                  "timestamp": "2022-11-22T16:35:39.314700600Z",
                  "speed": 139,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14219362119063,
                  "longitude": 11.54466959905699,
                  "timestamp": "2022-11-22T16:35:41.327670400Z",
                  "speed": 146,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14226959771426,
                  "longitude": 11.543404301962257,
                  "timestamp": "2022-11-22T16:35:43.338126300Z",
                  "speed": 55,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14226959771426,
                  "longitude": 11.543404301962257,
                  "timestamp": "2022-11-22T16:35:45.350408500Z",
                  "speed": 79,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14234557412543,
                  "longitude": 11.542139004867524,
                  "timestamp": "2022-11-22T16:35:47.366358300Z",
                  "speed": 102,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14234557412543,
                  "longitude": 11.542139004867524,
                  "timestamp": "2022-11-22T16:35:49.388567900Z",
                  "speed": 34,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14242155042416,
                  "longitude": 11.54087370777279,
                  "timestamp": "2022-11-22T16:35:51.411644300Z",
                  "speed": 206,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14242155042416,
                  "longitude": 11.54087370777279,
                  "timestamp": "2022-11-22T16:35:53.421962800Z",
                  "speed": 91,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14249752661043,
                  "longitude": 11.539608410678058,
                  "timestamp": "2022-11-22T16:35:55.438798500Z",
                  "speed": 169,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14249752661043,
                  "longitude": 11.539608410678058,
                  "timestamp": "2022-11-22T16:35:57.452255100Z",
                  "speed": 66,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14257350268423,
                  "longitude": 11.538343113583325,
                  "timestamp": "2022-11-22T16:35:59.469008400Z",
                  "speed": 7,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.14257350268423,
                  "longitude": 11.538343113583325,
                  "timestamp": "2022-11-22T16:36:01.486199800Z",
                  "speed": 218,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.142649478645595,
                  "longitude": 11.53707781648859,
                  "timestamp": "2022-11-22T16:36:03.494830400Z",
                  "speed": 26,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.142649478645595,
                  "longitude": 11.53707781648859,
                  "timestamp": "2022-11-22T16:36:05.505617700Z",
                  "speed": 91,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                },
                {
                  "latitude": 48.142681426838884,
                  "longitude": 11.536545753479004,
                  "timestamp": "2022-11-22T16:36:07.514040400Z",
                  "speed": 105,
                  "wifiState": "HIGH",
                  "gpsState": "VALID"
                }
              ]
            }
          ]
        }
      ]
    }
""".trimIndent()

private val journey by lazy { Json.decodeFromString<Journey>(json) }

fun defaultJourneys(n: Int = 25) = generateSequence { journey }.take(n).toList()
