# A+ Study House — Supabase Database Setup Guide

## Prerequisites
- Supabase account (https://supabase.com)
- Project created with ID: `lhybcueknuarolxjuoqi`

## Step 1: Open SQL Editor

1. Go to https://supabase.com/dashboard
2. Select your project
3. Click **SQL Editor** in the left sidebar
4. Click **New query**

## Step 2: Run the Complete Schema

Copy and paste the ENTIRE contents of this file:
```
BlankApp/supabase/migrations/001_initial_schema.sql
```

Click **Run** to execute.

This will create:
- 14 tables with RLS policies
- Indexes for performance
- Triggers for auto-updating timestamps
- Functions for auto-creating profiles

## Step 3: Create Admin User

1. Go to **Authentication** → **Users** in the left sidebar
2. Click **Add user**
3. Enter:
   - Email: `admin@aplusstudy.co.za`
   - Password: Choose a strong password (min 8 chars, uppercase, lowercase, number)
   - Email Confirm: ✅ (check this box)
4. Click **Create user**

## Step 4: Verify Setup

1. Go to **Table Editor** in the left sidebar
2. You should see these tables:
   - profiles
   - students
   - student_sports
   - collection_persons
   - medical_info
   - applications
   - invoices
   - payments
   - documents
   - permissions
   - messages
   - notifications
   - notification_preferences
   - activity_log

3. Go to **Authentication** → **Users**
4. You should see the admin user you created

## Step 5: Test the App

1. Open the A+ Study House app
2. Tap **Enter as Owner**
3. Login with:
   - Email: `admin@aplusstudy.co.za`
   - Password: (the one you set)

If login succeeds, the database is connected!

## Troubleshooting

### "relation does not exist" error
- Make sure you ran the SQL migration completely
- Check the Table Editor to see if tables were created

### "permission denied" error
- Make sure RLS policies were created
- Check that the user has the correct role

### Login fails
- Check the user was created in Authentication → Users
- Make sure email confirmation was checked

## Database Schema Overview

```
profiles (extends auth.users)
├── students
│   ├── student_sports
│   ├── collection_persons
│   └── medical_info
├── applications
├── invoices
│   └── payments
├── documents
├── permissions
├── messages
├── notifications
└── notification_preferences

activity_log (audit trail)
```

## Security

- **RLS enabled** on all 14 tables
- **Parents** can only see their own family's data
- **Admin** can see all data
- **Service role key** never exposed to client
