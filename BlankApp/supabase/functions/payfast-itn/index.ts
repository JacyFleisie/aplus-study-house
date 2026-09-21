import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SUPABASE_SERVICE_ROLE = Deno.env.get("SUPABASE_SERVICE_ROLE") ?? "";

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

    // Verify ITN signature
    const passphrase = Deno.env.get("PAYFAST_PASSPHRASE") ?? "";
    const receivedSig = data["signature"] ?? "";
    delete data["signature"];

    // Build signature string
    const sortedKeys = Object.keys(data).sort();
    let sigString = "";
    for (const key of sortedKeys) {
      if (data[key]) {
        if (sigString) sigString += "&";
        sigString += `${key}=${data[key]}`;
      }
    }
    if (passphrase) {
      sigString += `&passphrase=${passphrase}`;
    }

    // MD5 hash
    const encoder = new TextEncoder();
    const hashBuffer = await crypto.subtle.digest("MD5", encoder.encode(sigString));
    const expectedSig = Array.from(new Uint8Array(hashBuffer))
      .map((b) => b.toString(16).padStart(2, "0"))
      .join("");

    if (expectedSig !== receivedSig) {
      console.error("ITN signature mismatch");
      return new Response("Invalid signature", { status: 400 });
    }

    // Check payment status
    const paymentStatus = data["payment_status"] ?? "";
    if (paymentStatus !== "COMPLETE") {
      console.log(`Payment not complete: ${paymentStatus}`);
      return new Response("OK", { status: 200 });
    }

    // Update payment in database
    const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE);
    const invoiceId = data["m_payment_id"] ?? "";
    const amount = parseFloat(data["amount"] ?? "0");
    const pfPaymentId = data["pf_payment_id"] ?? "";

    // Find payment by invoice_id
    const { data: payments, error: findError } = await supabase
      .from("payments")
      .select("*")
      .eq("invoice_id", invoiceId)
      .eq("payment_method", "payfast")
      .eq("status", "pending")
      .single();

    if (findError || !payments) {
      console.error("Payment not found:", findError?.message);
      return new Response("Payment not found", { status: 404 });
    }

    // Update payment status
    const { error: updateError } = await supabase
      .from("payments")
      .update({
        status: "verified",
        verified_at: new Date().toISOString(),
        proof_url: `PayFast Transaction: ${pfPaymentId}`,
      })
      .eq("id", payments.id);

    if (updateError) {
      console.error("Update failed:", updateError.message);
      return new Response("Update failed", { status: 500 });
    }

    // Update invoice status
    await supabase
      .from("invoices")
      .update({ status: "paid", paid_at: new Date().toISOString() })
      .eq("id", invoiceId);

    console.log(`Payment verified for invoice ${invoiceId}`);
    return new Response("OK", { status: 200 });
  } catch (e) {
    console.error("ITN error:", e);
    return new Response("Internal error", { status: 500 });
  }
});
