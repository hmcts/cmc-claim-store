/**
 * Two additional columns to store important piece of information:
 * 1. issued_on - date when claim was posted to defendant
 * 2. response_deadline - date when defendant must take an action
 *
 * If you want to find out how these dates are calculated, please see ROC-984.
 */
DELETE FROM claim;

ALTER TABLE claim
  ADD COLUMN issued_on DATE,
  ADD COLUMN response_deadline DATE;
