-- Reference doc for runnung migration, not expected to be run as part of build
-- submittedOn

WITH subquery AS (
select event.id ,
event.created_date,
json_extract_path_text(data.data :: JSON, 'submittedOn') as date
from case_data data, case_event event where event.case_data_id = data.id and data.jurisdiction='CMC'
and (
  event.event_id ='IssueClaim' or
  event.event_id ='CreateClaim'
  )
)
update case_event event
set
  created_date = subquery.date::timestamp
from subquery
where event.id = subquery.id;


-- responseSubmittedOn

WITH subquery AS (
     WITH dataquery AS (
     SELECT
     case_data.id,
     case_data.jurisdiction,
     defendants -> 'value' as value
     FROM case_data , jsonb_array_elements(case_data.data::JSONB -> 'respondents') defendants
  )
     select event.id ,
     event.created_date,
     json_extract_path_text(data.value::JSON, 'responseSubmittedOn') as date
     from dataquery data, case_event event
     where event.case_data_id = data.id and data.jurisdiction='CMC'
     and ( event.event_id = 'AdmitPart'
     or  event.event_id = 'AdmitAll'
     or event.event_id =  'DisputesAll')
  )
update case_event event
set
  created_date = subquery.date::timestamp
from subquery
where event.id = subquery.id;

-- claimant response

WITH subquery AS (
     WITH dataquery AS (
     SELECT
     case_data.id,
     case_data.jurisdiction,
     defendants -> 'value' as value
     FROM case_data , jsonb_array_elements(case_data.data::JSONB -> 'respondents') defendants
  )
     select event.id ,
     event.created_date,
     json_extract_path_text(data.value::JSON, 'claimantResponse', 'submittedOn') as date
     from dataquery data, case_event event
     where event.case_data_id = data.id and data.jurisdiction='CMC'
     and ( event.event_id = 'ClaimantAccepts'
     or  event.event_id = 'ClaimantRejects')
  )
update case_event event
set
  created_date = subquery.date::timestamp
from subquery
where event.id = subquery.id;

-- ccj

WITH subquery AS (
     WITH dataquery AS (
     SELECT
     case_data.id,
     case_data.jurisdiction,
     defendants -> 'value' as value
     FROM case_data , jsonb_array_elements(case_data.data::JSONB -> 'respondents') defendants
  )
     select event.id ,
     event.created_date,
     json_extract_path_text(data.value::JSON, 'countyCourtJudgmentRequest', 'requestedDate') as date
     from dataquery data, case_event event
     where event.case_data_id = data.id and data.jurisdiction='CMC'
     and (event.event_id = 'DefaultCCJRequested' or
     event.event_id = 'CCJRequested')
  )
update case_event event
set
  created_date = subquery.date::timestamp
from subquery
where event.id = subquery.id;

-- settlements

WITH subquery AS (
      WITH dataquery AS (
          SELECT
          case_data.id,
          case_data.jurisdiction,
          defendants -> 'value' as value
          FROM case_data , jsonb_array_elements(case_data.data::JSONB -> 'respondents') defendants
      )
      select event.id ,
      event.created_date,
      json_extract_path_text(data.value::JSON, 'settlementReachedAt') as date
      from dataquery data, case_event event
      where event.case_data_id = data.id and data.jurisdiction='CMC'
      and (event.event_id = 'OfferCounterSignedByDefendant'
      or event.event_id = 'AgreementCounterSignedByDefendant')
)
update case_event event
set
  created_date = subquery.date::timestamp
from subquery
where event.id = subquery.id
  and subquery.date is not null;

-- re-determination

WITH subquery AS (
     WITH dataquery AS (
        SELECT
        case_data.id,
        case_data.jurisdiction,
        defendants -> 'value' as value
        FROM case_data , jsonb_array_elements(case_data.data::JSONB -> 'respondents') defendants
      )
      select event.id ,
      event.created_date,
      json_extract_path_text(data.value::JSON, 'reDeterminationRequestedAt') as date
      from dataquery data, case_event event
      where event.case_data_id = data.id and data.jurisdiction='CMC'
      and (event.event_id = 'ReferToJudgeByClaimant'
      or event.event_id = 'ReferToJudgeByDefendant')
)
update case_event event
set
  created_date = subquery.date::timestamp
from subquery
where event.id = subquery.id
  and subquery.date is not null;
