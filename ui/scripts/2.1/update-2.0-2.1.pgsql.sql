-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 2.0 to 2.1                              //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='2.1' where obminfo_name='db_version';

-------------------------------------------------------------------------------
-- Update DisplayPref table
-------------------------------------------------------------------------------
-- Add Resource Type
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resourcetype_label', 4, 1);

-------------------------------------------------------------------------------
-- Update Lead table
-------------------------------------------------------------------------------
-- Add contact link
ALTER TABLE Lead ADD COLUMN lead_contact_id integer;
ALTER TABLE Lead ALTER COLUMN lead_contact_id SET DEFAULT 0;

-------------------------------------------------------------------------------
-- Update Resource table
-------------------------------------------------------------------------------
-- Add ResourceType link
ALTER TABLE Resource ADD COLUMN resource_rtype_id integer AFTER resource_domain_id;

-------------------------------------------------------------------------------
-- Add delegation fields
-------------------------------------------------------------------------------
-- UserObm (delegation + delegation target)
ALTER TABLE UserObm ADD COLUMN userobm_delegation_target varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_delegation_target SET DEFAULT '';
ALTER TABLE P_UserObm ADD COLUMN userobm_delegation_target varchar(64);
ALTER TABLE P_UserObm ALTER COLUMN userobm_delegation_target SET DEFAULT '';
ALTER TABLE UserObm ADD COLUMN userobm_delegation varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_delegation SET DEFAULT '';
ALTER TABLE P_UserObm ADD COLUMN userobm_delegation varchar(64);
ALTER TABLE P_UserObm ALTER COLUMN userobm_delegation SET DEFAULT '';

-- UGroup
ALTER TABLE UGroup ADD COLUMN group_delegation varchar(64);
ALTER TABLE UGroup ALTER COLUMN group_delegation SET DEFAULT '';
ALTER TABLE P_UGroup ADD COLUMN group_delegation varchar(64);
ALTER TABLE P_UGroup ALTER COLUMN group_delegation SET DEFAULT '';

-- MailShare
ALTER TABLE MailShare ADD COLUMN mailshare_delegation varchar(64);
ALTER TABLE MailShare ALTER COLUMN mailshare_delegation SET DEFAULT '';
ALTER TABLE P_MailShare ADD COLUMN mailshare_delegation varchar(64);
ALTER TABLE P_MailShare ALTER COLUMN mailshare_delegation SET DEFAULT '';

-- MailShare
ALTER TABLE Host ADD COLUMN host_delegation varchar(64);
ALTER TABLE Host ALTER COLUMN host_delegation SET DEFAULT '';
ALTER TABLE P_Host ADD COLUMN host_delegation varchar(64);
ALTER TABLE P_Host ALTER COLUMN host_delegation SET DEFAULT '';


-------------------------------------------------------------------------------
-- Tables needed for Automate work
-------------------------------------------------------------------------------
--
-- Table structure for the table 'Deleted'
--
CREATE TABLE Deleted (
  deleted_id         serial,
  deleted_domain_id  integer,
  deleted_user_id    integer,
  deleted_delegation varchar(64) DEFAULT '',
  deleted_entity     varchar(32),
  deleted_entity_id  integer,
  deleted_timestamp  timestamp,
  PRIMARY KEY (deleted_id)
);


--
-- Table structure for the table 'Updated'
--
CREATE TABLE Updated (
  updated_id         serial,
  updated_domain_id  integer,
  updated_user_id    integer,
  updated_delegation varchar(64) DEFAULT '',
  updated_entity     varchar(32),
  updated_entity_id  integer,
  updated_type       char(1),
  PRIMARY KEY (updated_id)
);


--
-- Table structure for the table 'ResourceType'
--
CREATE TABLE ResourceType (
  resourcetype_id					  serial,
  resourcetype_domain_id	  integer DEFAULT 0,	
  resourcetype_label			  varchar(32) NOT NULL,
  resourcetype_property		  varchar(32),
  resourcetype_pkind				integer DEFAULT 0 NOT NULL,
  PRIMARY KEY (resourcetype_id)
);

--
-- Table structure for the table 'ResourceItem'
--
CREATE TABLE ResourceItem (
  resourceitem_id								serial,
  resourceitem_domain_id				integer DEFAULT 0,
  resourceitem_label						varchar(32) NOT NULL,
  resourceitem_resourcetype_id	integer NOT NULL,
  resourceitem_description			text,
  PRIMARY KEY (resourceitem_id)
);

--
-- Table structure for the table 'Updatedlinks'
--
CREATE TABLE Updatedlinks (
  updatedlinks_id         serial,
  updatedlinks_domain_id  integer,
  updatedlinks_user_id    integer,
  updatedlinks_delegation varchar(64),
  updatedlinks_table      varchar(32),
  updatedlinks_entity     varchar(32),
  updatedlinks_entity_id  integer,
  PRIMARY KEY (updatedlinks_id)
);
