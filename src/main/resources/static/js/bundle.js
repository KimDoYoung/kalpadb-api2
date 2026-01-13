const fs = require('fs');
const path = require('path');

const jsDir = path.dirname(__filename);
const commonJsDir = path.join(jsDir, 'common');
const pagesJsDir = path.join(jsDir, 'pages');

function bundleJs() {
  try {
    let bundledJs = '';

    // 1. common/common.js 읽기
    if (fs.existsSync(path.join(commonJsDir, 'common.js'))) {
      bundledJs += fs.readFileSync(path.join(commonJsDir, 'common.js'), 'utf-8');
      bundledJs += '\n\n';
    }

    // 2. pages/*.js 읽기 및 합치기
    if (fs.existsSync(pagesJsDir)) {
      const pageFiles = fs.readdirSync(pagesJsDir).filter(f => f.endsWith('.js'));
      pageFiles.sort(); // 알파벳 순서로 정렬

      pageFiles.forEach(file => {
        const content = fs.readFileSync(path.join(pagesJsDir, file), 'utf-8');
        bundledJs += `/* ===== ${file} ===== */\n`;
        bundledJs += content;
        bundledJs += '\n\n';
      });
    }

    // 3. kalpadb.js로 저장
    fs.writeFileSync(path.join(jsDir, 'kalpadb.js'), bundledJs);
    console.log('✓ JS 번들링 완료:', path.join(jsDir, 'kalpadb.js'));
  } catch (error) {
    console.error('✗ JS 번들링 오류:', error.message);
    process.exit(1);
  }
}

bundleJs();
