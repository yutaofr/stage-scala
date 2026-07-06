CREATE KEYSPACE IF NOT EXISTS clearing
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE clearing;

CREATE TABLE IF NOT EXISTS processing_state (
  tx_id text PRIMARY KEY,
  stage text,
  source_topic text,
  source_partition int,
  source_offset bigint,
  updated_at timestamp
);

CREATE TABLE IF NOT EXISTS clearing_history_by_day (
  clearing_date date,
  bucket smallint,
  created_at timestamp,
  tx_id text,
  sender text,
  receiver text,
  amount decimal,
  status text,
  PRIMARY KEY ((clearing_date, bucket), created_at, tx_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

CREATE TABLE IF NOT EXISTS transactions_by_bank_day (
  bank_id text,
  clearing_date date,
  created_at timestamp,
  tx_id text,
  direction text,
  counterparty text,
  amount decimal,
  PRIMARY KEY ((bank_id, clearing_date), created_at, tx_id)
) WITH CLUSTERING ORDER BY (created_at DESC);

CREATE TABLE IF NOT EXISTS bank_positions (
  bank_id text,
  clearing_date date,
  tx_id text,
  net_position decimal,
  tx_count int,
  updated_at timestamp,
  PRIMARY KEY ((bank_id, clearing_date), tx_id)
);

CREATE TABLE IF NOT EXISTS pair_activity_by_day (
  clearing_date date,
  bank_pair text,
  tx_id text,
  amount decimal,
  PRIMARY KEY ((clearing_date), bank_pair, tx_id)
);
