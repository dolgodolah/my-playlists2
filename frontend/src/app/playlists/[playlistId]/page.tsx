"use client";

import { FormEvent, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { isUnauthorizedError } from "@/lib/api";
import { getPlaylistDetail, PlaylistItem } from "@/lib/playlists";
import { addSong, deleteSong, getPlaylistSongs, searchYoutubeVideos, SongItem, updateSong, YoutubeVideoItem } from "@/lib/songs";
import { logoutSession } from "@/lib/auth";

let youtubeIframeApiPromise: Promise<any> | null = null;

function loadYoutubeIframeApi() {
  if (youtubeIframeApiPromise) {
    return youtubeIframeApiPromise;
  }

  youtubeIframeApiPromise = new Promise((resolve) => {
    const ytWindow = window as Window & {
      YT?: any;
      onYouTubeIframeAPIReady?: () => void;
    };

    if (ytWindow.YT?.Player) {
      resolve(ytWindow.YT);
      return;
    }

    const existingScript = document.querySelector('script[src="https://www.youtube.com/iframe_api"]');
    if (!existingScript) {
      const script = document.createElement("script");
      script.src = "https://www.youtube.com/iframe_api";
      document.head.appendChild(script);
    }

    ytWindow.onYouTubeIframeAPIReady = () => {
      resolve(ytWindow.YT);
    };
  });

  return youtubeIframeApiPromise;
}

export default function PlaylistDetailPage() {
  const router = useRouter();
  const params = useParams<{ playlistId: string }>();
  const playlistId = params.playlistId;
  const [playlist, setPlaylist] = useState<PlaylistItem | null>(null);
  const [songs, setSongs] = useState<SongItem[]>([]);
  const [nextSongId, setNextSongId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [pageErrorMessage, setPageErrorMessage] = useState<string | null>(null);
  const [addErrorMessage, setAddErrorMessage] = useState<string | null>(null);
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSearchingYoutube, setIsSearchingYoutube] = useState(false);
  const [isLoadingMoreSongs, setIsLoadingMoreSongs] = useState(false);
  const [isSongActionLoading, setIsSongActionLoading] = useState(false);
  const [youtubeKeyword, setYoutubeKeyword] = useState("");
  const [youtubeResults, setYoutubeResults] = useState<YoutubeVideoItem[]>([]);
  const [selectedYoutubeVideoId, setSelectedYoutubeVideoId] = useState<string | null>(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [playingSongId, setPlayingSongId] = useState<string | null>(null);
  const [editingSongId, setEditingSongId] = useState<string | null>(null);
  const [editTitle, setEditTitle] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const playerContainerRef = useRef<HTMLDivElement | null>(null);
  const youtubePlayerRef = useRef<any>(null);

  const resetYoutubeSearchState = () => {
    setYoutubeKeyword("");
    setYoutubeResults([]);
    setSelectedYoutubeVideoId(null);
  };

  const loadPage = async (): Promise<SongItem[]> => {
    const [playlistResponse, songsResponse] = await Promise.all([
      getPlaylistDetail(playlistId),
      getPlaylistSongs(playlistId),
    ]);
    setPlaylist(playlistResponse);
    setSongs(songsResponse.songs);
    setNextSongId(songsResponse.nextSongId);
    return songsResponse.songs;
  };

  useEffect(() => {
    let isCancelled = false;
    setIsLoading(true);
    setPageErrorMessage(null);

    Promise.all([getPlaylistDetail(playlistId), getPlaylistSongs(playlistId)])
      .then(([playlistResponse, songsResponse]) => {
        if (!isCancelled) {
          setPlaylist(playlistResponse);
          setSongs(songsResponse.songs);
          setNextSongId(songsResponse.nextSongId);
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
          setPageErrorMessage(error.message);
        } else {
          setPageErrorMessage("플레이리스트 상세 정보를 불러오지 못했습니다.");
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
  }, [playlistId]);

  useEffect(() => {
    if (songs.length === 0) {
      setPlayingSongId(null);
      return;
    }

    setPlayingSongId((prev) => {
      if (prev && songs.some((song) => song.songId === prev)) {
        return prev;
      }
      return songs[0].songId;
    });
  }, [songs]);

  useEffect(() => {
    if (!playingSongId || !playerContainerRef.current) {
      return;
    }

    const currentSong = songs.find((song) => song.songId === playingSongId);
    if (!currentSong) {
      return;
    }

    let isDisposed = false;

    loadYoutubeIframeApi().then((YT) => {
      if (isDisposed || !playerContainerRef.current) {
        return;
      }

      if (youtubePlayerRef.current) {
        youtubePlayerRef.current.destroy();
      }

      youtubePlayerRef.current = new YT.Player(playerContainerRef.current, {
        videoId: currentSong.videoId,
        playerVars: {
          autoplay: 1,
          controls: 1,
          rel: 0,
        },
        events: {
          onStateChange: (event: { data: number }) => {
            if (event.data !== YT.PlayerState.ENDED) {
              return;
            }

            const currentIndex = songs.findIndex((song) => song.songId === currentSong.songId);
            const nextSong = currentIndex >= 0 ? songs[currentIndex + 1] : null;
            if (nextSong) {
              setPlayingSongId(nextSong.songId);
            }
          },
        },
      });
    });

    return () => {
      isDisposed = true;
      if (youtubePlayerRef.current) {
        youtubePlayerRef.current.destroy();
        youtubePlayerRef.current = null;
      }
    };
  }, [playingSongId, songs]);

  const handleAddSong = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setAddErrorMessage(null);

    if (youtubeResults.length === 0) {
      setAddErrorMessage("먼저 유튜브 검색을 통해 노래를 검색해주세요.");
      return;
    }

    if (!selectedYoutubeVideoId) {
      setAddErrorMessage("검색 결과에서 추가할 노래를 선택해주세요.");
      return;
    }

    const selectedYoutubeSong = youtubeResults.find((song) => song.videoId === selectedYoutubeVideoId);
    if (!selectedYoutubeSong) {
      setAddErrorMessage("선택한 노래를 다시 선택해주세요.");
      return;
    }

    setIsSubmitting(true);
    const wasPlaying = youtubePlayerRef.current?.getPlayerState?.() === 1;

    try {
      const resolvedTitle = (title.trim() || selectedYoutubeSong.title).slice(0, 50);
      await addSong(playlistId, {
        title: resolvedTitle,
        videoId: selectedYoutubeSong.videoId,
        description,
      });
      setTitle("");
      setDescription("");
      resetYoutubeSearchState();
      setAddErrorMessage(null);
      setIsAddOpen(false);
      const updatedSongs = await loadPage();
      if (wasPlaying && updatedSongs.length > 0) {
        setPlayingSongId(updatedSongs[0].songId);
      }
    } catch (error) {
      if (isUnauthorizedError(error)) {
        return;
      }
      if (error instanceof Error) {
        setAddErrorMessage(error.message);
      } else {
        setAddErrorMessage("노래 추가에 실패했습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSearchYoutube = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const keyword = youtubeKeyword.trim();
    if (!keyword) {
      setAddErrorMessage("검색어를 입력해주세요.");
      return;
    }

    setAddErrorMessage(null);
    setIsSearchingYoutube(true);
    try {
      const response = await searchYoutubeVideos(playlistId, keyword);
      setYoutubeResults(response.songs);
      setSelectedYoutubeVideoId(response.songs[0]?.videoId ?? null);
    } catch (error) {
      if (isUnauthorizedError(error)) {
        return;
      }
      if (error instanceof Error) {
        setAddErrorMessage(error.message);
      } else {
        setAddErrorMessage("유튜브 검색에 실패했습니다.");
      }
    } finally {
      setIsSearchingYoutube(false);
    }
  };

  const handleLogout = (event: React.MouseEvent<HTMLAnchorElement>) => {
    event.preventDefault();
    logoutSession().finally(() => {
      router.replace("/login");
    });
  };

  const handleStartEditSong = (song: SongItem) => {
    setPageErrorMessage(null);
    setEditingSongId(song.songId);
    setEditTitle(song.title);
    setEditDescription(song.description);
  };

  const handleCancelEditSong = () => {
    setEditingSongId(null);
    setEditTitle("");
    setEditDescription("");
  };

  const handleUpdateSong = async (songId: string) => {
    setPageErrorMessage(null);
    setIsSongActionLoading(true);
    try {
      await updateSong(playlistId, songId, {
        title: editTitle,
        description: editDescription,
      });
      handleCancelEditSong();
      await loadPage();
    } catch (error) {
      if (isUnauthorizedError(error)) {
        return;
      }
      if (error instanceof Error) {
        setPageErrorMessage(error.message);
      } else {
        setPageErrorMessage("수록곡 수정에 실패했습니다.");
      }
    } finally {
      setIsSongActionLoading(false);
    }
  };

  const handleDeleteSong = async (songId: string) => {
    const isConfirmed = window.confirm("정말 이 수록곡을 삭제하시겠습니까?");
    if (!isConfirmed) {
      return;
    }

    setPageErrorMessage(null);
    setIsSongActionLoading(true);
    try {
      await deleteSong(playlistId, songId);
      if (editingSongId === songId) {
        handleCancelEditSong();
      }
      await loadPage();
    } catch (error) {
      if (isUnauthorizedError(error)) {
        return;
      }
      if (error instanceof Error) {
        setPageErrorMessage(error.message);
      } else {
        setPageErrorMessage("수록곡 삭제에 실패했습니다.");
      }
    } finally {
      setIsSongActionLoading(false);
    }
  };

  const handleSongListScroll = (event: React.UIEvent<HTMLDivElement>) => {
    if (nextSongId == null || isLoadingMoreSongs) {
      return;
    }

    const target = event.currentTarget;
    const isNearBottom = target.scrollTop + target.clientHeight >= target.scrollHeight - 40;
    if (!isNearBottom) {
      return;
    }

    setIsLoadingMoreSongs(true);
    getPlaylistSongs(playlistId, nextSongId)
      .then((nextSlice) => {
        setSongs((prev) => [...prev, ...nextSlice.songs]);
        setNextSongId(nextSlice.nextSongId);
      })
      .catch((error) => {
        if (isUnauthorizedError(error)) {
          return;
        }
        if (error instanceof Error) {
          setPageErrorMessage(error.message);
        } else {
          setPageErrorMessage("수록곡을 더 불러오지 못했습니다.");
        }
      })
      .finally(() => {
        setIsLoadingMoreSongs(false);
      });
  };

  const isScrollableSongs = songs.length >= 10 || nextSongId !== null;
  const playingSong = songs.find((song) => song.songId === playingSongId) ?? null;

  return (
    <main className="page-shell">
      <section className="panel-card playlist-card">
        <h1 className="page-title">플레이리스트 상세</h1>
        <p className="page-subtitle">
          {playlist ? `${playlist.author}님의 플레이리스트` : "상세 정보를 확인 중입니다."}
        </p>

        {isLoading && <div className="playlist-placeholder">상세 정보를 불러오는 중입니다...</div>}
        {!isLoading && pageErrorMessage && <p className="field-error">{pageErrorMessage}</p>}

        {!isLoading && playlist && (
          <>
            <article className="playlist-list-item">
              <h2 className="playlist-title">{playlist.title}</h2>
              <p className="playlist-description">{playlist.description}</p>
              <div className="playlist-meta">
                <span>{playlist.visibility ? "공개" : "비공개"}</span>
                <span>{playlist.songCount}곡</span>
                <span>{playlist.updatedDate}</span>
              </div>
            </article>

            {playlist.isEditable && (
              <section className="playlist-song-add-section">
                <button
                  type="button"
                  className="primary-button playlist-create-button"
                  onClick={() => {
                    setAddErrorMessage(null);
                    setIsAddOpen((prev) => {
                      const nextOpen = !prev;
                      if (!nextOpen) {
                        resetYoutubeSearchState()
                      }
                      return nextOpen;
                    });
                  }}
                >
                  {isAddOpen ? "노래 추가 닫기" : "노래 추가"}
                </button>

                {isAddOpen && (
                  <>
                    <form className="playlist-youtube-search-form" onSubmit={handleSearchYoutube}>
                      <label className="playlist-field-label" htmlFor="youtube-search-input">
                        유튜브 검색 (*)
                      </label>
                      <div className="playlist-youtube-search-input-wrap">
                        <input
                          id="youtube-search-input"
                          className="field-input playlist-youtube-search-input"
                          placeholder="노래 제목, 가수 이름을 검색하세요"
                          value={youtubeKeyword}
                          maxLength={120}
                          onChange={(event) => setYoutubeKeyword(event.target.value)}
                        />
                        <button className="playlist-youtube-search-icon-button" type="submit" aria-label="유튜브 검색">
                          <span className="playlist-youtube-search-icon" aria-hidden="true">
                            🔍
                          </span>
                        </button>
                      </div>
                    </form>

                    {isSearchingYoutube && (
                      <div className="playlist-youtube-search-loading" aria-label="검색 중" role="status">
                        <span className="playlist-spinner" />
                      </div>
                    )}

                    {youtubeResults.length > 0 && (
                      <div className="playlist-youtube-results">
                        {youtubeResults.map((song) => (
                          <article key={song.videoId} className="playlist-youtube-result-item">
                            <img
                              className="playlist-youtube-result-thumbnail"
                              src={`https://i.ytimg.com/vi/${song.videoId}/maxresdefault.jpg`}
                              alt="youtube thumbnail"
                            />
                            <div className="playlist-youtube-result-main">
                              <p className="playlist-youtube-result-title">{song.title}</p>
                            </div>
                            <input
                              type="checkbox"
                              className="playlist-youtube-result-checkbox"
                              checked={selectedYoutubeVideoId === song.videoId}
                              onChange={() => setSelectedYoutubeVideoId(song.videoId)}
                              aria-label={`노래 선택: ${song.title}`}
                            />
                          </article>
                        ))}
                      </div>
                    )}

                    <form className="form-grid playlist-song-add-form" onSubmit={handleAddSong}>
                      <label className="playlist-field-label" htmlFor="song-title-input">
                        노래 제목 (Optional)
                      </label>
                      <input
                        id="song-title-input"
                        className="field-input"
                        placeholder="노래 제목을 입력해 주세요."
                        value={title}
                        maxLength={50}
                        onChange={(event) => setTitle(event.target.value)}
                      />
                      <p className="playlist-song-title-hint">*비워두면 선택한 유튜브 검색 결과의 제목이 사용됩니다.</p>
                      <label className="playlist-field-label" htmlFor="song-description-input">
                        노래 설명 (Optional)
                      </label>
                      <textarea
                        id="song-description-input"
                        className="field-input field-textarea"
                        placeholder="노래 설명을 입력해 주세요."
                        value={description}
                        maxLength={100}
                        onChange={(event) => setDescription(event.target.value)}
                      />
                      {addErrorMessage && <p className="field-error playlist-song-add-error">{addErrorMessage}</p>}
                      <button className="primary-button" type="submit" disabled={isSubmitting}>
                        {isSubmitting ? "추가 중..." : "수록곡 추가"}
                      </button>
                    </form>
                  </>
                )}
              </section>
            )}

            <section className="playlist-songs-section">
              <h3 className="playlist-songs-title">수록곡</h3>
              {playingSong && (
                <article className="playlist-player-card">
                  <h4 className="playlist-player-title">현재 재생</h4>
                  <p className="playlist-player-song-title">{playingSong.title}</p>
                  <div className="playlist-player-frame-wrap">
                    <div
                      ref={playerContainerRef}
                      className="playlist-player-frame"
                    />
                  </div>
                </article>
              )}
              {songs.length === 0 ? (
                <div className="playlist-placeholder">아직 추가된 노래가 없습니다.</div>
              ) : (
                <div
                  className={`playlist-list${isScrollableSongs ? " playlist-list-scrollable" : ""}`}
                  onScroll={handleSongListScroll}
                >
                  {songs.map((song) => (
                    <article
                      key={song.songId}
                      className={`playlist-list-item${editingSongId !== song.songId ? " playlist-list-item-clickable" : ""}`}
                      role={editingSongId !== song.songId ? "button" : undefined}
                      tabIndex={editingSongId !== song.songId ? 0 : undefined}
                      onClick={editingSongId !== song.songId ? () => setPlayingSongId(song.songId) : undefined}
                      onKeyDown={
                        editingSongId !== song.songId
                          ? (event) => {
                              if (event.key === "Enter" || event.key === " ") {
                                event.preventDefault();
                                setPlayingSongId(song.songId);
                              }
                            }
                          : undefined
                      }
                    >
                      <img
                        className="playlist-song-thumbnail"
                        src={`https://i.ytimg.com/vi/${song.videoId}/maxresdefault.jpg`}
                        alt="youtube thumbnail"
                      />
                      <div className="playlist-song-content">
                        {editingSongId === song.songId ? (
                          <div className="playlist-song-edit-form">
                            <input
                              className="field-input"
                              value={editTitle}
                              maxLength={50}
                              onChange={(event) => setEditTitle(event.target.value)}
                            />
                            <textarea
                              className="field-input field-textarea"
                              placeholder="노래 설명을 입력해 주세요."
                              value={editDescription}
                              maxLength={100}
                              onChange={(event) => setEditDescription(event.target.value)}
                            />
                            <div className="playlist-song-actions">
                              <button
                                className="primary-button secondary-button playlist-item-action-button"
                                type="button"
                                onClick={(event) => {
                                  event.stopPropagation();
                                  handleCancelEditSong();
                                }}
                                disabled={isSongActionLoading}
                              >
                                취소
                              </button>
                              <button
                                className="primary-button playlist-item-action-button"
                                type="button"
                                onClick={(event) => {
                                  event.stopPropagation();
                                  handleUpdateSong(song.songId);
                                }}
                                disabled={isSongActionLoading}
                              >
                                {isSongActionLoading ? "저장 중..." : "저장"}
                              </button>
                            </div>
                          </div>
                        ) : (
                          <>
                            <h4 className="playlist-title">{song.title}</h4>
                            <p className="playlist-description playlist-song-description">{song.description}</p>
                            <div className="playlist-meta">
                              <span>{song.updatedDate}</span>
                            </div>
                          </>
                        )}
                      </div>
                      {editingSongId !== song.songId && (
                        <span className="playlist-item-open-icon playlist-song-play-indicator" aria-hidden="true" />
                      )}
                      <div className="playlist-song-item-actions">
                        {playlist.isEditable && editingSongId !== song.songId && (
                          <>
                            <button
                              className="primary-button secondary-button playlist-item-action-button icon-action-button icon-action-edit"
                              type="button"
                              aria-label="수록곡 수정"
                              title="수록곡 수정"
                              onClick={(event) => {
                                event.stopPropagation();
                                handleStartEditSong(song);
                              }}
                            />
                            <button
                              className="primary-button playlist-item-action-button playlist-item-delete-button icon-action-button icon-action-delete"
                              type="button"
                              aria-label="수록곡 삭제"
                              title="수록곡 삭제"
                              onClick={(event) => {
                                event.stopPropagation();
                                handleDeleteSong(song.songId);
                              }}
                              disabled={isSongActionLoading}
                            />
                          </>
                        )}
                      </div>
                    </article>
                  ))}
                  {isLoadingMoreSongs && <div className="playlist-placeholder">수록곡을 더 불러오는 중입니다...</div>}
                </div>
              )}
            </section>

            <div className="playlist-footer-actions account-links">
              <Link className="account-link" href="/">
                플레이리스트로 이동
              </Link>
              <Link className="account-link" href="/me">
                내 정보
              </Link>
              <a className="account-link" href="/login" onClick={handleLogout}>
                로그아웃
              </a>
            </div>
          </>
        )}
      </section>
    </main>
  );
}
