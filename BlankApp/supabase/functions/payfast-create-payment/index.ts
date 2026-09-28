import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

// Builds a signed PayFast checkout URL server-side.
//
// Secrets come from edge function env vars (set via
// `supabase secrets set PAYFAST_MERCHANT_ID=... PAYFAST_MERCHANT_KEY=... PAYFAST_PASSPHRASE=...`),
// never from app_config, so clients can't read them.
//
// verify_jwt is enabled: Supabase rejects calls without a valid Supabase JWT
// before this handler runs.
//
// POST { "invoiceId": string, "amount": number, "itemName": string, "parentEmail": string, "parentId": string }
// -> 200 { "url": "https://www.payfast.co.za/eng/process?...&signature=..." }

// ============ Pure-JS MD5 (Deno's crypto.subtle doesn't support MD5) ============
// Based on the public-domain Joseph Myers implementation, adapted for UTF-8 bytes.

function md5cycle(x: number[], k: number[]) {
  let [a, b, c, d] = x;

  a = ff(a, b, c, d, k[0], 7, -680876936);
  d = ff(d, a, b, c, k[1], 12, -389564586);
  c = ff(c, d, a, b, k[2], 17, 606105819);
  b = ff(b, c, d, a, k[3], 22, -1044525330);
  a = ff(a, b, c, d, k[4], 7, -176418897);
  d = ff(d, a, b, c, k[5], 12, 1200080426);
  c = ff(c, d, a, b, k[6], 17, -1473231341);
  b = ff(b, c, d, a, k[7], 22, -45705983);
  a = ff(a, b, c, d, k[8], 7, 1770035416);
  d = ff(d, a, b, c, k[9], 12, -1958414417);
  c = ff(c, d, a, b, k[10], 17, -42063);
  b = ff(b, c, d, a, k[11], 22, -1990404162);
  a = ff(a, b, c, d, k[12], 7, 1804603682);
  d = ff(d, a, b, c, k[13], 12, -40341101);
  c = ff(c, d, a, b, k[14], 17, -1502002290);
  b = ff(b, c, d, a, k[15], 22, 1236535329);

  a = gg(a, b, c, d, k[1], 5, -165796510);
  d = gg(d, a, b, c, k[6], 9, -1069501632);
  c = gg(c, d, a, b, k[11], 14, 643717713);
  b = gg(b, c, d, a, k[0], 20, -373897302);
  a = gg(a, b, c, d, k[5], 5, -701558691);
  d = gg(d, a, b, c, k[10], 9, 38016083);
  c = gg(c, d, a, b, k[15], 14, -660478335);
  b = gg(b, c, d, a, k[4], 20, -405537848);
  a = gg(a, b, c, d, k[9], 5, 568446438);
  d = gg(d, a, b, c, k[14], 9, -1019803690);
  c = gg(c, d, a, b, k[3], 14, -187363961);
  b = gg(b, c, d, a, k[8], 20, 1163531501);
  a = gg(a, b, c, d, k[13], 5, -1444681467);
  d = gg(d, a, b, c, k[2], 9, -51403784);
  c = gg(c, d, a, b, k[7], 14, 1735328473);
  b = gg(b, c, d, a, k[12], 20, -1926607734);

  a = hh(a, b, c, d, k[5], 4, -378558);
  d = hh(d, a, b, c, k[8], 11, -2022574463);
  c = hh(c, d, a, b, k[11], 16, 1839030562);
  b = hh(b, c, d, a, k[14], 23, -35309556);
  a = hh(a, b, c, d, k[1], 4, -1530992060);
  d = hh(d, a, b, c, k[4], 11, 1272893353);
  c = hh(c, d, a, b, k[7], 16, -155497632);
  b = hh(b, c, d, a, k[10], 23, -1094730640);
  a = hh(a, b, c, d, k[13], 4, 681279174);
  d = hh(d, a, b, c, k[0], 11, -358537222);
  c = hh(c, d, a, b, k[3], 16, -722521979);
  b = hh(b, c, d, a, k[6], 23, 76029189);
  a = hh(a, b, c, d, k[9], 4, -640364487);
  d = hh(d, a, b, c, k[12], 11, -421815835);
  c = hh(c, d, a, b, k[15], 16, 530742520);
  b = hh(b, c, d, a, k[2], 23, -995338651);

  a = ii(a, b, c, d, k[0], 6, -198630844);
  d = ii(d, a, b, c, k[7], 10, 1126891415);
  c = ii(c, d, a, b, k[14], 15, -1416354905);
  b = ii(b, c, d, a, k[5], 21, -57434055);
  a = ii(a, b, c, d, k[12], 6, 1700485571);
  d = ii(d, a, b, c, k[3], 10, -1894986606);
  c = ii(c, d, a, b, k[10], 15, -1051523);
  b = ii(b, c, d, a, k[1], 21, -2054922799);
  a = ii(a, b, c, d, k[8], 6, 1873313359);
  d = ii(d, a, b, c, k[15], 10, -30611744);
  c = ii(c, d, a, b, k[6], 15, -1560198380);
  b = ii(b, c, d, a, k[13], 21, 1309151649);
  a = ii(a, b, c, d, k[4], 6, -145523070);
  d = ii(d, a, b, c, k[11], 10, -1120210379);
  c = ii(c, d, a, b, k[2], 15, 718787259);
  b = ii(b, c, d, a, k[9], 21, -343485551);

  x[0] = add32(a, x[0]);
  x[1] = add32(b, x[1]);
  x[2] = add32(c, x[2]);
  x[3] = add32(d, x[3]);
}

