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
  return file;
}

async function waitForToast(page, text, timeout = 3000) {
  try {
    await page.waitForSelector(`.el-message:has-text("${text}"), .el-notification:has-text("${text}")`, { timeout });
    return true;
  } catch (e) {
    const html = await page.content();
    return html.includes(text);
  }
}

async function login(page, username, password) {
  await page.goto(`${BASE_URL}/login`);
  await page.waitForSelector('input[placeholder="请输入账号"]', { timeout: 10000 });
  await page.fill('input[placeholder="请输入账号"]', username);
  await page.fill('input[placeholder="请输入密码"]', password);
  await page.click('button:has-text("登录")');
  await page.waitForTimeout(2000);
}

async function run() {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1280, height: 900 } });
  const page = await context.newPage();
  const results = [];

  try {
    // Test 1: Invalid login should show error
    await login(page, 'admin', 'wrongpassword');
    const hasError = await waitForToast(page, '账号或密码错误') || await waitForToast(page, '用户名或密码错误') || await waitForToast(page, '失败');
    results.push({ test: 'Invalid login shows error', pass: hasError });
    await screenshot(page, 'login-error');

    // Test 2: Admin login and see user management menu
    await login(page, 'admin', 'admin123');
    await page.waitForSelector('.el-menu', { timeout: 10000 });
    const hasUserMenu = await page.isVisible('text=用户管理');
    const hasRequirementMenu = await page.isVisible('text=需求管理');
    results.push({ test: 'Admin sees user management menu', pass: hasUserMenu });
    results.push({ test: 'Admin sees requirement management menu', pass: hasRequirementMenu });
    await screenshot(page, 'admin-dashboard');

    // Test 3: Navigate to requirement page and create requirement
    await page.click('text=需求管理');
    await page.waitForTimeout(1500);
    await screenshot(page, 'requirement-list-admin');

    await page.click('button:has-text("新增需求")');
    await page.waitForTimeout(1500);
    await page.fill('input[placeholder="请输入需求名称"]', '验收测试需求-' + Date.now());
    await page.fill('textarea[placeholder="请输入需求描述"]', '这是浏览器验收测试创建的需求');
    await page.click('.el-select:has(input[placeholder="请选择产品经理"])');
    await page.waitForTimeout(500);
    await page.click('.el-select-dropdown__list li:first-child');
    await page.waitForTimeout(500);
    await page.fill('input[placeholder="请输入归属系统"]', '测试系统');
    await page.fill('input[placeholder="请输入初核工作量"]', '10');
    await page.fill('input[placeholder="请输入初核金额"]', '5000');
    await screenshot(page, 'requirement-form-admin');
    await page.click('button:has-text("保存")');
    await page.waitForTimeout(2500);
    const createSuccess = await waitForToast(page, '保存成功') || await waitForToast(page, '操作成功');
    results.push({ test: 'Admin creates requirement', pass: createSuccess });
    await screenshot(page, 'requirement-list-after-create');

    // Test 4: Logout and login as product manager
    await page.click('.el-dropdown-link');
    await page.waitForTimeout(500);
    await page.click('text=退出登录');
    await page.waitForTimeout(1500);

    await login(page, 'pm1', 'admin123');
    await page.waitForSelector('.el-menu', { timeout: 10000 });
    const pmUserMenu = await page.isVisible('text=用户管理');
    results.push({ test: 'Product manager cannot see user management menu', pass: !pmUserMenu });
    await screenshot(page, 'pm-dashboard');

    // Test 5: Product manager sees own requirements
    await page.click('text=需求管理');
    await page.waitForTimeout(1500);
    await screenshot(page, 'requirement-list-pm');
    const hasRows = await page.isVisible('.el-table__row');
    results.push({ test: 'Product manager sees requirement list', pass: hasRows });

    // Test 6: Product manager fills final workload
    const fillBtn = await page.locator('.el-table__row:first-child').getByRole('button', { name: '填写' });
    if (await fillBtn.isVisible()) {
      await fillBtn.scrollIntoViewIfNeeded();
      await fillBtn.click({ force: true });
      await page.waitForTimeout(1000);
      await page.fill('.el-dialog__body input[placeholder="请输入"]', '8');
      await screenshot(page, 'fill-form-pm');
      await page.click('.el-dialog__footer button:has-text("确定")');
      await page.waitForTimeout(2500);
      const fillSuccess = await waitForToast(page, '填写成功') || await waitForToast(page, '操作成功');
      results.push({ test: 'Product manager fills final workload', pass: fillSuccess });
    } else {
      results.push({ test: 'Product manager fills final workload', pass: false, note: 'No fill button found' });
    }
    await screenshot(page, 'requirement-list-after-fill');

    // Test 7: Logout and login as dev admin to approve
    await page.click('.el-dropdown-link');
    await page.waitForTimeout(500);
    await page.click('text=退出登录');
    await page.waitForTimeout(1500);

    await login(page, 'dev1', 'admin123');
    await page.waitForSelector('.el-menu', { timeout: 10000 });
    await page.click('text=需求管理');
    await page.waitForTimeout(1500);
    await screenshot(page, 'requirement-list-dev');

    // Click approve on the first FILLED row
    const approveBtn = await page.locator('.el-table__row:first-child').getByRole('button', { name: '核定' });
    if (await approveBtn.isVisible()) {
      await approveBtn.scrollIntoViewIfNeeded();
      await approveBtn.click({ force: true });
      await page.waitForTimeout(1000);
      await page.click('.el-message-box__btns button:has-text("确定")');
      await page.waitForTimeout(2500);
      const approveSuccess = await waitForToast(page, '核定成功') || await waitForToast(page, '操作成功');
      results.push({ test: 'Dev admin approves requirement', pass: approveSuccess });
    } else {
      results.push({ test: 'Dev admin approves requirement', pass: false, note: 'No approve button found' });
    }
    await screenshot(page, 'requirement-list-after-approve');

    // Test 8: Urge function
    const urgeBtn = await page.locator('.el-table__row:first-child').getByRole('button', { name: '催办' });
    if (await urgeBtn.isVisible()) {
      await urgeBtn.scrollIntoViewIfNeeded();
      await urgeBtn.click({ force: true });
      await page.waitForTimeout(1000);
      await page.click('.el-message-box__btns button:has-text("确定")');
      await page.waitForTimeout(2500);
      const urgeSuccess = await waitForToast(page, '催办成功') || await waitForToast(page, '操作成功');
      results.push({ test: 'Dev admin urges requirement', pass: urgeSuccess });
    } else {
      results.push({ test: 'Dev admin urges requirement', pass: false, note: 'No urge button found' });
    }
    await screenshot(page, 'requirement-list-after-urge');

  } catch (e) {
    console.error('Test error:', e);
    results.push({ test: 'Overall test execution', pass: false, note: e.message });
  } finally {
    await screenshot(page, 'final-state');
    await browser.close();
  }

  // Print results
  console.log('\n=== Acceptance Test Results ===');
  let passed = 0;
  results.forEach(r => {
    const status = r.pass ? 'PASS' : 'FAIL';
    console.log(`[${status}] ${r.test}${r.note ? ' - ' + r.note : ''}`);
    if (r.pass) passed++;
  });
  console.log(`\nTotal: ${passed}/${results.length} passed`);
  process.exit(passed === results.length ? 0 : 1);
}

run().catch(e => {
  console.error(e);
  process.exit(1);
});
