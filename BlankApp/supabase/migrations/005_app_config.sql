-- Create app_config table if it doesn't exist
CREATE TABLE IF NOT EXISTS app_config (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Insert WhatsApp number
INSERT INTO app_config (key, value) VALUES ('whatsapp_number', '0765616648')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = now();

-- Insert default bank details
INSERT INTO app_config (key, value) VALUES ('bank_name', 'Capitec')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = now();

INSERT INTO app_config (key, value) VALUES ('bank_account_name', 'A Study House Pty Ltd')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = now();

INSERT INTO app_config (key, value) VALUES ('bank_account_number', '105 425 6349')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = now();

INSERT INTO app_config (key, value) VALUES ('bank_branch_code', '')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = now();
