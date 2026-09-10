require('dotenv').config();
const { createServer } = require('http');
const { Server } = require('socket.io');
const { createClient } = require('@supabase/supabase-js');
const cors = require('cors');

const SUPABASE_URL = process.env.SUPABASE_URL || '';
const SUPABASE_ANON_KEY = process.env.SUPABASE_ANON_KEY || '';
const SOCKET_PORT = process.env.SOCKET_PORT || 3000;

if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
  console.error('Missing SUPABASE_URL or SUPABASE_ANON_KEY in .env');
  process.exit(1);
}

const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

const httpServer = createServer();
const io = new Server(httpServer, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
});

// Simple JWT verification via Supabase auth endpoint
async function verifySocketToken(token) {
  if (!token) return null;
  try {
    const { data, error } = await supabase.auth.getUser(token);
    if (error || !data.user) return null;
    return data.user.id;
  } catch (e) {
    return null;
  }
}

io.use(async (socket, next) => {
  const token = socket.handshake.auth?.token || socket.handshake.query?.token;
  const userId = await verifySocketToken(token);
  if (!userId) {
    return next(new Error('unauthorized'));
  }
  socket.userId = userId;
  next();
});

io.on('connection', (socket) => {
  const userId = socket.userId;
  socket.join(`user:${userId}`);
  console.log(`Socket connected: ${userId}`);

  socket.on('send_message', async (payload, ack) => {
    try {
      const { recipientId, content } = payload;
      if (!recipientId || !content || typeof content !== 'string') {
        return ack?.({ ok: false, error: 'invalid_payload' });
      }

      const { data, error } = await supabase
        .from('messages')
        .insert({
          sender_id: userId,
          recipient_id: recipientId,
          content,
          subject: 'Message',
          category: 'general',
          is_read: false,
          is_announcement: false
        })
        .select('*')
        .single();

      if (error || !data) {
        console.error('send_message insert error', error);
        return ack?.({ ok: false, error: 'insert_failed' });
      }

      const message = {
        id: data.id,
        senderId: data.sender_id,
        recipientId: data.recipient_id,
        content: data.content,
        category: data.category,
        isRead: data.is_read,
        threadId: data.thread_id || null,
        timestamp: data.created_at
      };

      // Emit to recipient
      io.to(`user:${recipientId}`).emit('new_message', message);
      // Emit to sender for multi-device sync
      io.to(`user:${userId}`).emit('new_message', message);

      // Send ack to sender with message id
      io.to(`user:${userId}`).emit('message_ack', {
        id: id,
        status: 'ok',
        callback_id: id
      });

      ack?.({ ok: true, message });
    } catch (e) {
      console.error('send_message exception', e);
      ack?.({ ok: false, error: 'exception' });
    }
  });

  socket.on('history', async ({ otherUserId }, ack) => {
    try {
      if (!otherUserId) {
        return ack?.({ ok: false, error: 'missing_otherUserId' });
      }

      const { data, error } = await supabase
        .from('messages')
        .select('*')
        .or(`and(sender_id.eq.${userId},recipient_id.eq.${otherUserId}),and(sender_id.eq.${otherUserId},recipient_id.eq.${userId})`)
        .order('created_at', { ascending: true });

      if (error) {
        console.error('load_history error', error);
        return ack?.({ ok: false, error: 'query_failed' });
      }

      const messages = (data || []).map((row) => ({
        id: row.id,
        senderId: row.sender_id,
        recipientId: row.recipient_id,
        content: row.content,
        category: row.category,
        isRead: row.is_read,
        threadId: row.thread_id || null,
        timestamp: row.created_at
      }));

      ack?.({ ok: true, messages });
    } catch (e) {
      console.error('load_history exception', e);
      ack?.({ ok: false, error: 'exception' });
    }
  });

  socket.on('disconnect', () => {
    console.log(`Socket disconnected: ${userId}`);
  });
});

httpServer.listen(SOCKET_PORT, () => {
  console.log(`Socket.io messaging server listening on :${SOCKET_PORT}`);
});
