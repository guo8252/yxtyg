const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto('http://localhost:10011/login');
  await page.fill('input[placeholder="请输入账号"]', 'pm1');
  await page.fill('input[placeholder="请输入密码"]', 'admin123');
  await page.click('button:has-text("登录")');
  await page.waitForTimeout(3000);
  await page.click('text=需求管理');
  await page.waitForTimeout(2000);
  const buttons = await page.locator('.el-table__row:visible').first().locator('button, span').all();
  console.log('buttons count', buttons.length);
  for (const btn of buttons) {
    const tag = await btn.evaluate(el => el.tagName);
    const text = await btn.textContent();
    console.log(tag, text.trim());
  }
  await browser.close();
})();
