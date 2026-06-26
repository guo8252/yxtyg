const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto('http://localhost:10011/login');
  await page.fill('input[placeholder="请输入账号"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'wrong');
  await page.click('button:has-text("登录")');
  await page.waitForTimeout(1000);
  const html = await page.content();
  const idx = html.indexOf('账号或密码错误');
  console.log('idx', idx);
  console.log(html.substring(idx - 200, idx + 100));
  await browser.close();
})();
