/**
 * Quill 에디터 공통 설정 및 초기화
 * 모든 게시판에서 공통으로 사용하는 Quill 에디터 관련 기능
 */

// Quill 인스턴스를 전역으로 관리
let globalQuillInstance = null;

// 게시판 코드를 전역으로 관리 (각 페이지에서 설정)
let globalBoardCode = null;

/**
 * Quill 에디터 초기화
 * @param {HTMLElement} editorElement - 에디터가 마운트될 HTML 요소
 * @param {Object} options - 설정 옵션
 * @param {Function} onTextChange - 텍스트 변경 콜백 함수
 * @returns {Object} Quill 인스턴스
 */
function initQuillEditor(editorElement, options = {}, onTextChange = null) {
    // Table Better 모듈 등록
    if (typeof QuillTableBetter !== 'undefined') {
        try {
            Quill.register({
                'modules/table-better': QuillTableBetter
            }, true);
            console.log('Quill Table Better module registered successfully');
        } catch (error) {
            console.warn('Failed to register Quill Table Better module:', error);
        }
    } else {
        console.warn('QuillTableBetter not found. Make sure to include the script.');
    }

    // 이미지 리사이즈 모듈 등록 (CDN에서 로드된 경우에만)
    if (typeof QuillResizeImage !== 'undefined') {
        try {
            Quill.register('modules/imageResize', QuillResizeImage);
            console.log('Quill Image Resize module registered successfully');
        } catch (error) {
            console.warn('Failed to register Quill Image Resize module:', error);
        }
    } else {
        console.warn('QuillResizeImage not found. Make sure to include the CDN script.');
    }

    const defaultOptions = {
        theme: 'snow',
        placeholder: '여기에 내용을 입력하세요... (이미지는 Ctrl+V로 붙여넣기, 테이블 버튼으로 표 삽입 가능)',
        modules: {
            toolbar: {
                container: [
                    ['bold', 'italic', 'underline','strike'],
                    [{'header':[1,2,false]}],
                    [{ 'header': 1 }, { 'header': 2 }],               // custom button values
                    [{ 'list': 'ordered' }, { 'list': 'bullet' }],
                    [{ 'indent': '-1' }, { 'indent': '+1' }],          // outdent/indent
                    [{ 'align': [] }],
                    [{ 'color': [] }, { 'background': [] }],          // dropdown with defaults from theme
                    ['link', 'image'],
                    ['table-better'],  // Table Better 버튼 추가
                    ['clean']
                ],
                handlers: {
                    image: () => customImageHandler()
                }
            },
            // 기본 테이블 모듈 비활성화
            table: false,
            // Table Better 모듈 설정
            'table-better': typeof QuillTableBetter !== 'undefined' ? {
                language: 'ko_KR',
                menus: ['column', 'row', 'merge', 'table', 'cell', 'wrap', 'copy', 'delete'],
                toolbarTable: true
            } : undefined,
            // 이미지 리사이즈 모듈 설정
            imageResize: typeof QuillResizeImage !== 'undefined' ? {
                displaySize: true,      // 이미지 크기 표시
                modules: ['Resize', 'DisplaySize', 'Toolbar']  // 사용할 기능들
            } : undefined,
            // 키보드 바인딩 (테이블 관련)
            keyboard: typeof QuillTableBetter !== 'undefined' ? {
                bindings: QuillTableBetter.keyboardBindings
            } : undefined
        }
    };

    // 옵션 병합
    const finalOptions = { ...defaultOptions, ...options };

    // Quill 인스턴스 생성
    globalQuillInstance = new Quill(editorElement, finalOptions);

    // 내용 변경 감지
    globalQuillInstance.on('text-change', () => {
        // 콜백 함수가 있으면 호출
        if (onTextChange && typeof onTextChange === 'function') {
            onTextChange(globalQuillInstance.root.innerHTML);
        }
    });

    // 클립보드 및 드래그앤드롭 핸들러 설정
    setupQuillHandlers(globalQuillInstance);

    return globalQuillInstance;
}

/**
 * 커스텀 이미지 핸들러
 */
function customImageHandler() {
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    input.setAttribute('accept', 'image/*');
    input.click();

    input.onchange = async () => {
        const file = input.files[0];
        if (file) {
            try {
                showQuillMessage('이미지 업로드 중...', 'loading');
                const url = await uploadImage(file);
                insertImageUrl(url);
                showQuillMessage('이미지가 성공적으로 업로드되었습니다!', 'success');
            } catch (error) {
                console.error('Image upload error:', error);
                showQuillMessage(`업로드 실패: ${error.message}`, 'error');
            }
        }
    };
}

/**
 * 파일을 Base64로 변환
 * @param {File} file - 변환할 파일
 * @returns {Promise<string>} Base64 문자열
 */
function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });
}

