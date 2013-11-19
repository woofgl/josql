-- Create user only once: CREATE USER josql_test_user WITH password 'welcome';

DROP DATABASE IF EXISTS josql_test_db;

CREATE DATABASE josql_test_db;

ALTER DATABASE josql_test_db OWNER TO josql_test_user;

\connect josql_test_db

SET search_path = public;

-- --------- user --------- --

CREATE TABLE "user"
(
  id bigserial NOT NULL,
  username character varying(255),
  create_date timestamp without time zone,
  CONSTRAINT user_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "user"
  OWNER TO josql_test_user;
  
-- --------- /user --------- --  

-- --------- contact --------- --

CREATE TABLE contact
(
  id bigint NOT NULL,
  name character varying(255),
  "firstName" character varying(255),
  email character varying(255),
  create_date timestamp without time zone,
  CONSTRAINT contact_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contact
  OWNER TO josql_test_user;
  
-- --------- /contact --------- --  


-- --------- role --------- --

CREATE TABLE role
(
  id bigserial NOT NULL,
  name character varying(255),
  CONSTRAINT role_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE role
  OWNER TO josql_test_user;

  
-- --------- /role --------- --  

-- --------- team --------- --

CREATE TABLE team
(
  id serial NOT NULL,
  name character varying(255),
  CONSTRAINT team_pk PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE team
  OWNER TO josql_test_user;
  
-- --------- /team --------- --  


-- --------- team_contact --------- --

CREATE TABLE team_contact
(
  contact_id bigint NOT NULL,
  team_id bigint NOT NULL,
  CONSTRAINT team_contact_pk PRIMARY KEY (contact_id, team_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE team_contact
  OWNER TO josql_test_user;
  
-- --------- /team_contact --------- --