import { apiRequest } from "@/lib/api";

export interface SongItem {
  songId: string;
  title: string;
  videoId: string;
  description: string;
  createdDate: string;
  updatedDate: string;
}

export interface SongsSlice {
  songs: SongItem[];
  nextSongId: string | null;
}

export interface AddSongPayload {
  title: string;
  videoId: string;
  description?: string;
}

export interface UpdateSongPayload {
  title: string;
  description?: string;
}

export interface YoutubeVideoItem {
  title: string;
  videoId: string;
}

export interface YoutubeVideosResponse {
  songs: YoutubeVideoItem[];
}

export async function getPlaylistSongs(playlistId: string, lastSongId?: string): Promise<SongsSlice> {
  const params = new URLSearchParams();
  params.set("limit", "10");
  if (lastSongId) {
    params.set("lastSongId", lastSongId);
  }
  return request<SongsSlice>(`/api/v1/playlists/${playlistId}/songs?${params.toString()}`, { method: "GET" });
}

export async function addSong(playlistId: string, payload: AddSongPayload): Promise<void> {
  return request<void>(`/api/v1/playlists/${playlistId}/songs`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function updateSong(playlistId: string, songId: string, payload: UpdateSongPayload): Promise<void> {
  return request<void>(`/api/v1/playlists/${playlistId}/songs/${songId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export async function deleteSong(playlistId: string, songId: string): Promise<void> {
  return request<void>(`/api/v1/playlists/${playlistId}/songs/${songId}`, {
    method: "DELETE",
  });
}

export async function searchYoutubeVideos(playlistId: string, keyword: string): Promise<YoutubeVideosResponse> {
  const params = new URLSearchParams();
  params.set("q", keyword);
  return request<YoutubeVideosResponse>(`/api/v1/playlists/${playlistId}/songs/search?${params.toString()}`, {
    method: "GET",
  });
}

async function request<T>(path: string, init: RequestInit): Promise<T> {
  return apiRequest<T>(path, init, "노래 요청에 실패했습니다.");
}
