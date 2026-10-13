-- Viyzo Worker additive backend tables.
-- Safe to apply on the existing project: does not replace or delete existing tables/data.

create table if not exists public.job_applications (
  id uuid primary key default gen_random_uuid(),
  job_id uuid not null references public.jobs(id) on delete cascade,
  worker_id uuid not null references auth.users(id) on delete cascade,
  message text,
  status text not null default 'submitted'
    check (status in ('submitted', 'reviewing', 'approved', 'rejected', 'completed')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (job_id, worker_id)
);

create table if not exists public.worker_verifications (
  id uuid primary key default gen_random_uuid(),
  worker_id uuid not null references auth.users(id) on delete cascade,
  verification_type text not null
    check (verification_type in ('identity', 'selfie', 'document')),
  status text not null default 'pending'
    check (status in ('pending', 'approved', 'rejected')),
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
  status text not null default 'pending'
    check (status in ('pending', 'processing', 'paid', 'failed', 'cancelled')),
  reference text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.job_applications enable row level security;
alter table public.worker_verifications enable row level security;
alter table public.worker_payouts enable row level security;

drop policy if exists "viyzo_applications_read_own" on public.job_applications;
create policy "viyzo_applications_read_own" on public.job_applications
  for select to authenticated using (worker_id = (select auth.uid()));

drop policy if exists "viyzo_applications_create_own" on public.job_applications;
create policy "viyzo_applications_create_own" on public.job_applications
  for insert to authenticated with check (
    worker_id = (select auth.uid())
    and exists (
      select 1 from public.jobs
      where jobs.id = job_applications.job_id and jobs.status = 'open'
    )
  );

drop policy if exists "viyzo_verifications_read_own" on public.worker_verifications;
create policy "viyzo_verifications_read_own" on public.worker_verifications
  for select to authenticated using (worker_id = (select auth.uid()));

drop policy if exists "viyzo_payouts_read_own" on public.worker_payouts;
create policy "viyzo_payouts_read_own" on public.worker_payouts
  for select to authenticated using (worker_id = (select auth.uid()));

grant select, insert on public.job_applications to authenticated;
grant select on public.worker_verifications to authenticated;
grant select on public.worker_payouts to authenticated;

create index if not exists job_applications_worker_id_idx on public.job_applications(worker_id);
create index if not exists job_applications_job_id_idx on public.job_applications(job_id);
create index if not exists worker_verifications_worker_id_idx on public.worker_verifications(worker_id);
create index if not exists worker_payouts_worker_id_idx on public.worker_payouts(worker_id);

revoke execute on function public.rls_auto_enable() from public, anon, authenticated;
