UPDATE claim
  SET directions_questionnaire_deadline = responded_at + INTERVAL '19 days'
  WHERE response->>'responseType' = 'FULL_DEFENCE'
    AND directions_questionnaire_deadline IS NULL;
