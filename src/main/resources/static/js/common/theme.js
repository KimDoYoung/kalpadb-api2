// DaisyUI Theme Manager
document.addEventListener('DOMContentLoaded', () => {
  const htmlElement = document.documentElement;
  const themes = ['light', 'dark', 'corporate', 'dracula']; // tailwind.config에 정의된 테마

  // 저장된 theme 로드 (기본값: light)
  const savedTheme = localStorage.getItem('theme') || 'light';
  htmlElement.setAttribute('data-theme', savedTheme);

  // Theme 토글 함수 (public)
  window.toggleTheme = function() {
    const currentTheme = htmlElement.getAttribute('data-theme');
    const currentIndex = themes.indexOf(currentTheme);
    const newIndex = (currentIndex + 1) % themes.length;
    const newTheme = themes[newIndex];

    htmlElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
  };

  // Alpine.js 통합
  document.addEventListener('alpine:init', () => {
    Alpine.data('themeManager', () => ({
      currentTheme: savedTheme,
      themes: themes,

      toggle() {
        const index = this.themes.indexOf(this.currentTheme);
        this.currentTheme = this.themes[(index + 1) % this.themes.length];
        htmlElement.setAttribute('data-theme', this.currentTheme);
        localStorage.setItem('theme', this.currentTheme);
      },

      setTheme(theme) {
        if (this.themes.includes(theme)) {
          this.currentTheme = theme;
          htmlElement.setAttribute('data-theme', theme);
          localStorage.setItem('theme', theme);
        }
      }
    }));
  });
});
