/**
 * 홈 페이지 JavaScript
 */

onDomReady(() => {
  // 페이지 초기화
  initHomePage();
});

function initHomePage() {
  console.log('홈 페이지 초기화됨');

  // 이벤트 리스너 등록
  const homeCards = document.querySelectorAll('.home-card');
  homeCards.forEach(card => {
    card.addEventListener('click', handleCardClick);
  });
}

function handleCardClick(e) {
  const card = e.currentTarget;
  const link = card.querySelector('a');
  if (link) {
    window.location.href = link.href;
  }
}
