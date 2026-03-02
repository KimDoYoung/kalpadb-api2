/**
 * 공시사항(gosi) 페이지 Initialization
 * src/main/resources/templates/user1/board/gosi.html용 JavaScript
 *
 * SSR(Server-Side Rendering) 방식:
 * - Thymeleaf에서 게시글 목록을 렌더링
 * - Alpine.js에서는 검색, 페이징, 정렬 등의 클라이언트 상호작용 처리
 * - 페이징은 서버에서 처리 (GET 파라미터로 page 전달)
 */

import { initAlpineBase } from '../modules/alpine-base.js';
import { setupScrollListener } from '../modules/scroll.js';
import { setupThemeLangWatchers } from '../modules/theme-lang-watcher.js';

/**
 * 공시사항 페이지 Alpine Data
 */
export function initGosiPage() {
    return {
        ...initAlpineBase(),

        // Board 상태
        boardCode: 'gosi',
        submenuOpen: false,

        // 페이징 상태
        currentPage: 0,
        pageSize: 10,
        totalPages: 0,
        totalElements: 0,

        // 검색 상태
        searchType: 'title', // 'title' or 'content'
        searchKeyword: '',
        isSearching: false,

        // 게시글 목록 (Thymeleaf에서 렌더링됨)
        posts: [],
        isLoading: false,

        /**
         * x-init에서 호출될 초기화 함수
         */
        init() {
            // 1. Scroll 리스너 설정
            setupScrollListener(this);

            // 2. Dark mode와 lang 변경 감시
            setupThemeLangWatchers(this);

            // 3. URL 파라미터 읽기 (페이징, 검색)
            this.parseUrlParams();

            // 4. 게시글 목록 DOM에서 로드 (Thymeleaf 렌더링된 데이터)
            this.loadPostsFromDOM();
        },

        /**
         * URL 파라미터 파싱 (페이징 정보 읽기)
         */
        parseUrlParams() {
            const params = new URLSearchParams(window.location.search);

            const page = params.get('page');
            const keyword = params.get('keyword');
            const searchType = params.get('searchType');

            if (page !== null) {
                this.currentPage = parseInt(page, 10);
            }

            if (keyword !== null) {
                this.searchKeyword = keyword;
                this.isSearching = true;
            }

            if (searchType !== null && ['title', 'content'].includes(searchType)) {
                this.searchType = searchType;
            }
        },

        /**
         * Thymeleaf에서 렌더링된 게시글 목록을 DOM에서 읽기
         */
        loadPostsFromDOM() {
            // Thymeleaf에서 data-* 속성으로 렌더링된 게시글 정보 읽기
            const postElements = document.querySelectorAll('[data-post-id]');

            this.posts = Array.from(postElements).map(el => ({
                id: el.getAttribute('data-post-id'),
                titleKor: el.getAttribute('data-title-kor'),
                titleEng: el.getAttribute('data-title-eng'),
                baseYmd: el.getAttribute('data-base-ymd'),
                viewCount: el.getAttribute('data-view-count'),
                authorKor: el.getAttribute('data-author-kor') || '관리자',
            }));

            // 페이징 정보 읽기
            const pagination = document.querySelector('[data-pagination]');
            if (pagination) {
                this.currentPage = parseInt(pagination.getAttribute('data-current-page') || 0, 10);
                this.totalPages = parseInt(pagination.getAttribute('data-total-pages') || 0, 10);
                this.totalElements = parseInt(pagination.getAttribute('data-total-elements') || 0, 10);
            }
        },

        /**
         * 검색 수행
         */
        onSearch(e) {
            if (e) {
                e.preventDefault();
            }

            // 검색 키워드가 비어있으면 작동 안함
            if (!this.searchKeyword.trim()) {
                return;
            }

            // 검색 페이지 URL 구성
            const params = new URLSearchParams({
                page: 0, // 검색시 1페이지로
                keyword: this.searchKeyword,
                searchType: this.searchType
            });

            window.location.href = `/user1/board/gosi?${params.toString()}`;
        },

        /**
         * 검색 초기화
         */
        clearSearch() {
            this.searchKeyword = '';
            this.searchType = 'title';
            window.location.href = `/user1/board/gosi`;
        },

        /**
         * 특정 페이지로 이동
         */
        goToPage(pageNum) {
            if (pageNum < 0 || pageNum >= this.totalPages) {
                return;
            }

            const params = new URLSearchParams({ page: pageNum });

            if (this.isSearching && this.searchKeyword) {
                params.append('keyword', this.searchKeyword);
                params.append('searchType', this.searchType);
            }

            window.location.href = `/user1/board/gosi?${params.toString()}`;
        },

        /**
         * 다음 페이지
         */
        nextPage() {
            this.goToPage(this.currentPage + 1);
        },

        /**
         * 이전 페이지
         */
        prevPage() {
            this.goToPage(this.currentPage - 1);
        },

        /**
         * 게시글 상세보기로 이동
         */
        viewPost(postId) {
            window.location.href = `/user1/board/gosi/${postId}`;
        },

        /**
         * 날짜 포맷팅 (YYYYMMDD -> YYYY.MM.DD)
         */
        formatDate(baseYmd) {
            if (!baseYmd || baseYmd.length !== 8) {
                return baseYmd;
            }
            return `${baseYmd.substring(0, 4)}.${baseYmd.substring(4, 6)}.${baseYmd.substring(6, 8)}`;
        },

        /**
         * 최신글 표시 (생성일로부터 7일 이내)
         */
        isRecentPost(baseYmd) {
            if (!baseYmd) return false;

            const postDate = new Date(
                parseInt(baseYmd.substring(0, 4), 10),
                parseInt(baseYmd.substring(4, 6), 10) - 1,
                parseInt(baseYmd.substring(6, 8), 10)
            );

            const today = new Date();
            const diffTime = Math.abs(today - postDate);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

            return diffDays <= 7;
        },

        /**
         * 페이지 번호 배열 생성 (페이지네이션 UI용)
         */
        getPageNumbers() {
            if (this.totalPages <= 1) {
                return [];
            }

            const pages = [];
            const startPage = Math.max(0, this.currentPage - 2);
            const endPage = Math.min(this.totalPages - 1, this.currentPage + 2);

            for (let i = startPage; i <= endPage; i++) {
                pages.push(i);
            }

            return pages;
        },

        /**
         * 검색 타입 라벨 가져오기
         */
        getSearchTypeLabel() {
            return this.searchType === 'title'
                ? (this.lang === 'ko' ? '제목' : 'Title')
                : (this.lang === 'ko' ? '내용' : 'Content');
        }
    };
}
