-- KokoroFy accounts, synced settings/history and playlists.
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  avatar_url text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create table if not exists public.user_settings (
  user_id uuid primary key references auth.users(id) on delete cascade,
  settings jsonb not null default '{}'::jsonb,
  updated_at timestamptz not null default now()
);
create table if not exists public.listening_history (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  track_id uuid references public.tracks(id) on delete set null,
  played_at timestamptz not null default now(),
  seconds_played integer not null default 0,
  completed boolean not null default false
);
alter table public.playlist_songs add column if not exists track_id uuid references public.tracks(id) on delete cascade;
create index if not exists idx_playlist_songs_track_id on public.playlist_songs(track_id);
create index if not exists idx_listening_history_user_played_at on public.listening_history(user_id, played_at desc);
create index if not exists idx_listening_history_track on public.listening_history(track_id);
alter table public.profiles enable row level security;
alter table public.user_settings enable row level security;
alter table public.listening_history enable row level security;
drop policy if exists "kokorofy favorites access" on public.favorites;
drop policy if exists "kokorofy playlists access" on public.playlists;
drop policy if exists "kokorofy playlist_songs access" on public.playlist_songs;
create policy "Users manage their own favorites" on public.favorites for all to authenticated using ((select auth.uid())::text = user_id) with check ((select auth.uid())::text = user_id);
create policy "Users manage their own playlists" on public.playlists for all to authenticated using ((select auth.uid())::text = user_id) with check ((select auth.uid())::text = user_id);
create policy "Users manage songs in their playlists" on public.playlist_songs for all to authenticated using (exists (select 1 from public.playlists p where p.id = playlist_id and p.user_id = (select auth.uid())::text)) with check (exists (select 1 from public.playlists p where p.id = playlist_id and p.user_id = (select auth.uid())::text));
create policy "Users read own profile" on public.profiles for select to authenticated using ((select auth.uid()) = id);
create policy "Users insert own profile" on public.profiles for insert to authenticated with check ((select auth.uid()) = id);
create policy "Users update own profile" on public.profiles for update to authenticated using ((select auth.uid()) = id) with check ((select auth.uid()) = id);
create policy "Users manage own settings" on public.user_settings for all to authenticated using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);
create policy "Users manage own listening history" on public.listening_history for all to authenticated using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);
create unique index if not exists uq_playlist_track on public.playlist_songs(playlist_id, track_id) where track_id is not null;
