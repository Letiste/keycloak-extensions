const { test, expect } = require('@playwright/test');
const sinon = require('sinon');
const { goToRezoleoRealmAsAdmin } = require('../helpers/e2e.helpers.js');
const { startServer, closeServer } = require('../server/server.js');

test.describe('edit event', () => {
  test.describe.configure({ mode: 'serial' });
  const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
  let spyController;

  test.beforeAll(async () => {
    spyController = sinon.spy();
    await startServer(spyController);
  });

  test.afterAll(async () => {
    await closeServer();
  });

  test.beforeEach(() => {
    spyController.resetHistory();
  });

  test('as an admin, saving user data should send an edit event', async ({ page }) => {
    await goToRezoleoRealmAsAdmin(page);

    await page.getByRole('link', { name: 'Users' }).click();
    await page.getByRole('link', { name: 'toto' }).click();
    await page.getByTestId('save-user').click();

    await delay(1_000);

    const reqBody = spyController.lastCall?.args[0]?.body;
    expect('firstname' in reqBody).toBeTruthy();
    expect('lastname' in reqBody).toBeTruthy();
    expect('email' in reqBody).toBeTruthy();
    expect('sso_id' in reqBody).toBeTruthy();
  });

  test('as an admin, saving user data attributes should send an edit event with room', async ({ page }) => {
    await goToRezoleoRealmAsAdmin(page);

    await page.getByRole('link', { name: 'Users' }).click();
    await page.getByRole('link', { name: 'toto' }).click();
    await page.getByTestId('attributes').click();
    await page.getByTestId('save-attributes').click();

    await delay(1_000);

    const reqBody = spyController.lastCall?.args[0]?.body;
    expect('firstname' in reqBody).toBeTruthy();
    expect('lastname' in reqBody).toBeTruthy();
    expect('email' in reqBody).toBeTruthy();
    expect('sso_id' in reqBody).toBeTruthy();
    expect('room' in reqBody).toBeTruthy();
  });

  test('as a user, changing my attributes from my profile should send an edit event', async ({ page }) => {
    // values for the user must be different from the previous one to send an update event
    const uniqueValue = Math.random()
      .toString(36)
      .replace(/[^a-z]+/g, '')
      .substring(0, 5);

    await page.goto('http://localhost:8080/realms/rezoleo/account/#/');
    await page.getByRole('button', { name: 'Sign in' }).click();
    await page.getByLabel('Username or email').fill('toto');
    await page.getByLabel('Password').fill('toto');
    await page.getByRole('button', { name: 'Sign In' }).click();
    await page.locator('#page-layout-default-nav a').filter({ hasText: 'Personal info' }).click();
    // wait for response to fill the inputs, otherwise they will be appended after the filled inputs by playwright
    // causing the inputs to be invalid, and preventing to save
    await page.waitForResponse('http://localhost:8080/realms/rezoleo/account/');
    await page.getByLabel('Email').fill(`${uniqueValue}@toto.com`);
    await page.getByLabel('First name').fill(uniqueValue);
    await page.getByLabel('Last name').fill(uniqueValue);
    await page.getByRole('button', { name: 'Save' }).click();

    await delay(1_000);

    const reqBody = spyController.lastCall?.args[0]?.body;
    expect('firstname' in reqBody).toBeTruthy();
    expect('lastname' in reqBody).toBeTruthy();
    expect('email' in reqBody).toBeTruthy();
    expect('sso_id' in reqBody).toBeTruthy();
  });
});
