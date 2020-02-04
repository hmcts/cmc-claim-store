curl -X GET "localhost:9200/_search?pretty" -H 'Content-Type: application/json' -d'{"size": 1000,"query": {"bool" : {"must" : [{"term" : {"state" : {"value" : "open","boost" : 1.0}}},{"range" : {"data.respondents.value.responseSubmittedOn" : {"from" : null,"to" : "2019-11-15","include_lower" : true,"include_upper" : true,"boost" : 1.0}}},{"range" : {"data.submittedOn" : {"from" : "2019-09-09T03:12:00.000Z","to" : null,"include_lower" : true,"include_upper" : true,"boost" : 1.0}}}],"must_not" : [{"exists" : {"field" : "data.respondents.value.paidInFullDate","boost" : 1.0}},{"exists" : {"field" : "data.respondents.value.claimantResponse.submittedOn","boost" : 1.0}}],"adjust_pure_negative" : true,"boost" : 1.0}}}'

curl -X GET "localhost:9200/_search?pretty" -H 'Content-Type: application/json' -d'
{"size": 1000,"query": {
  "bool" : {
    "must" : [
      {
        "term" : {
          "state" : {
            "value" : "open",
            "boost" : 1.0
          }
        }
      },
      {
        "range" : {
          "data.respondents.value.responseSubmittedOn" : {
            "from" : null,
            "to" : "2019-11-15",
            "include_lower" : true,
            "include_upper" : true,
            "boost" : 1.0
          }
        }
      },
      {
        "range" : {
          "data.submittedOn" : {
            "from" : "2019-09-09T03:12:00.000Z",
            "to" : null,
            "include_lower" : true,
            "include_upper" : true,
            "boost" : 1.0
          }
        }
      }
    ],
      "must_not" : [
      {
        "exists" : {
          "field" : "data.respondents.value.paidInFullDate",
          "boost" : 1.0
        }
      },
      {
        "exists" : {
          "field" : "data.respondents.value.claimantResponse.submittedOn",
          "boost" : 1.0
        }
      }
    ],
      "adjust_pure_negative" : true,
      "boost" : 1.0
  }
}}'


{ "query": {  "match_all": { "boost" : 1.2 }}}

curl -X POST "http://localhost:4452/searchCases?ctid=MoneyClaimCase" -H "accept: application/json" -H "Content-Type: application/json" -H H -"Authorization: " "ServiceAuthorization: " -d '{ "query": {  "match_all": { "boost" : 1.2 }}}'


civilmoneyclaims+anonymouscitizen@gmail.com





