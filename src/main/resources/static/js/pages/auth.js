/**
 * 인증 페이지 JavaScript (로그인)
 */

onDomReady(() => {
  // 페이지 초기화
  initAuthPage();
});

function initAuthPage() {
  console.log('인증 페이지 초기화됨');

  // 로그인 폼 제출
  const loginForm = document.querySelector('#loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', handleLoginSubmit);
  }

  // 숫자 패드 초기화
  initNumericPad();
}

function initNumericPad() {
  const numericPad = document.querySelector('.numeric-pad');
  if (!numericPad) return;

  const passwordInput = document.querySelector('input[name="password"]');
  if (!passwordInput) return;

  // 숫자 버튼 이벤트
  document.querySelectorAll('.numeric-pad button[data-number]').forEach(btn => {
    btn.addEventListener('click', () => {
      passwordInput.value += btn.dataset.number;
      passwordInput.dispatchEvent(new Event('input'));
    });
  });

  // 삭제 버튼
  const deleteBtn = document.querySelector('.numeric-pad .delete-btn');
  if (deleteBtn) {
    deleteBtn.addEventListener('click', () => {
      passwordInput.value = passwordInput.value.slice(0, -1);
      passwordInput.dispatchEvent(new Event('input'));
    });
  }

  // 초기화 버튼
  const clearBtn = document.querySelector('.numeric-pad .clear-btn');
  if (clearBtn) {
    clearBtn.addEventListener('click', () => {
      passwordInput.value = '';
      passwordInput.dispatchEvent(new Event('input'));
    });
  }
}

async function handleLoginSubmit(e) {
  e.preventDefault();

  const form = e.target;
  const formData = new FormData(form);
  const data = Object.fromEntries(formData);

  // 유효성 검사
  if (!data.username || !data.password) {
    showMessage('사용자명과 비밀번호를 입력하세요.', 'error');
    return;
  }

  try {
    setLoading(true);
    const response = await apiCall(form.action || '/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });

    if (response.ok) {
      showMessage('로그인되었습니다.', 'success');
      setTimeout(() => {
        window.location.href = (typeof APP_CTX !== 'undefined' ? APP_CTX : '') + '/';
      }, 1000);
    } else {
      const error = await response.text();
      showMessage(error || '로그인에 실패했습니다.', 'error');
    }
  } catch (error) {
    console.error('오류:', error);
    showMessage('오류가 발생했습니다.', 'error');
  } finally {
    setLoading(false);
  }
}
