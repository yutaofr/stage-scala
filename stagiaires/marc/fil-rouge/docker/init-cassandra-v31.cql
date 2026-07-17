CREATE KEYSPACE IF NOT EXISTS clearing
  WITH replication = {
    'class': 'NetworkTopologyStrategy',
    'datacenter1': 1
  }
  AND durable_writes = true;

USE clearing;

CREATE TABLE IF NOT EXISTS processing_state (
  event_key text,
  payload_fingerprint text,
  stage text,
  decision_kind text,
  source_topic text,
  source_partition int,
  source_offset bigint,
  updated_at timestamp,
  PRIMARY KEY ((event_key), payload_fingerprint)
);

CREATE TABLE IF NOT EXISTS clearing_history_by_day (
  clearing_date date,
  bucket smallint,
  occurred_at timestamp,
  event_key text,
  transaction_id int,
  sender text,
  receiver text,
  settlement_amount decimal,
  currency text,
  status text,
  PRIMARY KEY ((clearing_date, bucket), occurred_at, event_key)
) WITH CLUSTERING ORDER BY (occurred_at DESC, event_key ASC);

CREATE TABLE IF NOT EXISTS transactions_by_bank_day (
  bank_id text,
  clearing_date date,
  occurred_at timestamp,
  event_key text,
  direction text,
  counterparty text,
  amount decimal,
  currency text,
  PRIMARY KEY (
    (bank_id, clearing_date),
    occurred_at,
    event_key,
    direction
  )
) WITH CLUSTERING ORDER BY (
  occurred_at DESC,
  event_key ASC,
  direction ASC
);

CREATE TABLE IF NOT EXISTS bank_positions (
  bank_id text,
  clearing_date date,
  event_key text,
  contribution decimal,
  transaction_count int,
  occurred_at timestamp,
  PRIMARY KEY ((bank_id, clearing_date), event_key)
);

CREATE TABLE IF NOT EXISTS pair_activity_by_day (
  clearing_date date,
  bank_pair text,
  event_key text,
  amount decimal,
  transaction_count int,
  PRIMARY KEY ((clearing_date), bank_pair, event_key)
);
