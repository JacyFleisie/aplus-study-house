/**
 * One-off script: enable Supabase Realtime for messages/notifications/permissions.
 * Adds the tables to the supabase_realtime publication so postgres_changes events fire.
 *
 * Usage: node enable-realtime.mjs "<postgres-connection-string>"
 * The connection string is passed as an argument and never written to disk.
 */
import pg from 'pg';

const connStr = process.argv[2];
if (!connStr) {
  console.error('Usage: node enable-realtime.mjs "<connection-string>"');
  process.exit(1);
}

const client = new pg.Client({ connectionString: connStr, ssl: { rejectUnauthorized: false } });
await client.connect();

// Check current publication members
const before = await client.query(
  "SELECT tablename FROM pg_publication_tables WHERE pubname = 'supabase_realtime'"
);
console.log('Tables already in publication:', before.rows.map((r) => r.tablename));

for (const table of ['notifications', 'messages', 'permissions']) {
  const exists = before.rows.some((r) => r.tablename === table);
  if (!exists) {
    await client.query(`ALTER PUBLICATION supabase_realtime ADD TABLE public.${table}`);
    console.log(`Added ${table} to supabase_realtime`);
  } else {
    console.log(`${table} already in publication`);
  }
}

// Full row images on UPDATE/DELETE (so payloads carry complete records)
await client.query('ALTER TABLE public.messages REPLICA IDENTITY FULL');
await client.query('ALTER TABLE public.permissions REPLICA IDENTITY FULL');
await client.query('ALTER TABLE public.notifications REPLICA IDENTITY FULL');
console.log('Replica identity set to FULL');

const after = await client.query(
  "SELECT tablename FROM pg_publication_tables WHERE pubname = 'supabase_realtime'"
);
console.log('Publication now includes:', after.rows.map((r) => r.tablename));

await client.end();
