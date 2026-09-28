import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

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

export function md5Hex(input: string): string {
  const bytes = new TextEncoder().encode(input);
  return md51(bytes).map(rhex).join("");
}

// PHP-style urlencode — matches PayFast's signature construction
// (PHP urlencode: spaces become '+').
export function pfUrlencode(str: string): string {
  return encodeURIComponent(str).replace(/%20/g, "+");
}

// ============ ITN handler ============

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY") ?? "";

serve(async (req: Request) => {
  try {
    if (req.method !== "POST") {
      return new Response("Method not allowed", { status: 405 });
    }

    const formData = await req.formData();
    const data: Record<string, string> = {};
    formData.forEach((value, key) => {
      data[key] = value.toString();
    });

    // Verify ITN signature — replicate PayFast's construction exactly:
    // sorted key=urlencode(value) pairs + '&passphrase=' + urlencode(passphrase), then MD5.
    const passphrase = Deno.env.get("PAYFAST_PASSPHRASE") ?? "";
    const receivedSig = data["signature"] ?? "";
    delete data["signature"];

    const sortedKeys = Object.keys(data).sort();
    let sigString = "";
    for (const key of sortedKeys) {
      if (data[key]) {
        if (sigString) sigString += "&";
        sigString += `${key}=${pfUrlencode(data[key])}`;
      }
    }
    if (passphrase) {
      sigString += `&passphrase=${pfUrlencode(passphrase)}`;
    }

    const expectedSig = md5Hex(sigString);

    if (expectedSig !== receivedSig.toLowerCase()) {
      console.error("ITN signature mismatch");
      return new Response("Invalid signature", { status: 400 });
    }

    // Check payment status
    const paymentStatus = data["payment_status"] ?? "";
    if (paymentStatus !== "COMPLETE") {
      console.log(`Payment not complete: ${paymentStatus}`);
      return new Response("OK", { status: 200 });
    }

    const invoiceId = data["m_payment_id"] ?? "";
    const pfPaymentId = data["pf_payment_id"] ?? "";
    const itnAmount = parseFloat(data["amount"] ?? "0");

    // All DB work happens inside the SECURITY DEFINER RPC — no service_role
    // key is needed, and the payment lookup + verification is atomic.
    const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

    // Pay-All batches share one checkout: m_payment_id = 'batch_<id>'.
    // The payments rows store batch_id exactly as the full m_payment_id
    // (including the 'batch_' prefix), so pass it through unchanged.
    if (invoiceId.startsWith("batch_")) {
      const { data: batchResult, error: batchError } = await supabase.rpc("verify_payfast_batch_payment", {
        p_batch_id: invoiceId,
        p_pf_payment_id: pfPaymentId,
        p_amount: itnAmount,
      });

      if (batchError) {
        console.error("Batch RPC failed:", batchError.message);
        return new Response("Internal error", { status: 500 });
      }

      const batchStatus = batchResult?.status ?? "unknown";
      if (batchStatus === "not_found") {
        console.error(`Batch not found: ${batchId}`);
        return new Response("Payment not found", { status: 404 });
      }
      if (batchStatus === "amount_mismatch") {
        console.error(`Batch amount mismatch: expected=${batchResult.expected} received=${batchResult.received}`);
        return new Response("Amount mismatch", { status: 400 });
      }

      console.log(`Batch ITN processed for ${batchId}: ${batchStatus}`);
      return new Response("OK", { status: 200 });
    }

    const { data: result, error: rpcError } = await supabase.rpc("verify_payfast_payment", {
      p_m_payment_id: invoiceId,
      p_pf_payment_id: pfPaymentId,
      p_amount: itnAmount,
    });

    if (rpcError) {
      console.error("RPC failed:", rpcError.message);
      return new Response("Internal error", { status: 500 });
    }

    const status = result?.status ?? "unknown";
    if (status === "not_found") {
      console.error(`Payment not found for ${invoiceId}`);
      return new Response("Payment not found", { status: 404 });
    }
    if (status === "amount_mismatch") {
      console.error(`Amount mismatch for ${invoiceId}: expected=${result.expected} received=${result.received}`);
      return new Response("Amount mismatch", { status: 400 });
    }

    console.log(`ITN processed for ${invoiceId}: ${status}`);
    return new Response("OK", { status: 200 });
  } catch (e) {
    console.error("ITN error:", e);
    return new Response("Internal error", { status: 500 });
  }
});
