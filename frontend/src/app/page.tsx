"use client";

import { MouseEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { logoutSession } from "@/lib/auth";
import { deletePlaylist, getAllPlaylists, getMyPlaylists, PlaylistItem, updatePlaylist } from "@/lib/playlists";
import { isUnauthorizedError } from "@/lib/api";

type CategoryType = "my" | "all";
type SortType = "latest" | "songCount" | "title";

export default function HomePage() {
  const router = useRouter();
  const [nickname, setNickname] = useState<string | null>(null);
  const [playlists, setPlaylists] = useState<PlaylistItem[]>([]);
  const [category, setCategory] = useState<CategoryType>("my");
  const [keyword, setKeyword] = useState("");
  const [sort, setSort] = useState<SortType>("latest");
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [editingPlaylistId, setEditingPlaylistId] = useState<string | null>(null);
  const [editTitle, setEditTitle] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [editVisibility, setEditVisibility] = useState<"true" | "false">("true");
  const [isUpdating, setIsUpdating] = useState(false);
  const [nextPlaylistId, setNextPlaylistId] = useState<string | null>(null);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  useEffect(() => {
    let isCancelled = false;
    setIsLoading(true);
    setErrorMessage(null);

    const getPlaylists = category === "my" ? getMyPlaylists : getAllPlaylists;

    getPlaylists()
      .then((slice) => {
        if (isCancelled) {
          return;
        }

        setPlaylists(slice.playlists);
        setNextPlaylistId(slice.nextPlaylistId);
        if (category === "my") {
          setNickname(slice.playlists[0]?.author ?? null);
        } else {
          setNickname(null);
        }
      })
      .catch((error) => {
        if (isCancelled) {
          return;
        }

        if (isUnauthorizedError(error)) {
          return;
        }
        if (error instanceof Error) {
          setErrorMessage(error.message);
        } else {
          setErrorMessage("플레이리스트를 불러오지 못했습니다.");
        }
      })
      .finally(() => {
        if (!isCancelled) {
          setIsLoading(false);
        }
      });

    return () => {
      isCancelled = true;
    };
  }, [category]);

  const reloadPlaylists = async () => {
    const getPlaylists = category === "my" ? getMyPlaylists : getAllPlaylists;
    const slice = await getPlaylists();
    setPlaylists(slice.playlists);
    setNextPlaylistId(slice.nextPlaylistId);
    if (category === "my") {
      setNickname(slice.playlists[0]?.author ?? null);
    } else {
      setNickname(null);
    }
  };

  const visiblePlaylists = useMemo(() => {
    const filtered = playlists.filter((playlist) => {
      const value = keyword.trim().toLowerCase();
      if (!value) {
        return true;
      }

      return (
        playlist.title.toLowerCase().includes(value) ||
        playlist.description.toLowerCase().includes(value) ||
        playlist.author.toLowerCase().includes(value)
      );
    });

    return [...filtered].sort((a, b) => {
      if (sort === "title") {
        return a.title.localeCompare(b.title, "ko");
      }

      if (sort === "songCount") {
        return b.songCount - a.songCount;
      }

      return b.updatedDate.localeCompare(a.updatedDate);
    });
  }, [playlists, keyword, sort]);

  const handleLogout = (event: MouseEvent<HTMLAnchorElement>) => {
    event.preventDefault();
    logoutSession().finally(() => {
      router.replace("/login");
    });
  };

  const openPlaylistDetail = (playlistId: string) => {
    router.push(`/playlists/${playlistId}`);
  };

  const startEdit = (playlist: PlaylistItem) => {
    setEditingPlaylistId(playlist.playlistId);
    setEditTitle(playlist.title);
    setEditDescription(playlist.description);
    setEditVisibility(playlist.visibility ? "true" : "false");
    setErrorMessage(null);
  };

  const cancelEdit = () => {
    setEditingPlaylistId(null);
    setEditTitle("");
    setEditDescription("");
    setEditVisibility("true");
  };

  const handleUpdatePlaylist = async (playlistId: string) => {
    setErrorMessage(null);
    setIsUpdating(true);
    try {
      await updatePlaylist(playlistId, {
        title: editTitle,
        description: editDescription,
        visibility: editVisibility === "true",
      });
      cancelEdit();
      await reloadPlaylists();
    } catch (error) {
      if (isUnauthorizedError(error)) {
        return;
      }
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("플레이리스트 수정 중 오류가 발생했습니다.");
      }
    } finally {
      setIsUpdating(false);
    }
  };

  const handleDeletePlaylist = async (playlistId: string) => {
    const isConfirmed = window.confirm("정말 이 플레이리스트를 삭제하시겠습니까?");
    if (!isConfirmed) {
      return;
    }

    setErrorMessage(null);
    try {
      await deletePlaylist(playlistId);
      if (editingPlaylistId === playlistId) {
        cancelEdit();
      }
      await reloadPlaylists();
    } catch (error) {
      if (isUnauthorizedError(error)) {
        return;
      }
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("플레이리스트 삭제 중 오류가 발생했습니다.");
      }
    }
  };

  const handlePlaylistListScroll = (event: React.UIEvent<HTMLDivElement>) => {
    if (isLoading || isLoadingMore || nextPlaylistId == null) {
      return;
    }

    const target = event.currentTarget;
    const isNearBottom = target.scrollTop + target.clientHeight >= target.scrollHeight - 40;
    if (!isNearBottom) {
      return;
    }

    const getPlaylists = category === "my" ? getMyPlaylists : getAllPlaylists;
    setIsLoadingMore(true);
    getPlaylists(nextPlaylistId)
      .then((slice) => {
        setPlaylists((prev) => [...prev, ...slice.playlists]);
        setNextPlaylistId(slice.nextPlaylistId);
      })
      .catch((error) => {
        if (isUnauthorizedError(error)) {
          return;
        }
        if (error instanceof Error) {
          setErrorMessage(error.message);
        } else {
          setErrorMessage("플레이리스트를 더 불러오지 못했습니다.");
        }
      })
      .finally(() => {
        setIsLoadingMore(false);
      });
  };

  const isScrollableList = visiblePlaylists.length >= 10 || nextPlaylistId !== null;

  return (
    <main className="page-shell">
      <section className="panel-card playlist-card">
        <h1 className="page-title">내플리스</h1>
        <p className="page-subtitle">
          {category === "all"
            ? "전체 플레이리스트를 둘러보세요."
            : nickname
              ? `${nickname}님의 플레이리스트`
              : "사용자 정보를 확인 중입니다."}
        </p>

        <section className="playlist-controls">
          <div className="playlist-create-action">
            <a className="primary-button playlist-create-button" href="/playlists/add">
              플레이리스트 추가
            </a>
          </div>

          <div className="category-tabs" role="tablist" aria-label="플레이리스트 카테고리">
            <button
              className={`category-tab${category === "my" ? " is-active" : ""}`}
              type="button"
              onClick={() => setCategory("my")}
            >
              내 플레이리스트
            </button>
            <button
              className={`category-tab${category === "all" ? " is-active" : ""}`}
              type="button"
              onClick={() => setCategory("all")}
            >
              전체 플레이리스트
            </button>
          </div>

          <div className="playlist-filters">
            <input
              className="field-input"
              placeholder="제목/설명/작성자 검색"
              value={keyword}
              onChange={(event) => setKeyword(event.target.value)}
            />
            <select className="playlist-select" value={sort} onChange={(event) => setSort(event.target.value as SortType)}>
              <option value="latest">최신순</option>
              <option value="songCount">곡 많은 순</option>
              <option value="title">제목순</option>
            </select>
          </div>
        </section>

        {isLoading && <div className="playlist-placeholder">플레이리스트를 불러오는 중입니다...</div>}

        {!isLoading && errorMessage && <p className="field-error">{errorMessage}</p>}

        {!isLoading && !errorMessage && visiblePlaylists.length === 0 && (
          <div className="playlist-placeholder">조건에 맞는 플레이리스트가 없습니다.</div>
        )}

        {!isLoading && !errorMessage && visiblePlaylists.length > 0 && (
          <div className={`playlist-list${isScrollableList ? " playlist-list-scrollable" : ""}`} onScroll={handlePlaylistListScroll}>
            {visiblePlaylists.map((playlist) => (
              <article
                key={playlist.playlistId}
                className={`playlist-list-item${editingPlaylistId !== playlist.playlistId ? " playlist-list-item-clickable" : ""}`}
                role={editingPlaylistId !== playlist.playlistId ? "button" : undefined}
                tabIndex={editingPlaylistId !== playlist.playlistId ? 0 : undefined}
                onClick={
                  editingPlaylistId !== playlist.playlistId
                    ? () => openPlaylistDetail(playlist.playlistId)
                    : undefined
                }
                onKeyDown={
                  editingPlaylistId !== playlist.playlistId
                    ? (event) => {
                        if (event.key === "Enter" || event.key === " ") {
                          event.preventDefault();
                          openPlaylistDetail(playlist.playlistId);
                        }
                      }
                    : undefined
                }
              >
                {editingPlaylistId === playlist.playlistId ? (
                  <div className="playlist-edit-form">
                    <input
                      className="field-input"
                      value={editTitle}
                      minLength={2}
                      maxLength={50}
                      onChange={(event) => setEditTitle(event.target.value)}
                    />
                    <textarea
                      className="field-input field-textarea"
                      placeholder="플레이리스트 소개를 입력해 주세요."
                      value={editDescription}
                      maxLength={100}
                      onChange={(event) => setEditDescription(event.target.value)}
                    />
                    <div className="visibility-group">
                      <label className="visibility-option">
                        <input
                          type="radio"
                          value="true"
                          checked={editVisibility === "true"}
                          onChange={(event) => setEditVisibility(event.target.value as "true" | "false")}
                        />
                        공개
                      </label>
                      <label className="visibility-option">
                        <input
                          type="radio"
                          value="false"
                          checked={editVisibility === "false"}
                          onChange={(event) => setEditVisibility(event.target.value as "true" | "false")}
                        />
                        비공개
                      </label>
                    </div>
                    <div className="playlist-item-actions">
                      <button
                        type="button"
                        className="primary-button secondary-button playlist-item-action-button"
                        onClick={cancelEdit}
                      >
                        취소
                      </button>
                      <button
                        type="button"
                        className="primary-button playlist-item-action-button"
                        disabled={isUpdating}
                        onClick={() => handleUpdatePlaylist(playlist.playlistId)}
                      >
                        {isUpdating ? "저장 중..." : "저장"}
                      </button>
                    </div>
                  </div>
                ) : (
                  <>
                    <div className="playlist-item-layout">
                      <div className="playlist-item-body">
                        <div className="playlist-item-main">
                          <h2 className="playlist-title">{playlist.title}</h2>
                          <p className="playlist-description playlist-item-description">{playlist.description}</p>
                          <div className="playlist-meta">
                            <span>{playlist.author}</span>
                            <span>{playlist.songCount}곡</span>
                            <span>{playlist.updatedDate}</span>
                          </div>
                        </div>
                      </div>
                      <span className="playlist-item-open-icon playlist-item-play-indicator" aria-hidden="true" />
                      {playlist.isEditable && (
                        <div className="playlist-item-actions playlist-item-actions-right">
                          <button
                            type="button"
                            className="primary-button secondary-button playlist-item-action-button icon-action-button icon-action-edit"
                            aria-label="플레이리스트 수정"
                            title="플레이리스트 수정"
                            onClick={(event) => {
                              event.stopPropagation();
                              startEdit(playlist);
                            }}
                          />
                          <button
                            type="button"
                            className="primary-button playlist-item-action-button playlist-item-delete-button icon-action-button icon-action-delete"
                            aria-label="플레이리스트 삭제"
                            title="플레이리스트 삭제"
                            onClick={(event) => {
                              event.stopPropagation();
                              handleDeletePlaylist(playlist.playlistId);
                            }}
                          />
                        </div>
                      )}
                    </div>
                  </>
                )}
              </article>
            ))}
            {isLoadingMore && <div className="playlist-placeholder">추가 플레이리스트를 불러오는 중입니다...</div>}
          </div>
        )}

        <div className="playlist-footer-actions account-links">
          <a className="account-link" href="/me">
            내 정보
          </a>
          <a className="account-link" href="/login" onClick={handleLogout}>
            로그아웃
          </a>
        </div>
      </section>
    </main>
  );
}
