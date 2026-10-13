-- Viyzo Worker: safe starter schema.
-- Review and run manually in Supabase SQL Editor. This file does NOT execute itself.
-- Never put database passwords, service_role keys, or secret keys in this repository.

create extension if not exists pgcrypto;

create table if not exists public.worker_profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text,
  phone text,
  role text not null default 'worker' check (role in ('worker', 'supervisor', 'admin')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.jobs (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  description text,
  location text,
  pay_amount numeric(12,2) not null default 0 check (pay_amount >= 0),
  currency text not null default 'INR',
  status text not null default 'open' check (status in ('draft', 'open', 'paused', 'closed')),
  created_by uuid references auth.users(id) on delete set null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.job_applications (
  id uuid primary key default gen_random_uuid(),
  job_id uuid not null references public.jobs(id) on delete cascade,
  worker_id uuid not null references auth.users(id) on delete cascade,
  message text,
  status text not null default 'submitted' check (status in ('submitted', 'reviewing', 'approved', 'rejected', 'completed')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (job_id, worker_id)
);

create table if not exists public.worker_verifications (
  id uuid primary key default gen_random_uuid(),
  worker_id uuid not null references auth.users(id) on delete cascade,
  verification_type text not null check (verification_type in ('identity', 'selfie', 'document')),
  status text not null default 'pending' check (status in ('pending', 'approved', 'rejected')),
  reviewer_id uuid references auth.users(id) on delete set null,
  reviewer_note text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.worker_payouts (
  id uuid primary key default gen_random_uuid(),
  worker_id uuid not null references auth.users(id) on delete cascade,
  amount numeric(12,2) not null check (amount > 0),
  currency text not null default 'INR',
  status text not null default 'pending' check (status in ('pending', 'processing', 'paid', 'failed', 'cancelled')),
  reference text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.worker_profiles enable row level security;
alter table public.jobs enable row level security;
alter table public.job_applications enable row level security;
alter table public.worker_verifications enable row level security;
alter table public.worker_payouts enable row level security;

-- Remove/recreate only policies owned by this starter migration, so it is rerunnable.
drop policy if exists "worker_profiles_select_own" on public.worker_profiles;
create policy "worker_profiles_select_own" on public.worker_profiles
  for select to authenticated using (id = (select auth.uid()));

drop policy if exists "worker_profiles_insert_own_worker" on public.worker_profiles;
create policy "worker_profiles_insert_own_worker" on public.worker_profiles
  for insert to authenticated with check (id = (select auth.uid()) and role = 'worker');

drop policy if exists "worker_profiles_update_own_nonprivileged" on public.worker_profiles;
create policy "worker_profiles_update_own_nonprivileged" on public.worker_profiles
  for update to authenticated
  using (id = (select auth.uid()))
  with check (id = (select auth.uid()) and role = 'worker');

drop policy if exists "jobs_read_open_authenticated" on public.jobs;
create policy "jobs_read_open_authenticated" on public.jobs
  for select to authenticated using (status = 'open');

drop policy if exists "applications_read_own" on public.job_applications;
create policy "applications_read_own" on public.job_applications
  for select to authenticated using (worker_id = (select auth.uid()));

drop policy if exists "applications_create_own" on public.job_applications;
create policy "applications_create_own" on public.job_applications
  for insert to authenticated
  with check (
    worker_id = (select auth.uid())
    and exists (
      select 1 from public.jobs
      where jobs.id = job_applications.job_id and jobs.status = 'open'
    )
  );

drop policy if exists "verifications_read_own" on public.worker_verifications;
create policy "verifications_read_own" on public.worker_verifications
  for select to authenticated using (worker_id = (select auth.uid()));

drop policy if exists "payouts_read_own" on public.worker_payouts;
create policy "payouts_read_own" on public.worker_payouts
  for select to authenticated using (worker_id = (select auth.uid()));

-- Intentionally no client insert/update policies for verification or payouts.
-- Intentionally no client job-management or application-status-update policies.
-- Those actions must go through trusted server-side code (for example, a Cloudflare Worker)
-- after authentication/authorization checks. Never expose the Supabase service_role key in Android.
