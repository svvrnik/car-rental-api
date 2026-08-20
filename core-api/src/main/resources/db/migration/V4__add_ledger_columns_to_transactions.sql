ALTER TABLE transactions ADD COLUMN transaction_id VARCHAR(255) NOT NULL;

ALTER TABLE transactions ADD CONSTRAINT uc_transactions_transaction_id UNIQUE (transaction_id);

ALTER TABLE transactions ADD COLUMN reference VARCHAR(255);