function cmn(q: number, a: number, b: number, x: number, s: number, t: number) {
  a = add32(add32(a, q), add32(x, t));
  return add32((a << s) | (a >>> (32 - s)), b);
}
function ff(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
  return cmn((b & c) | (~b & d), a, b, x, s, t);
}
function gg(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
  return cmn((b & d) | (c & ~d), a, b, x, s, t);
}
function hh(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
  return cmn(b ^ c ^ d, a, b, x, s, t);
}
function ii(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
  return cmn(c ^ (b | ~d), a, b, x, s, t);
}

function add32(a: number, b: number) {
  return (a + b) & 0xFFFFFFFF;
}

function md5blk(bytes: Uint8Array): number[] {
  const blks: number[] = [];
  for (let i = 0; i < 64; i += 4) {
    blks[i >> 2] = bytes[i] + (bytes[i + 1] << 8) + (bytes[i + 2] << 16) + (bytes[i + 3] << 24);
  }
  return blks;
}

function md51(bytes: Uint8Array): number[] {
  const n = bytes.length;
  const state = [1732584193, -271733879, -1732584194, 271733878];
  let i;
  for (i = 64; i <= n; i += 64) {
    md5cycle(state, md5blk(bytes.subarray(i - 64, i)));
  }
  const rest = bytes.subarray(i - 64);
  const bitLen = n * 8;
  if (rest.length >= 56) {
    const tail = new Uint8Array(64);
    tail.set(rest);
    tail[rest.length] = 0x80;
    md5cycle(state, md5blk(tail));
    const tail2 = new Uint8Array(64);
    tail2[56] = bitLen & 0xff;
    tail2[57] = (bitLen >> 8) & 0xff;
    tail2[58] = (bitLen >> 16) & 0xff;
    tail2[59] = (bitLen >>> 24) & 0xff;
    md5cycle(state, md5blk(tail2));
  } else {
    const tail = new Uint8Array(64);
    tail.set(rest);
    tail[rest.length] = 0x80;
    tail[56] = bitLen & 0xff;
    tail[57] = (bitLen >> 8) & 0xff;
    tail[58] = (bitLen >> 16) & 0xff;
    tail[59] = (bitLen >>> 24) & 0xff;
    md5cycle(state, md5blk(tail));
  }
  return state;
}

function rhex(n: number): string {
  let s = "";
  for (let j = 0; j < 4; j++) {
    s += ((n >> (j * 8 + 4)) & 0x0f).toString(16) + ((n >> (j * 8)) & 0x0f).toString(16);
  }
  return s;
}

function md5Hex(input: string): string {
  const bytes = new TextEncoder().encode(input);
  return md51(bytes).map(rhex).join("");
}

// PHP-style urlencode — matches PayFast's signature construction
// (PHP urlencode: spaces become '+').
function pfUrlencode(str: string): string {
  return encodeURIComponent(str).replace(/%20/g, "+");
}

function buildSignatureString(params: Record<string, string>, passphrase: string): string {
  const parts: string[] = [];
  for (const key of Object.keys(params).sort()) {
    const value = params[key];
    if (value !== undefined && value !== null && value !== "") {
      parts.push(`${key}=${pfUrlencode(value)}`);
    }
  }
  let s = parts.join("&");
  if (passphrase) {
    s += `&passphrase=${pfUrlencode(passphrase)}`;
  }
  return s;
}

// ============ Handler ============

const PAYFAST_PROCESS_URL = "https://www.payfast.co.za/eng/process";
const PAYFAST_SANDBOX_URL = "https://sandbox.payfast.co.za/eng/process";

