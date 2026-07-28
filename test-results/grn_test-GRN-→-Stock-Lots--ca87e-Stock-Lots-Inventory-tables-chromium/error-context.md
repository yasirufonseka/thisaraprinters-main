# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: grn_test.spec.js >> GRN → Stock Lots & Inventory Tables >> T3 – Submit GRN and verify it appears in Stock Lots & Inventory tables
- Location: tests\grn_test.spec.js:74:3

# Error details

```
Error: page.goto: net::ERR_CONNECTION_REFUSED at http://localhost:8080/inventory/management
Call log:
  - navigating to "http://localhost:8080/inventory/management", waiting until "domcontentloaded"

```

# Page snapshot

```yaml
- generic [ref=e3]:
  - generic [ref=e6]:
    - heading "This site can’t be reached" [level=1] [ref=e7]
    - paragraph [ref=e8]:
      - strong [ref=e9]: localhost
      - text: refused to connect.
    - generic [ref=e10]:
      - paragraph [ref=e11]: "Try:"
      - list [ref=e12]:
        - listitem [ref=e13]: Checking the connection
        - listitem [ref=e14]:
          - link "Checking the proxy and the firewall" [ref=e15] [cursor=pointer]:
            - /url: "#buttons"
    - generic [ref=e16]: ERR_CONNECTION_REFUSED
  - generic [ref=e17]:
    - button "Reload" [ref=e19] [cursor=pointer]
    - button "Details" [ref=e20] [cursor=pointer]
```

# Test source

