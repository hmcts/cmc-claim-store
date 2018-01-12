
/*
  Merge title and name for claimants and defendants of type individual
  name: { title, name } => name: {'Mr. full name'}
 */

-- Function to merge title and name

CREATE OR REPLACE FUNCTION merge_title_and_name(p_partyType varchar ) RETURNS void AS $$
DECLARE
      length int;
      i int;
BEGIN
    FOR i IN SELECT id FROM claim
    LOOP
    SELECT INTO length jsonb_array_length(jsonb_extract_path(claim::JSONB, p_partyType)) from Claim where id =id;
    FOR counter IN 0..length-1 LOOP
         WITH subquery AS (
          SELECT
            id,
            COALESCE(
              NULLIF(
            ' ' || TRIM(json_extract_path_text(claim :: JSON, p_partyType, counter::text, 'title')) || ' ',
            '  '),
          ' ')
        || TRIM(json_extract_path_text(claim :: JSON, p_partyType, counter::text, 'name')) AS name,
        json_extract_path_text(claim :: JSON, p_partyType, counter::text, 'type') as type
          FROM claim
          where id = i
          )
        UPDATE
          claim
        SET
          claim = jsonb_set(claim, ('{'||p_partyType||', '  || counter || ', name}')::text[], to_jsonb(name) ,true)
        FROM
          subquery
        WHERE
          subquery.id = claim.id
          and subquery.type='individual'
          and claim.id=i;
    END LOOP;
    END LOOP;
    END;
$$ LANGUAGE plpgsql;



-- Function to remove title from claimants

CREATE OR REPLACE FUNCTION remove_title_from_individuals(p_partyType varchar ) RETURNS void AS $$
DECLARE
	length int;
	i int;
BEGIN
	FOR i IN SELECT id FROM claim
	LOOP
		SELECT INTO length jsonb_array_length(jsonb_extract_path(claim::JSONB, p_partyType)) from Claim where id =id;
		FOR counter IN 0..length-1
		LOOP

		WITH subquery AS (
			SELECT
			id,
			jsonb_extract_path(claim::JSONB, p_partyType, counter::text) - 'title' as claimant,
			json_extract_path_text(claim :: JSON, p_partyType, counter::text, 'type') as type
			FROM claim
			where id = i
		)
		UPDATE
		claim
			SET
			claim = jsonb_set(claim, ('{'||p_partyType||', '  ||counter || '}')::text[], to_jsonb(claimant) ,true)
		FROM
			subquery
		WHERE
			subquery.id = claim.id
			and subquery.type='individual'
			and claim.id=i;
		END LOOP;
	END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Merge title and name for claimants
SELECT merge_title_and_name('claimants');

-- Remove title from claimants
SELECT remove_title_from_individuals('claimants');

-- Merge title and name for defendants
SELECT merge_title_and_name('defendants');

-- Remove title from defendants
SELECT remove_title_from_individuals('defendants');
