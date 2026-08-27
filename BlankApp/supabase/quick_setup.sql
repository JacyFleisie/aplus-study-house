-- ============================================
-- A+ STUDY HOUSE — QUICK DATABASE SETUP
-- Run this in Supabase SQL Editor
-- ============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- PROFILES TABLE (extends Supabase Auth)
-- ============================================
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    phone TEXT,
    role TEXT NOT NULL DEFAULT 'parent' CHECK (role IN ('parent', 'admin')),
    surname TEXT,
    id_number TEXT,
    employer TEXT,
    work_phone TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- STUDENTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS students (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    date_of_birth DATE,
    grade INTEGER CHECK (grade BETWEEN 1 AND 7),
    school TEXT,
    address TEXT,
    gender TEXT CHECK (gender IN ('Male', 'Female', '')),
    class_number TEXT,
    teacher_name TEXT,
    lsen BOOLEAN DEFAULT FALSE,
    status TEXT DEFAULT 'pending' CHECK (status IN ('active', 'pending', 'inactive')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- STUDENT SPORTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS student_sports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    sport_name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(student_id, sport_name)
);

-- ============================================
-- COLLECTION PERSONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS collection_persons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    person_name TEXT NOT NULL,
    contact_number TEXT,
    vehicle_registration TEXT,
    person_order INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- MEDICAL INFO TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS medical_info (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID UNIQUE NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    doctor_name TEXT,
    doctor_location TEXT,
    doctor_contact TEXT,
    medical_plan TEXT,
    medical_aid_number TEXT,
    allergies TEXT,
    has_allergies BOOLEAN DEFAULT FALSE,
    epilepsy BOOLEAN DEFAULT FALSE,
    diabetic BOOLEAN DEFAULT FALSE,
    asthma BOOLEAN DEFAULT FALSE,
    nose_bleeder BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- APPLICATIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    student_first_name TEXT NOT NULL,
    student_last_name TEXT NOT NULL,
    student_grade INTEGER CHECK (student_grade BETWEEN 1 AND 7),
    student_dob DATE,
    student_school TEXT,
    student_address TEXT,
    student_gender TEXT,
    student_class_number TEXT,
    student_teacher_name TEXT,
    student_lsen BOOLEAN DEFAULT FALSE,
    sports TEXT,
    collection_person_1 TEXT,
    collection_contact_1 TEXT,
    collection_vehicle_1 TEXT,
    collection_person_2 TEXT,
    collection_contact_2 TEXT,
    collection_vehicle_2 TEXT,
    transport_required BOOLEAN DEFAULT FALSE,
    doctor_name TEXT,
    doctor_location TEXT,
    doctor_contact TEXT,
    medical_plan TEXT,
    medical_aid_number TEXT,
    allergies TEXT,
    has_allergies BOOLEAN DEFAULT FALSE,
    epilepsy BOOLEAN DEFAULT FALSE,
    diabetic BOOLEAN DEFAULT FALSE,
    asthma BOOLEAN DEFAULT FALSE,
    nose_bleeder BOOLEAN DEFAULT FALSE,
    parent_mother_name TEXT,
    parent_mother_surname TEXT,
    parent_mother_id TEXT,
    parent_mother_employer TEXT,
    parent_mother_work_phone TEXT,
    parent_mother_cell TEXT,
    parent_mother_email TEXT,
    parent_father_name TEXT,
    parent_father_surname TEXT,
    parent_father_id TEXT,
    parent_father_employer TEXT,
    parent_father_work_phone TEXT,
    parent_father_cell TEXT,
    parent_father_email TEXT,
    photo_consent BOOLEAN DEFAULT FALSE,
    signature_data TEXT,
    payment_method TEXT CHECK (payment_method IN ('eft', 'cash', 'later')),
    payment_amount DECIMAL(10,2) DEFAULT 450.00,
    payment_proof_url TEXT,
    status TEXT DEFAULT 'submitted' CHECK (status IN ('submitted', 'under_review', 'changes_required', 'payment_verified', 'approved', 'rejected')),
    changes_required TEXT,
    rejection_reason TEXT,
    admin_notes TEXT,
    submitted_at TIMESTAMPTZ DEFAULT NOW(),
    reviewed_at TIMESTAMPTZ,
    payment_verified_at TIMESTAMPTZ,
    approved_at TIMESTAMPTZ,
    rejected_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- INVOICES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    due_date DATE,
    status TEXT DEFAULT 'pending' CHECK (status IN ('paid', 'pending', 'overdue')),
    category TEXT DEFAULT 'aftercare' CHECK (category IN ('aftercare', 'transport', 'stationery', 'registration')),
    paid_date DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- PAYMENTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID REFERENCES invoices(id),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    parent_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL,
    payment_method TEXT NOT NULL CHECK (payment_method IN ('eft', 'cash')),
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'verified', 'rejected')),
    proof_url TEXT,
    admin_notes TEXT,
    payment_date TIMESTAMPTZ DEFAULT NOW(),
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- DOCUMENTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN ('registration', 'medical', 'id_document', 'report', 'photo', 'proof_of_payment', 'other')),
    student_id UUID REFERENCES students(id) ON DELETE SET NULL,
    parent_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    file_url TEXT,
    file_size TEXT,
    upload_date TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- PERMISSIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    description TEXT,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    parent_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES profiles(id),
    category TEXT DEFAULT 'general' CHECK (category IN ('excursion', 'medical', 'photo', 'sports', 'general')),
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'responded')),
    response TEXT CHECK (response IN ('granted', 'declined')),
    response_date TIMESTAMPTZ,
    response_notes TEXT,
    due_date DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- MESSAGES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subject TEXT NOT NULL,
    sender_id UUID NOT NULL REFERENCES profiles(id),
    recipient_id TEXT NOT NULL,
    content TEXT NOT NULL,
    category TEXT DEFAULT 'general' CHECK (category IN ('application', 'finance', 'permission', 'announcement', 'general')),
    is_read BOOLEAN DEFAULT FALSE,
    thread_id UUID,
    parent_message_id UUID REFERENCES messages(id),
    is_announcement BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- NOTIFICATIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT DEFAULT 'general' CHECK (type IN ('payment', 'document', 'application', 'permission', 'message', 'reminder', 'general')),
    is_read BOOLEAN DEFAULT FALSE,
    action_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- NOTIFICATION PREFERENCES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS notification_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    payment_notifications BOOLEAN DEFAULT TRUE,
    document_notifications BOOLEAN DEFAULT TRUE,
    application_notifications BOOLEAN DEFAULT TRUE,
    permission_notifications BOOLEAN DEFAULT TRUE,
    message_notifications BOOLEAN DEFAULT TRUE,
    reminder_notifications BOOLEAN DEFAULT TRUE,
    announcement_notifications BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- ACTIVITY LOG TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS activity_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    actor_id UUID REFERENCES profiles(id),
    actor_name TEXT NOT NULL,
    action TEXT NOT NULL,
    target TEXT NOT NULL,
    type TEXT DEFAULT 'general' CHECK (type IN ('application', 'payment', 'permission', 'document', 'message', 'student', 'system')),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- ENABLE ROW LEVEL SECURITY
