const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto('http://localhost:10011/login');
  await page.fill('input[placeholder="请输入账号"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'wrong');
  await page.click('button:has-text("登录")');
  await page.waitForTimeout(500);
  const html = await page.content();
  console.log('has 账号或密码错误:', html.includes('账号或密码错误'));
  await page.waitForTimeout(1500);
  const html2 = await page.content();
  console.log('after 2s has 账号或密码错误:', html2.includes('账号或密码错误'));
  await browser.close();
})();