/**
 * 서버 이미지 업로드
 * @param {File} file - 업로드할 이미지 파일
 * @returns {Promise<string>} 업로드된 이미지 URL
 */
async function uploadImage(file) {
    try {
        // boardCode 확인
        if (!globalBoardCode) {
            throw new Error('boardCode가 설정되지 않았습니다. initQuillEditor 전에 setBoardCode(boardCode)를 호출하세요.');
        }

        // File을 Base64로 변환
        const base64Data = await fileToBase64(file);

        const response = await axios.post('/api/images/upload-clipboard', {
            image: base64Data
        }, {
            params: {
                boardCode: globalBoardCode
            },
            headers: {
                'Content-Type': 'application/json'
            }
        });

        // 서버 응답: { "success": true, "url": "..." }
        if (response.data.success) {
            return response.data.url;
        } else {
            throw new Error(response.data.error || 'Upload failed');
        }
    } catch (error) {
        if (error.response) {
            throw new Error(`Upload failed: ${error.response.status} - ${error.response.data.error || error.response.data}`);
        } else if (error.request) {
            throw new Error('Network error: No response from server');
        } else {
            throw new Error(`Request error: ${error.message}`);
        }
    }
}

/**
 * 현재 커서 위치에 이미지 URL 삽입
 * @param {string} url - 삽입할 이미지 URL
 * @param {Object} quillInstance - Quill 인스턴스 (선택사항, 없으면 globalQuillInstance 사용)
 */
function insertImageUrl(url, quillInstance = null) {
    const instance = quillInstance || globalQuillInstance;
    if (!instance) {
        console.error('Quill instance not available');
        return;
    }

    const range = instance.getSelection(true);
    const index = range ? range.index : instance.getLength();
    instance.insertEmbed(index, 'image', url, 'user');
    instance.setSelection(index + 1, 0);
}

/**
 * Quill 에디터 클립보드 및 드래그앤드롭 핸들러 설정
 * @param {Object} quillInstance - Quill 인스턴스
 */
function setupQuillHandlers(quillInstance) {
    setupClipboardHandlers(quillInstance);
    setupDragDropHandlers(quillInstance);
}

/**
 * 클립보드 핸들러 설정
 * @param {Object} quillInstance - Quill 인스턴스
 */
function setupClipboardHandlers(quillInstance) {
    // 1. Quill clipboard 모듈 설정 - 이미지만 차단
    try {
        const Delta = Quill.import('delta');
        const clipboard = quillInstance.getModule('clipboard');

        // HTML에 포함된 이미지 태그만 무시하도록 매처 추가
        clipboard.addMatcher('IMG', (node, delta) => {
            // HTML에 포함된 <img> 태그는 무시 (빈 Delta 반환)
            return new Delta();
        });

    } catch (e) {
        console.log('Clipboard matcher setup failed:', e);
    }

    // 2. paste 이벤트 핸들러 - 실제 이미지 파일만 가로챔
    quillInstance.root.addEventListener('paste', async (e) => {
        const clipboardData = e.clipboardData || window.clipboardData;
        if (!clipboardData || !clipboardData.items) return;

        const items = [...clipboardData.items];

        // 실제 이미지 파일이 있는지 확인 (스크린샷, 파일 복사 등)
        const hasImageFile = items.some(item =>
            item.kind === 'file' && item.type.startsWith('image/')
        );

        // 이미지 파일이 있으면 우리가 직접 처리
        if (hasImageFile) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();

            const imageItems = items.filter(item =>
                item.kind === 'file' && item.type.startsWith('image/')
            );

            for (const item of imageItems) {
                const file = item.getAsFile();
                if (file) {
                    try {
                        showQuillMessage('이미지를 편집기에 삽입하는 중...', 'loading');
                        const dataUrl = await fileToBase64(file);
                        insertImageUrl(dataUrl, quillInstance);
                        showQuillMessage('이미지가 삽입되었습니다. 저장 시 서버에 업로드됩니다.', 'success');
                    } catch (error) {
                        console.error('Image insert error:', error);
                        showQuillMessage(`이미지 삽입 실패: ${error.message}`, 'error');
                    }
                }
            }
            return false;
        }

        // 이미지 파일이 없으면 Quill의 기본 동작에 맡김
        // (텍스트, HTML, 표 등은 Quill이 알아서 처리)
    }, true); // capture phase에서 실행
}

/**
 * 드래그앤드롭 핸들러 설정
 * @param {Object} quillInstance - Quill 인스턴스
 */
