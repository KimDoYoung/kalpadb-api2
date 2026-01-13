const fs = require('fs');
const path = require('path');

const cssDir = path.dirname(__filename);
const commonCssDir = path.join(cssDir, 'common');
const pagesCssDir = path.join(cssDir, 'pages');

// 간단한 CSS minify 함수
function minifyCss(css) {
  return css
    .replace(/\/\*[\s\S]*?\*\//g, '') // 주석 제거
    .replace(/\s+/g, ' ') // 연속된 공백을 단일 공백으로
    .replace(/\s*([{};:,>+~])\s*/g, '$1') // 괄호 주변 공백 제거
    .trim();
}

function bundleCss() {
  try {
    let bundledCss = '';

    // 1. common/common.css 읽기
    if (fs.existsSync(path.join(commonCssDir, 'common.css'))) {
      bundledCss += fs.readFileSync(path.join(commonCssDir, 'common.css'), 'utf-8');
      bundledCss += '\n\n';
    }

    // 2. pages/*.css 읽기 및 합치기
    if (fs.existsSync(pagesCssDir)) {
      const pageFiles = fs.readdirSync(pagesCssDir).filter(f => f.endsWith('.css'));
      pageFiles.sort(); // 알파벳 순서로 정렬

      pageFiles.forEach(file => {
        const content = fs.readFileSync(path.join(pagesCssDir, file), 'utf-8');
        bundledCss += content;
        bundledCss += '\n\n';
      });
    }

    // 3. Minify 및 kalpadb.css로 저장
    const minifiedCss = minifyCss(bundledCss);
    fs.writeFileSync(path.join(cssDir, 'kalpadb.css'), minifiedCss);
    console.log('✓ CSS 번들링 완료:', path.join(cssDir, 'kalpadb.css'));
  } catch (error) {
    console.error('✗ CSS 번들링 오류:', error.message);
    process.exit(1);
  }
}

bundleCss();
