const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:10011';
const SCREENSHOT_DIR = path.join(__dirname, 'screenshots');

if (!fs.existsSync(SCREENSHOT_DIR)) {
  fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });
}

async function screenshot(page, name) {
  const file = path.join(SCREENSHOT_DIR, `${name}.png`);
  await page.screenshot({ path: file, fullPage: true });
  console.log(`Screenshot: ${file}`);
}

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto(`${BASE_URL}/login`);
  await page.fill('input[placeholder="请输入账号"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'admin123');
  await page.click('button:has-text("登录")');
  await page.waitForTimeout(3000);
  await page.click('text=需求管理');
  await page.waitForTimeout(2000);
  await page.click('button:has-text("新增需求")');
  await page.waitForTimeout(2000);
  await screenshot(page, 'debug-add-requirement');
  const html = await page.content();
  console.log('Contains 新增需求:', html.includes('新增需求'));
  await browser.close();
})();
