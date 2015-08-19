CREATE TYPE cases.case_temp as
(
	  id bigint,
	  creation_date timestamp without time zone,
	  modification_date timestamp without time zone,
	  name character varying(255),
	  "number" character varying(255),
	  current_case_stage_id bigint,
	  case_definition_id bigint,
	  short_num text,
	  total_count bigint
);

CREATE OR REPLACE FUNCTION build_select_case_query(in_query text, in_case_no character varying, in_case_short_no character varying, in_assignedperson character varying, in_createdate date, in_createdateto date, in_createdaterange boolean, in_stages character varying, in_textsearch character varying)
	RETURNS character varying AS
$BODY$
declare
	v_query text;
	v_filter text;
	v_case_no varchar;
	v_case_short_no varchar;
	v_result bigint;
	v_createDate varchar;
	v_createDateTo varchar;
	v_stages varchar[];
	v_stages_sql text;
	c_date_format constant varchar := 'yyyy-mm-dd';
	v_textsearch text;
begin
	v_query := in_query;
	v_filter = ' WHERE c.id = ca.case_id ';

	v_case_no = trim(in_case_no);
	if(v_case_no is not null and v_case_no <> '')
	then
		v_filter := v_filter || ' AND c.number ilike ' || quote_literal('%' || v_case_no || '%');
	end if;
	v_case_short_no = trim(in_case_short_no);
	if(v_case_short_no is not null and v_case_short_no <> '')
	then
		if(LENGTH(v_case_short_no) > 4) then
			v_filter := v_filter || ' AND replace(c.number, substring(c.number, ''^(.*/).*/.*''), '''') ilike ' || quote_literal(v_case_short_no || '%');
		else
			v_filter := v_filter || ' AND replace(c.number, substring(c.number, ''^(.*/).*/.*''), '''') ilike lpad( ' || quote_literal(v_case_short_no) || ',4, ''0'') || ''%''';
		end if;
	end if;

	if (in_createdate is not null and in_createdaterange = false) then
		v_createDate := to_char(in_createdate, c_date_format);
		v_filter := v_filter || ' and to_char(c.creation_date, ''' || c_date_format || ''') = ' || quote_literal(v_createDate);
	end if;
	if (in_createdaterange = true and (in_createdate is not null or in_createdateto is not null)) then
		if (in_createdate is not null) then
			v_filter := v_filter || ' and c.creation_date >= to_date(' || quote_literal(to_char(in_createdate, c_date_format)) || ', ''' || c_date_format || ''')';
		end if;
		if (in_createdateto is not null) then
			v_filter := v_filter || ' and c.creation_date < to_date(' || quote_literal(to_char(in_createdateto + interval '1 day', c_date_format)) || ', ''' || c_date_format || ''')';
		end if;
	end if;

	if (in_stages is not null) then
		v_stages := string_to_array(in_stages, ',');
		for i in 1..array_upper(v_stages, 1) loop
			v_stages[i] := quote_literal(v_stages[i]);
		end loop;
		v_stages_sql := array_to_string(v_stages, ',');
		if v_stages_sql is not null then
			v_filter := v_filter || ' and exists(select 1 from cases.pt_case_stage sg where sg.case_id = c.id and sg.id = c.current_case_stage_id and sg.name in (' || v_stages_sql || '))';
		end if;
	end if;

	v_textsearch := trim(in_textsearch);
	if (v_textsearch is not null and v_textsearch <> '') then
		v_textsearch := plainto_tsquery(unaccent(v_textsearch));
		v_textsearch :=  v_textsearch || ':*';

		v_filter := v_filter || ' and exists(';
		v_filter := v_filter || 'select 1 from cases.pt_case_stage ts_cs join cases.pt_case_stage_attr ts_csa on ts_csa.case_stage_id = ts_cs.id and ts_csa.key = ''comment'' join cases.pt_case_stage_comments_attr ts_csca on ts_csca.id = ts_csa.id and cases.pt_case_comment ts_cc join ts_csca on ts_cc.case_stage_comment_id = ts_csca.id';
		v_filter := v_filter || 'where ts_cs.case_id = c.id and ';
		v_filter := v_filter || 'to_tsvector(''simple'', unaccent(ts_cc.comment_body)) @@ to_tsquery(' || quote_literal(v_textsearch) || ')';
		v_filter := v_filter || 'union all ';
		v_filter := v_filter || 'select 1 FROM cases.pt_case_s_attr c_s_attr WHERE c_s_attr.case_id = c.id AND ';
		v_filter := v_filter || 'to_tsvector(''simple'', c_s_attr.key || '' '' || c_s_attr.value) @@ to_tsquery(' || quote_literal(v_textsearch) || ') ';


		v_filter := v_filter || '))';
	end if;

	v_query := v_query || v_filter;

	return v_query;
end; $BODY$
	LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION select_case_count(in_case_no character varying, in_case_short_no character varying, in_createdate date, in_createdateto date, in_createdaterange boolean, in_stages character varying, in_textsearch character varying)
	RETURNS bigint AS
$BODY$
declare
	v_query text;
	v_filter text;
	v_case_no varchar;
	v_result bigint;
begin
	v_query := 'SELECT count(c.*) from cases.pt_case c WHERE exists(select ca.id from cases.pt_case_attr ca ';

	v_query := build_select_case_query(v_query, in_case_no, in_case_short_no, in_createdate, in_createdateto, in_createdaterange, in_stages, in_textsearch);

	v_query := v_query || ' ) ';

	for v_result in  execute v_query loop
	 	return  v_result;
	end loop;

end; $BODY$
	LANGUAGE plpgsql VOLATILE
	COST 100;

CREATE OR REPLACE FUNCTION select_case(in_case_no character varying, in_orderby character varying, in_ascorder boolean, in_pagesize integer, in_currentpage integer, in_createdate date, in_createdateto date, in_createdaterange boolean, in_stages character varying, in_textsearch character varying)
	RETURNS SETOF cases.case_temp AS
$BODY$
declare
	v_query text;
	v_filter text;
	v_result complaints.pt_case;
	v_sortorder varchar;
	v_case_no varchar;
	v_row cases.case_temp;
	v_inserted boolean;
begin
	IF in_ascOrder = true THEN
		v_sortorder := 'ASC';
	ELSE
		v_sortorder := 'DESC';
	END IF;

	v_inserted := false;

	v_query := 'SELECT c.*, substr(c.number, 9) ||  substr(c.number, 4, 4), count(*) OVER() AS full_count
	from complaints.pt_case c
	inner join cases.pt_case_attr ca on (ca.case_id = c.id)';

	v_query := build_select_case_query(v_query, in_case_no, in_case_short_no, in_createdate, in_createdateto, in_createdaterange, in_stages, in_textsearch);

    IF in_orderBy = 'number' THEN
			v_query := v_query || ' GROUP BY c.id ';
    		v_query := v_query || ' ORDER BY substr(c.number, 9) ||  substr(c.number, 4, 4)  ' || v_sortorder;
	ELSEIF in_orderBy = 'creation_date' THEN
		v_query := v_query || ' GROUP BY c.id, c.creation_date ';
		v_query := v_query || ' ORDER BY c.creation_date ' || v_sortorder;
	ELSEIF in_orderBy IS NOT NULL AND in_orderBy != '' THEN
		v_query := v_query || ' ORDER BY ' || in_orderBy || ' ' || v_sortorder;
	ELSE
		v_query := v_query || ' ORDER BY number ' || v_sortorder;
	END IF;

 	v_query := v_query || ' limit ' || in_pageSize || ' offset ' || in_currentPage;

	RAISE NOTICE '%',v_query;

	CREATE TEMP TABLE case_query_count(case_count bigint) ON COMMIT DROP;


	for v_row  in  execute v_query loop
		IF v_inserted = false THEN
			INSERT INTO case_query_count(case_count) values (v_row.total_count);
			v_inserted := true;
		END IF;

	 	return next v_row ;
	end loop;


end; $BODY$
	LANGUAGE plpgsql VOLATILE
	COST 100
	ROWS 1000;

DROP INDEX IF EXISTS cases.idx_pt_case_comment_body;

CREATE INDEX idx_pt_case_comment_body ON cases.pt_case_comment USING gin(to_tsvector('simple', comment_body));

DROP INDEX IF EXISTS cases.idx_pt_case_s_l_attr_val;

CREATE INDEX idx_pt_case_s_l_attr_val ON cases.pt_case_s_l_attr USING gin(to_tsvector('simple', value));

DROP INDEX IF EXISTS cases.idx_pt_case_number_short;

CREATE INDEX idx_pt_case_number_short ON cases.pt_case (substring(number, '^(.*/).*/.*', ''));

CREATE EXTENSION IF NOT EXISTS unaccent;