serve(async (req: Request) => {
  try {
    if (req.method !== "POST") {
      return new Response(JSON.stringify({ error: "Method not allowed" }), { status: 405 });
    }

    const authHeader = req.headers.get("Authorization") ?? "";
    const jwt = authHeader.replace(/^Bearer\s+/i, "");
    if (!jwt) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), { status: 401 });
    }

    const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
    const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
      global: { headers: { Authorization: `Bearer ${jwt}` } },
    });
    const { data: authData, error: authError } = await supabase.auth.getUser(jwt);
    if (authError || !authData?.user) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), { status: 401 });
    }

    const merchantId = Deno.env.get("PAYFAST_MERCHANT_ID") ?? "";
    const merchantKey = Deno.env.get("PAYFAST_MERCHANT_KEY") ?? "";
    const passphrase = Deno.env.get("PAYFAST_PASSPHRASE") ?? "";
    const mode = Deno.env.get("PAYFAST_MODE") ?? "production";

    if (!merchantId || !merchantKey) {
      return new Response(JSON.stringify({ error: "PayFast not configured" }), { status: 503 });
    }

    const body = await req.json().catch(() => null);
    if (!body || typeof body !== "object") {
      return new Response(JSON.stringify({ error: "Invalid body" }), { status: 400 });
    }

    const invoiceId = String(body.invoiceId ?? "");
    const requestedAmount = Number(body.amount ?? 0);
    const itemName = String(body.itemName ?? "Payment");
    const parentEmail = String(body.parentEmail ?? "");
    const parentId = String(body.parentId ?? "");
    // Pay-All: client may pass an array of invoice ids to settle in ONE checkout.
    const batchInvoiceIds: string[] = Array.isArray(body.invoiceIds)
      ? body.invoiceIds.map((id: unknown) => String(id)).filter((id: string) => id.length > 0)
      : [];

    const isBatch = batchInvoiceIds.length > 1;

    if (!isBatch && (!invoiceId || !Number.isFinite(requestedAmount) || requestedAmount <= 0)) {
      return new Response(JSON.stringify({ error: "invoiceId and positive amount required" }), { status: 400 });
    }

    // Resolve the amount and checkout reference server-side. For batches the
    // invoices are fetched and verified (ownership + pending status) and the
    // total computed here — the client never gets to choose the amount.
    let amount = requestedAmount;
    let mPaymentId = invoiceId;
    let resolvedItemName = itemName;

    if (isBatch) {
      const { data: invoiceRows, error: invoiceError } = await supabase
        .from("invoices")
        .select("id, amount, description, status, student_id, students!inner(parent_id)")
        .in("id", batchInvoiceIds)
        .eq("status", "pending");

      if (invoiceError || !invoiceRows || invoiceRows.length !== batchInvoiceIds.length) {
        return new Response(
          JSON.stringify({ error: "Some invoices are not payable (missing, already paid, or not yours)" }),
          { status: 400 }
        );
      }

      for (const row of invoiceRows as Record<string, unknown>[]) {
        const students = row.students as Record<string, unknown> | Record<string, unknown>[] | null;
        const studentArr = Array.isArray(students) ? students : students ? [students] : [];
        const ownerOk = studentArr.some((s) => String(s.parent_id ?? "") === parentId);
        if (!ownerOk) {
          return new Response(JSON.stringify({ error: "Invoice does not belong to this parent" }), { status: 403 });
        }
      }

      amount = invoiceRows.reduce((sum, row) => sum + Number(row.amount ?? 0), 0);
      if (!Number.isFinite(amount) || amount <= 0) {
        return new Response(JSON.stringify({ error: "Invalid batch total" }), { status: 400 });
      }
      mPaymentId = `batch_${crypto.randomUUID()}`;
      resolvedItemName = `A+ Study House — ${invoiceRows.length} invoices (batch payment)`;
    }

    // Notify URL: this project's payfast-itn function
    const notifyUrl = `${SUPABASE_URL.replace(/\/$/, "")}/functions/v1/payfast-itn`;

    const params: Record<string, string> = {
      merchant_id: merchantId,
      merchant_key: merchantKey,
      return_url: "https://aplusstudyhouse.co.za/payment/success",
      cancel_url: "https://aplusstudyhouse.co.za/payment/cancel",
      notify_url: notifyUrl,
      name_first: "",
      name_last: "",
      email_address: parentEmail,
      m_payment_id: mPaymentId,
      amount: amount.toFixed(2),
      item_name: resolvedItemName,
      item_description: isBatch
        ? `Batch of ${batchInvoiceIds.length} invoices`.slice(0, 100)
        : `Invoice #${invoiceId}`.slice(0, 100),
      custom_str1: parentId,
      custom_str2: mPaymentId,
    };

    // PayFast checkout expects urlencoded values in the query string
    // (same encoding used for the signature, so verification matches).
    const signature = md5Hex(buildSignatureString(params, passphrase));
    const query = Object.keys(params)
      .filter((k) => params[k] !== "")
      .sort()
      .map((k) => `${k}=${pfUrlencode(params[k])}`)
      .join("&") + `&signature=${signature}`;
    const url = `${mode === "sandbox" ? PAYFAST_SANDBOX_URL : PAYFAST_PROCESS_URL}?${query}`;

    return new Response(
      JSON.stringify({ url, batchPaymentId: isBatch ? mPaymentId : null, amount }),
      {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }
    );
  } catch (e) {
    console.error("payfast-create-payment error:", e);
    return new Response(JSON.stringify({ error: "Internal error" }), { status: 500 });
  }
});