function setupDragDropHandlers(quillInstance) {
    quillInstance.root.addEventListener('drop', async (e) => {
        e.preventDefault();
        const files = [...(e.dataTransfer?.files || [])].filter(file =>
            file.type.startsWith('image/')
        );

        if (files.length === 0) return;

        // 드롭 지점으로 커서 이동
        const selection = quillInstance.getSelection();
        const dropPos = selection ? selection.index : quillInstance.getLength();
        quillInstance.setSelection(dropPos, 0);

        for (const file of files) {
            try {
                showQuillMessage('이미지를 편집기에 삽입하는 중...', 'loading');
                const dataUrl = await fileToBase64(file);
                insertImageUrl(dataUrl, quillInstance);
                showQuillMessage('이미지가 삽입되었습니다. 저장 시 서버에 업로드됩니다.', 'success');
            } catch (error) {
                console.error('Image insert error:', error);
                showQuillMessage(`이미지 삽입 실패: ${error.message}`, 'error');
            }
        }
    });

    quillInstance.root.addEventListener('dragover', (e) => {
        e.preventDefault();
    });
}

/**
 * Quill 관련 상태 메시지 표시
 * 이 함수는 각 페이지에서 구현되어야 함
 * @param {string} text - 메시지 텍스트
 * @param {string} type - 메시지 타입 (success, error, loading)
 */
function showQuillMessage(text, type = 'success') {
    // 전역 메시지 표시 함수가 있으면 사용
    if (typeof window.showMessage === 'function') {
        window.showMessage(text, type);
    } else {
        // 기본 alert 사용
        console.log(`[${type.toUpperCase()}] ${text}`);
        if (type === 'error') {
            alert(`오류: ${text}`);
        }
    }
}

/**
 * 게시판 코드 설정
 * 각 페이지에서 Quill 에디터 초기화 전에 이 함수를 호출하여 boardCode를 설정해야 합니다.
 * @param {string} boardCode - 게시판 코드 (예: 'gosi', 'gongji', 'bodo')
 */
function setBoardCode(boardCode) {
    globalBoardCode = boardCode;
    console.log(`Board code set to: ${boardCode}`);
}

/**
 * 게시판 코드 반환
 * @returns {string} 현재 게시판 코드
 */
function getBoardCode() {
    return globalBoardCode;
}

/**
 * Quill 인스턴스 반환
 * @returns {Object} 현재 Quill 인스턴스
 */
function getQuillInstance() {
    return globalQuillInstance;
}

/**
 * Quill 에디터 내용 가져오기
 * @returns {string} HTML 내용
 */
function getQuillContent() {
    return globalQuillInstance ? globalQuillInstance.root.innerHTML : '';
}

/**
 * Quill 에디터 내용 설정
 * @param {string} content - 설정할 HTML 내용
 */
function setQuillContent(content) {
    if (globalQuillInstance) {
        globalQuillInstance.root.innerHTML = content;
    }
}

/**
 * 저장 전 content 처리 - data URL 이미지를 서버 URL로 변환
 * @param {string} content - 처리할 HTML content
 * @returns {Promise<string>} 처리된 HTML content
 */
async function processContentBeforeSave(content) {
    if (!content) return content;

    // 임시 div 생성하여 HTML 파싱
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = content;

    // data URL 이미지들 찾기
    const dataUrlImages = tempDiv.querySelectorAll('img[src^="data:"]');

    if (dataUrlImages.length === 0) {
        return content; // data URL 이미지가 없으면 그대로 반환
    }

    console.log(`저장 전 처리: ${dataUrlImages.length}개의 data URL 이미지를 서버에 업로드합니다.`);

    // 각 data URL 이미지를 순차적으로 업로드
    for (let i = 0; i < dataUrlImages.length; i++) {
        const img = dataUrlImages[i];
        const dataUrl = img.src;

        try {
            console.log(`이미지 ${i + 1}/${dataUrlImages.length} 업로드 중...`);

            // data URL을 Blob으로 변환
            const response = await fetch(dataUrl);
            const blob = await response.blob();

            // Blob을 File 객체로 변환
            const file = new File([blob], `clipboard-image-${Date.now()}-${i}.${blob.type.split('/')[1]}`, {
                type: blob.type
            });

            // 서버에 업로드
            const serverUrl = await uploadImage(file);

            // img src를 서버 URL로 변경
            img.src = serverUrl;

            console.log(`이미지 ${i + 1} 업로드 완료: ${serverUrl}`);

        } catch (error) {
            console.error(`이미지 ${i + 1} 업로드 실패:`, error);
            // 업로드 실패한 이미지는 제거
            img.remove();
        }
    }

    // 처리된 HTML 반환
    return tempDiv.innerHTML;
}

/**
 * data URL에서 파일 확장자 추출
 * @param {string} dataUrl - data URL
 * @returns {string} 파일 확장자
 */
function getExtensionFromDataUrl(dataUrl) {
    const matches = dataUrl.match(/data:image\/([^;]+)/);
    if (matches && matches[1]) {
        return matches[1]; // png, jpeg, gif 등
    }
    return 'png'; // 기본값
}

