UPDATE claim
  SET county_court_judgment = JSONB_SET(county_court_judgment, '{repaymentPlan}', county_court_judgment->'repaymentPlan'#-'firstPayment')
  WHERE county_court_judgment->'repaymentPlan' ? 'firstPayment';
