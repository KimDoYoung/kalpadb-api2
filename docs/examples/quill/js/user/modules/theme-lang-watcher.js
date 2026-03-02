/**
 * Theme and Language Watcher
 * darkMode와 lang 변경 시 localStorage에 저장
 * x-init에서 $watch를 통해 사용
 */

export function setupThemeLangWatchers(instance) {
    // darkMode 변경 감시
    instance.$watch('darkMode', (value) => {
        localStorage.setItem('darkMode', value);
    });

    // lang 변경 감시
    instance.$watch('lang', (value) => {
        localStorage.setItem('lang', value);
    });
}
