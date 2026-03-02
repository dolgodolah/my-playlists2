import { apiRequest } from "@/lib/api";

export interface PlaylistItem {
  playlistId: string;
  title: string;
  description: string;
  updatedDate: string;
  visibility: boolean;
  author: string;
  songCount: number;
  isEditable: boolean;
}

export interface CreatePlaylistPayload {
  title: string;
  description: string;
  visibility: boolean;
}

export interface UpdatePlaylistPayload {
  title: string;
  description: string;
  visibility: boolean;
}

interface PlaylistsResponse {
  playlists: PlaylistItem[];
  nextPlaylistId: string | null;
}

export interface PlaylistSlice {
  playlists: PlaylistItem[];
  nextPlaylistId: string | null;
}

export async function getMyPlaylists(lastPlaylistId?: string): Promise<PlaylistSlice> {
  const response = await request<PlaylistsResponse>(buildPlaylistPath("/api/v1/playlists", lastPlaylistId), { method: "GET" });
  return {
    playlists: response.playlists ?? [],
    nextPlaylistId: response.nextPlaylistId ?? null,
  };
}

export async function getAllPlaylists(lastPlaylistId?: string): Promise<PlaylistSlice> {
  const response = await request<PlaylistsResponse>(buildPlaylistPath("/api/v1/playlists/all", lastPlaylistId), { method: "GET" });
  return {
    playlists: response.playlists ?? [],
    nextPlaylistId: response.nextPlaylistId ?? null,
  };
}

export async function getPlaylistDetail(playlistId: string): Promise<PlaylistItem> {
  return request<PlaylistItem>(`/api/v1/playlists/${playlistId}`, { method: "GET" });
}

export async function createPlaylist(payload: CreatePlaylistPayload): Promise<void> {
  return request<void>("/api/v1/playlists", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function updatePlaylist(playlistId: string, payload: UpdatePlaylistPayload): Promise<void> {
  return request<void>(`/api/v1/playlists/${playlistId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export async function deletePlaylist(playlistId: string): Promise<void> {
  return request<void>(`/api/v1/playlists/${playlistId}`, {
    method: "DELETE",
  });
}

async function request<T>(path: string, init: RequestInit): Promise<T> {
  return apiRequest<T>(path, init, "플레이리스트 요청에 실패했습니다.");
}

function buildPlaylistPath(basePath: string, lastPlaylistId?: string): string {
  const params = new URLSearchParams();
  params.set("limit", "10");
  if (lastPlaylistId) {
    params.set("lastPlaylistId", lastPlaylistId);
  }
  return `${basePath}?${params.toString()}`;
}