```ts
  1   | // =============================================================
  2   | //  Thisara Printers – GRN Save Test
  3   | //  Tests that a submitted GRN is persisted in:
  4   | //    1. The "inventory" table  →  GRN tab table on the UI
  5   | //    2. The "stock_lots" table →  Material Inventory tab table on the UI
  6   | // =============================================================
  7   | // Run with:
  8   | //   npx playwright test tests/grn_test.spec.js --headed
  9   | // =============================================================
  10  | 
  11  | const { test, expect } = require('@playwright/test');
  12  | 
  13  | // ── Config ─────────────────────────────────────────────────────
  14  | const BASE_URL   = 'http://localhost:8080';
  15  | const GRN_PAGE   = `${BASE_URL}/inventory/management`;
  16  | const GRN_API    = `${BASE_URL}/inventory/api/grn/save-full`;
  17  | 
  18  | // ── Helpers ─────────────────────────────────────────────────────
  19  | /**
  20  |  * Wait for a SweetAlert2 popup and optionally confirm it.
  21  |  */
  22  | async function waitForSwal(page, { confirm = false } = {}) {
  23  |   const swal = page.locator('.swal2-popup');
  24  |   await swal.waitFor({ state: 'visible', timeout: 10_000 });
  25  |   const title = await page.locator('.swal2-title').innerText();
  26  |   const text  = await page.locator('.swal2-html-container').innerText().catch(() => '');
  27  |   console.log(`[SweetAlert] title="${title}" text="${text}"`);
  28  |   if (confirm) {
  29  |     await page.locator('.swal2-confirm').click();
  30  |     await swal.waitFor({ state: 'hidden', timeout: 5_000 });
  31  |   }
  32  |   return { title, text };
  33  | }
  34  | 
  35  | // ── Test Suite ──────────────────────────────────────────────────
  36  | test.describe('GRN → Stock Lots & Inventory Tables', () => {
  37  | 
  38  |   test.beforeEach(async ({ page }) => {
  39  |     // Go directly to the inventory management page (no login needed)
> 40  |     await page.goto(GRN_PAGE, { waitUntil: 'domcontentloaded' });
      |                ^ Error: page.goto: net::ERR_CONNECTION_REFUSED at http://localhost:8080/inventory/management
  41  |     await page.waitForLoadState('networkidle');
  42  |   });
  43  | 
  44  |   // ── TEST 1: Page loads correctly ──────────────────────────────
  45  |   test('T1 – Inventory Management page loads', async ({ page }) => {
  46  |     await expect(page).toHaveTitle(/Inventory Management/i);
  47  |     await expect(page.locator('h2')).toContainText('Inventory Management');
  48  | 
  49  |     // Both tabs should be visible
  50  |     await expect(page.locator('button.tab-btn').nth(0)).toContainText('Material Inventory');
  51  |     await expect(page.locator('button.tab-btn').nth(1)).toContainText('Goods Received Notes');
  52  | 
  53  |     console.log('✅ T1 PASSED – Page loaded successfully');
  54  |   });
  55  | 
  56  |   // ── TEST 2: GRN Modal opens ───────────────────────────────────
  57  |   test('T2 – GRN modal opens when "Receive Goods (GRN)" is clicked', async ({ page }) => {
  58  |     // Switch to GRN tab
  59  |     await page.locator('button.tab-btn').nth(1).click();
  60  |     await page.waitForTimeout(300);
  61  | 
  62  |     // Click the "Receive Goods (GRN)" button
  63  |     await page.locator('button.btn-gold').click();
  64  | 
  65  |     // Modal should appear
  66  |     const modal = page.locator('#grnModal');
  67  |     await expect(modal).toBeVisible({ timeout: 5_000 });
  68  |     await expect(modal.locator('.modal-title')).toContainText('Receive Goods');
  69  | 
  70  |     console.log('✅ T2 PASSED – GRN modal opened');
  71  |   });
  72  | 
  73  |   // ── TEST 3: Full GRN save via API → verify in both tables ─────
  74  |   test('T3 – Submit GRN and verify it appears in Stock Lots & Inventory tables', async ({ page }) => {
  75  | 
  76  |     // ----- Step 1: Read current row counts before submit -----
  77  |     // Stock Lots table (Material Inventory tab – active by default)
  78  |     const stockLotsTableBody = page.locator('#materialTable tbody');
  79  |     const stockLotsBefore = await stockLotsTableBody.locator('tr').count();
  80  |     console.log(`[Before] Stock Lots rows: ${stockLotsBefore}`);
  81  | 
  82  |     // Switch to GRN tab and count rows
  83  |     await page.locator('button.tab-btn').nth(1).click();
  84  |     await page.waitForTimeout(300);
  85  |     const grnTableBody = page.locator('#grnTable tbody');
  86  |     const grnRowsBefore = await grnTableBody.locator('tr').count();
  87  |     console.log(`[Before] GRN table rows: ${grnRowsBefore}`);
  88  | 
  89  |     // ----- Step 2: Get available supplier/material/user IDs from dropdowns -----
  90  |     await page.locator('button.btn-gold').click();
  91  |     const modal = page.locator('#grnModal');
  92  |     await expect(modal).toBeVisible({ timeout: 5_000 });
  93  | 
  94  |     // Grab the FIRST valid option from each select
  95  |     const supplierSelect  = page.locator('#grnSupplier');
  96  |     const materialSelect  = page.locator('#grnItem');
  97  |     const userSelect      = page.locator('#grnReceivedBy');
  98  | 
  99  |     const supplierOptions = await supplierSelect.locator('option:not([disabled])').all();
  100 |     const materialOptions = await materialSelect.locator('option:not([disabled])').all();
  101 |     const userOptions     = await userSelect.locator('option:not([disabled])').all();
  102 | 
  103 |     // Ensure there is data to pick from
  104 |     if (supplierOptions.length === 0) {
  105 |       console.warn('⚠️  No suppliers found in the dropdown – skipping T3');
  106 |       test.skip();
  107 |     }
  108 |     if (materialOptions.length === 0) {
  109 |       console.warn('⚠️  No materials found in the dropdown – skipping T3');
  110 |       test.skip();
  111 |     }
  112 |     if (userOptions.length === 0) {
  113 |       console.warn('⚠️  No users found in the dropdown – skipping T3');
  114 |       test.skip();
  115 |     }
  116 | 
  117 |     const supplierId = await supplierOptions[0].getAttribute('value');
  118 |     const materialId = await materialOptions[0].getAttribute('value');
  119 |     const userId     = await userOptions[0].getAttribute('value');
  120 |     const supplierName = await supplierOptions[0].innerText();
  121 |     const materialName = await materialOptions[0].innerText();
  122 | 
  123 |     console.log(`[GRN Data] Supplier: "${supplierName}" (${supplierId}), Material: "${materialName}" (${materialId}), User: ${userId}`);
  124 | 
  125 |     // ----- Step 3: Fill the GRN form -----
  126 |     const testInvoiceNo = `INV-TEST-${Date.now()}`;
  127 |     const testBatchNo   = `BATCH-TEST-${Date.now()}`;
  128 |     const testQty       = 50;
  129 |     const today         = new Date().toISOString().split('T')[0];
  130 | 
  131 |     await page.locator('#grnSupplierInvoice').fill(testInvoiceNo);
  132 |     await page.locator('#grnBatchNo').fill(testBatchNo);
  133 |     await supplierSelect.selectOption(supplierId);
  134 |     await materialSelect.selectOption(materialId);
  135 |     await page.locator('#grnQty').fill(String(testQty));
  136 |     await page.locator('#grnUnits').selectOption('Reams');
  137 |     await page.locator('#grnDate').fill(today);
  138 |     await userSelect.selectOption(userId);
  139 |     await page.locator('#grnNotes').fill('Playwright automated test GRN');
  140 | 
```