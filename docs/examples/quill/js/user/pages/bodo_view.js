/**
 * 보도자료 상세보기(bodo_view) 페이지 Initialization
 * src/main/resources/templates/user1/board/bodo_view.html용 JavaScript
 *
 * SSR(Server-Side Rendering) 방식:
 * - Thymeleaf에서 게시글 상세정보를 렌더링
 * - Alpine.js에서는 언어 전환, 다크모드, 이전/다음 글 네비게이션, 첨부파일 로드 등을 처리
 */

import { initAlpineBase } from '../modules/alpine-base.js';
import { setupScrollListener } from '../modules/scroll.js';
import { setupThemeLangWatchers } from '../modules/theme-lang-watcher.js';

/**
 * 보도자료 상세보기 페이지 Alpine Data
 */
export function initBodoViewPage() {
    return {
        ...initAlpineBase(),

        // 게시글 상태
        boardCode: 'bodo',
        postId: null,

        // 첨부파일 상태
        attachedFiles: [],
        isLoadingFiles: false,

        // 이전/다음 글 네비게이션 상태
        hasPreviousPost: false,
        hasNextPost: false,
        previousPostId: null,
        nextPostId: null,
        isLoadingNavigation: false,

        /**
         * x-init에서 호출될 초기화 함수
         */
        init() {
            // 1. Scroll 리스너 설정
            setupScrollListener(this);

            // 2. Dark mode와 lang 변경 감시
            setupThemeLangWatchers(this);

            // 3. 게시글 ID 읽기 (Thymeleaf에서 제공)
            this.parsePostData();

            // 4. 첨부파일 로드
            this.loadAttachedFiles();

            // 5. 이전/다음 글 정보 로드
            this.loadNavigationPosts();
        },

        /**
         * 게시글 데이터 파싱 (DOM에서 읽기)
         */
        parsePostData() {
            // meta 태그에서 post ID 읽기
            const postIdMeta = document.querySelector('meta[name="post-id"]');
            if (postIdMeta) {
                this.postId = parseInt(postIdMeta.getAttribute('content'), 10);
            }
        },

        /**
         * 첨부파일 로드
         * GET /api/files/post/bodo/{postId}
         */
        loadAttachedFiles() {
            if (!this.postId) return;

            this.isLoadingFiles = true;

            fetch(`/api/files/post/bodo/${this.postId}`)
                .then(response => {
                    if (!response.ok) {
                        console.warn('Failed to load files:', response.status);
                        return { data: [] };
                    }
                    return response.json();
                })
                .then(data => {
                    // API response structure: { success: true, data: [...files] }
                    this.attachedFiles = Array.isArray(data.data) ? data.data : [];
                    console.log(`Loaded ${this.attachedFiles.length} files`);
                })
                .catch(error => {
                    console.error('Error loading attached files:', error);
                    this.attachedFiles = [];
                })
                .finally(() => {
                    this.isLoadingFiles = false;
                });
        },

        /**
         * 이전/다음 글 정보 로드
         * GET /user1/api/board/bodo/{id}/navigation
         */
        loadNavigationPosts() {
            if (!this.postId) return;

            this.isLoadingNavigation = true;

            fetch(`/user1/api/board/bodo/${this.postId}/navigation`)
                .then(response => {
                    if (!response.ok) {
                        console.warn('Failed to load navigation:', response.status);
                        return null;
                    }
                    return response.json();
                })
                .then(data => {
                    if (!data) {
                        this.hasPreviousPost = false;
                        this.hasNextPost = false;
                        return;
                    }

                    if (data.previousPost) {
                        this.hasPreviousPost = true;
                        this.previousPostId = data.previousPost.id;
                    }

                    if (data.nextPost) {
                        this.hasNextPost = true;
                        this.nextPostId = data.nextPost.id;
                    }
                })
                .catch(error => {
                    console.error('Error loading navigation posts:', error);
                    this.hasPreviousPost = false;
                    this.hasNextPost = false;
                })
                .finally(() => {
                    this.isLoadingNavigation = false;
                });
        },

        /**
         * 이전 글로 이동
         */
        previousPost() {
            if (this.previousPostId) {
                window.location.href = `/user1/board/bodo/${this.previousPostId}`;
            }
        },

        /**
         * 다음 글로 이동
         */
        nextPost() {
            if (this.nextPostId) {
                window.location.href = `/user1/board/bodo/${this.nextPostId}`;
            }
        },

        /**
         * 파일 크기 포맷팅 (bytes -> KB, MB)
         */
        formatFileSize(bytes) {
            if (!bytes) return '0 B';

            const units = ['B', 'KB', 'MB', 'GB'];
            const size = Math.abs(bytes);
            let unitIndex = 0;

            let formattedSize = size;
            while (formattedSize >= 1024 && unitIndex < units.length - 1) {
                formattedSize /= 1024;
                unitIndex++;
            }

            return formattedSize.toFixed(1) + ' ' + units[unitIndex];
        },

        /**
         * 목록으로 돌아가기
         */
        backToList() {
            window.location.href = '/user1/board/bodo';
        }
    };
}