-- ============================================
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE students ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_sports ENABLE ROW LEVEL SECURITY;
ALTER TABLE collection_persons ENABLE ROW LEVEL SECURITY;
ALTER TABLE medical_info ENABLE ROW LEVEL SECURITY;
ALTER TABLE applications ENABLE ROW LEVEL SECURITY;
ALTER TABLE invoices ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE permissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification_preferences ENABLE ROW LEVEL SECURITY;
ALTER TABLE activity_log ENABLE ROW LEVEL SECURITY;

-- ============================================
-- RLS POLICIES (Simplified)
-- ============================================

-- Profiles: Users can view/update own, admins can view all
CREATE POLICY "Users can view own profile" ON profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON profiles FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Allow profile creation" ON profiles FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Admins can view all profiles" ON profiles FOR SELECT USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));

-- Students: Parents can view own children, admins can view all
CREATE POLICY "Parents can view own children" ON students FOR SELECT USING (parent_id = auth.uid());
CREATE POLICY "Parents can insert own children" ON students FOR INSERT WITH CHECK (parent_id = auth.uid());
CREATE POLICY "Parents can update own children" ON students FOR UPDATE USING (parent_id = auth.uid());
CREATE POLICY "Admins can view all students" ON students FOR SELECT USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));
CREATE POLICY "Admins can manage all students" ON students FOR ALL USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));

-- Applications: Parents can view/create own, admins can view all
CREATE POLICY "Parents can view own applications" ON applications FOR SELECT USING (parent_id = auth.uid());
CREATE POLICY "Parents can create own applications" ON applications FOR INSERT WITH CHECK (parent_id = auth.uid());
CREATE POLICY "Admins can view all applications" ON applications FOR SELECT USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));
CREATE POLICY "Admins can update any application" ON applications FOR UPDATE USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));

