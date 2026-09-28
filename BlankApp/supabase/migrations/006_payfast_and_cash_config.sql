-- 006: PayFast + cash payment display config
--
-- NOTE: the PayFast merchant key and passphrase are intentionally NOT stored
-- here. app_config is world-readable to authenticated clients (anon SELECT),
-- so secrets would be exposed. They live only as edge-function secrets:
--
--   supabase secrets set PAYFAST_MERCHANT_ID=... \
--                      PAYFAST_MERCHANT_KEY=... \
--                      PAYFAST_PASSPHRASE=... \
--                      PAYFAST_MODE=production
--
-- The payfast-create-payment edge function signs checkout URLs server-side;
-- the payfast-itn edge function verifies ITN callbacks with PAYFAST_PASSPHRASE.

INSERT INTO app_config (key, value) VALUES
  ('payfast_merchant_id', '23328942'),
  ('payfast_mode', 'production'),
  ('cash_payment_location', 'A+ Study House, Witpoortjie, Roodepoort'),
  ('cash_payment_hours', 'Monday – Friday: 07h00 – 18h00'),
  ('cash_payment_phone', '')
ON CONFLICT (key) DO UPDATE SET
  value = EXCLUDED.value,
  updated_at = now();

-- Defensive: remove any secrets accidentally inserted by older versions
DELETE FROM app_config WHERE key IN ('payfast_merchant_key', 'payfast_passphrase');
