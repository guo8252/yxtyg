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
  const count1 = await page.locator('.el-table__row:first-child button.el-button:has-text("填写")').count();
  console.log('button.el-button count', count1);
  const count2 = await page.locator('.el-table__row:first-child button:has-text("填写")').count();
  console.log('button count', count2);
  const count3 = await page.locator('.el-table__row:first-child').getByRole('button', { name: '填写' }).count();
  console.log('getByRole count', count3);
  await browser.close();
})();
