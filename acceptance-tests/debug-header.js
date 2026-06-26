const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  await page.goto('http://localhost:10011/login');
  await page.fill('input[placeholder="请输入账号"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'admin123');
  await page.click('button:has-text("登录")');
  await page.waitForTimeout(3000);
  const html = await page.content();
  console.log('has user-menu class:', html.includes('user-menu'));
  console.log('has 退出登录:', html.includes('退出登录'));
  const header = await page.$eval('.el-header', el => el.outerHTML).catch(e => 'not found');
  console.log(header.substring(0, 1500));
  await page.screenshot({ path: 'screenshots/header-debug.png', fullPage: true });
  await browser.close();
})();
