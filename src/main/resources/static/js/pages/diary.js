/**
 * 일기 페이지 JavaScript
 */

onDomReady(() => {
  // 페이지 초기화
  initDiaryPage();
});

function initDiaryPage() {
  console.log('일기 페이지 초기화됨');

  // 일기 목록 항목 클릭
  const diaryItems = document.querySelectorAll('.diary-item');
  diaryItems.forEach(item => {
    item.addEventListener('click', handleDiaryItemClick);
  });

  // 폼 제출
  const diaryForm = document.querySelector('form');
  if (diaryForm) {
    diaryForm.addEventListener('submit', handleDiaryFormSubmit);
  }
}

function handleDiaryItemClick(e) {
  const item = e.currentTarget;
  const link = item.querySelector('a');
  if (link) {
    window.location.href = link.href;
  }
}

async function handleDiaryFormSubmit(e) {
  e.preventDefault();

  const form = e.target;
  const formData = new FormData(form);
  const data = Object.fromEntries(formData);

  try {
    setLoading(true);
    const response = await apiCall(form.action, {
      method: form.method || 'POST',
      body: JSON.stringify(data),
    });

    if (response.ok) {
      showMessage('일기가 저장되었습니다.', 'success');
      setTimeout(() => {
        window.location.href = '/diary/list';
      }, 1000);
    } else {
      showMessage('저장에 실패했습니다.', 'error');
    }
  } catch (error) {
    console.error('오류:', error);
    showMessage('오류가 발생했습니다.', 'error');
  } finally {
    setLoading(false);
  }
}
