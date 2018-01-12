
/*
  Merge title and name for claimants and defendants of type individual
  name: { title, name } => name: {'Mr. full name'}
 */

-- Merge title and name for claimants
DO $$
DECLARE
      length int;
      i int;
BEGIN
    FOR i IN SELECT id FROM claim
    LOOP
    SELECT INTO length jsonb_array_length(jsonb_extract_path(claim::JSONB, 'claimants')) from Claim where id =id;
    FOR counter IN 0..length-1 LOOP
         WITH subquery AS (
          SELECT
            id,
            COALESCE(
              NULLIF(
            ' ' || TRIM(json_extract_path_text(claim :: JSON, 'claimants', counter::text, 'title')) || ' ',
            '  '),
          ' ')
        || TRIM(json_extract_path_text(claim :: JSON, 'claimants', counter::text, 'name')) AS name,
        json_extract_path_text(claim :: JSON, 'claimants', counter::text, 'type') as type
          FROM claim
          where id = i
          )
        UPDATE
          claim
        SET
          claim = jsonb_set(claim, ('{claimants, '  || counter || ', name}')::text[], to_jsonb(name) ,true)
        FROM
          subquery
        WHERE
          subquery.id = claim.id
          and subquery.type='individual'
          and claim.id=i;
    END LOOP;
    END LOOP;
END; $$

-- Remove title from claimants

DO $$
DECLARE
	length int;
	i int;
BEGIN
	FOR i IN SELECT id FROM claim
	LOOP
		SELECT INTO length jsonb_array_length(jsonb_extract_path(claim::JSONB, 'claimants')) from Claim where id =id;
		FOR counter IN 0..length-1
		LOOP

		WITH subquery AS (
			SELECT
			id,
			jsonb_extract_path(claim::JSONB, 'claimants', counter::text) - 'title' as claimant,
			json_extract_path_text(claim :: JSON, 'claimants', counter::text, 'type') as type
			FROM claim
			where id = i
		)
		UPDATE
		claim
			SET
			claim = jsonb_set(claim, ('{claimants, '  || counter || '}')::text[], to_jsonb(claimant) ,true)
		FROM
			subquery
		WHERE
			subquery.id = claim.id
			and subquery.type='individual'
			and claim.id=i;
		END LOOP;
	END LOOP;
END; $$


-- Merge title and name for defendants

DO $$
DECLARE
      length int;
      i int;
BEGIN
    FOR i IN SELECT id FROM claim
    LOOP
    SELECT INTO length jsonb_array_length(jsonb_extract_path(claim::JSONB, 'defendants')) from Claim where id =id;
    FOR counter IN 0..length-1 LOOP
         WITH subquery AS (
          SELECT
            id,
            COALESCE(
              NULLIF(
            ' ' || TRIM(json_extract_path_text(claim :: JSON, 'defendants', counter::text, 'title')) || ' ',
            '  '),
          ' ')
        || TRIM(json_extract_path_text(claim :: JSON, 'defendants', counter::text, 'name')) AS name,
        json_extract_path_text(claim :: JSON, 'defendants', counter::text, 'type') as type
          FROM claim
          where id = i
          )
        UPDATE
          claim
        SET
          claim = jsonb_set(claim, ('{defendants, '  || counter || ', name}')::text[], to_jsonb(name) ,true)
        FROM
          subquery
        WHERE
          subquery.id = claim.id
          and subquery.type='individual'
          and claim.id=i;
    END LOOP;
    END LOOP;
END; $$

-- Remove title from defendants

DO $$
DECLARE
	length int;
	i int;
BEGIN
	FOR i IN SELECT id FROM claim
	LOOP
		SELECT INTO length jsonb_array_length(jsonb_extract_path(claim::JSONB, 'defendants')) from Claim where id =id;
		FOR counter IN 0..length-1
		LOOP

		WITH subquery AS (
			SELECT
			id,
			jsonb_extract_path(claim::JSONB, 'defendants', counter::text) - 'title' as claimant,
			json_extract_path_text(claim :: JSON, 'defendants', counter::text, 'type') as type
			FROM claim
			where id = i
		)
		UPDATE
		claim
			SET
			claim = jsonb_set(claim, ('{defendants, '  || counter || '}')::text[], to_jsonb(claimant) ,true)
		FROM
			subquery
		WHERE
			subquery.id = claim.id
			and subquery.type='individual'
			and claim.id=i;
		END LOOP;
	END LOOP;
END; $$