-- Invoices: Parents can view own children's, admins can view all
CREATE POLICY "Parents can view own children invoices" ON invoices FOR SELECT USING (EXISTS (SELECT 1 FROM students WHERE id = student_id AND parent_id = auth.uid()));
CREATE POLICY "Admins can view all invoices" ON invoices FOR SELECT USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));
CREATE POLICY "Admins can manage invoices" ON invoices FOR ALL USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));

-- Payments: Parents can view/create own, admins can view all
CREATE POLICY "Parents can view own payments" ON payments FOR SELECT USING (parent_id = auth.uid());
CREATE POLICY "Parents can create own payments" ON payments FOR INSERT WITH CHECK (parent_id = auth.uid());
CREATE POLICY "Admins can view all payments" ON payments FOR SELECT USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));
CREATE POLICY "Admins can update any payment" ON payments FOR UPDATE USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));

-- Documents: Parents can view/upload own, admins can view all
CREATE POLICY "Parents can view own documents" ON documents FOR SELECT USING (parent_id = auth.uid());
CREATE POLICY "Parents can upload own documents" ON documents FOR INSERT WITH CHECK (parent_id = auth.uid());
CREATE POLICY "Parents can delete own documents" ON documents FOR DELETE USING (parent_id = auth.uid());
CREATE POLICY "Admins can view all documents" ON documents FOR SELECT USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));

-- Permissions: Parents can view/respond to own, admins can create/manage
CREATE POLICY "Parents can view own permissions" ON permissions FOR SELECT USING (parent_id = auth.uid());
CREATE POLICY "Parents can update own permissions" ON permissions FOR UPDATE USING (parent_id = auth.uid());
CREATE POLICY "Admins can view all permissions" ON permissions FOR SELECT USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));
CREATE POLICY "Admins can create permissions" ON permissions FOR INSERT WITH CHECK (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));

-- Messages: Users can view sent/received, admins can view all
CREATE POLICY "Users can view own messages" ON messages FOR SELECT USING (sender_id = auth.uid() OR recipient_id = auth.uid()::text OR recipient_id = 'ALL');
CREATE POLICY "Users can send messages" ON messages FOR INSERT WITH CHECK (sender_id = auth.uid());
CREATE POLICY "Admins can view all messages" ON messages FOR SELECT USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));

-- Notifications: Users can view/update own
CREATE POLICY "Users can view own notifications" ON notifications FOR SELECT USING (user_id = auth.uid());
CREATE POLICY "Users can update own notifications" ON notifications FOR UPDATE USING (user_id = auth.uid());

-- Activity Log: Only admins can view
CREATE POLICY "Admins can view activity log" ON activity_log FOR SELECT USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin'));

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================
CREATE INDEX IF NOT EXISTS idx_students_parent_id ON students(parent_id);
CREATE INDEX IF NOT EXISTS idx_applications_parent_id ON applications(parent_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_invoices_student_id ON invoices(student_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_payments_student_id ON payments(student_id);
CREATE INDEX IF NOT EXISTS idx_payments_parent_id ON payments(parent_id);
CREATE INDEX IF NOT EXISTS idx_documents_parent_id ON documents(parent_id);
CREATE INDEX IF NOT EXISTS idx_permissions_parent_id ON permissions(parent_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);

-- ============================================
-- AUTO-CREATE PROFILE TRIGGER
-- ============================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, full_name, phone, role)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email),
        COALESCE(NEW.raw_user_meta_data->>'phone', ''),
        COALESCE(NEW.raw_user_meta_data->>'role', 'parent')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to auto-create profile on signup
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================
-- AUTO-UPDATE TIMESTAMP TRIGGER
-- ============================================
CREATE OR REPLACE FUNCTION public.update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply updated_at trigger to relevant tables
CREATE TRIGGER update_profiles_updated_at BEFORE UPDATE ON profiles FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER update_students_updated_at BEFORE UPDATE ON students FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER update_medical_info_updated_at BEFORE UPDATE ON medical_info FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER update_applications_updated_at BEFORE UPDATE ON applications FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER update_invoices_updated_at BEFORE UPDATE ON invoices FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER update_permissions_updated_at BEFORE UPDATE ON permissions FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();
CREATE TRIGGER update_notification_preferences_updated_at BEFORE UPDATE ON notification_preferences FOR EACH ROW EXECUTE FUNCTION public.update_updated_at();

-- ============================================
-- DONE! Database is ready.
-- ============================================
