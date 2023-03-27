const { test, expect } = require('@playwright/test');
const { goToRezoleoRealmAsAdmin } = require('../helpers/e2e.helpers.js');

test.describe('correct room', () => {
    test.describe.configure({ mode: 'serial' });
    const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

    test('as an admin, room should be correctly formatted', async ({ page }) => {
        await goToRezoleoRealmAsAdmin(page);

        await page.getByRole('link', { name: 'Users' }).click();
        await page.getByRole('link', { name: 'toto' }).click();
        await page.getByTestId('attributes').click();
        await page.getByTestId('attributes[0].value').fill('f123');
        await page.getByTestId('save-attributes').click();

        await page.waitForResponse(RegExp("http://localhost:8080/admin/realms/rezoleo/users/.*"))

        await page.getByRole('button', { name: 'Close alert: Error updating group The room does not have the correct format. Examples of correct format are F123a or DF2' }).click();
    });

    test('as an admin, room should be unique', async ({ page }) => {
        await goToRezoleoRealmAsAdmin(page);

        await page.getByRole('link', { name: 'Users' }).click();
        await page.getByRole('link', { name: 'toto' }).click();
        await page.getByTestId('attributes').click();
        await page.getByTestId('attributes[0].value').fill('A123');
        await page.getByTestId('save-attributes').click();

        await page.waitForResponse(RegExp("http://localhost:8080/admin/realms/rezoleo/users/.*"))

        await page.getByRole('button', { name: 'Close alert: Error updating group this room already belongs to another user' }).click();
    });

    test('as a user registering, room should be correctly formatted', async ({page}) => {
        await page.goto('http://localhost:8080/realms/rezoleo/account/#/');
        await page.getByRole('button', { name: 'Sign in' }).click();
        await page.getByRole('link', { name: 'Register' }).click();
        await page.getByLabel('Room').fill('f1234');
        await page.getByRole('button', { name: 'Register' }).click();

        await expect(page.getByText('The room does not have the correct format. Examples of correct format are F123a or DF2')).toBeVisible();
    })

    test('as a user registering, room should unique', async ({page}) => {
        await page.goto('http://localhost:8080/realms/rezoleo/account/#/');
        await page.getByRole('button', { name: 'Sign in' }).click();
        await page.getByRole('link', { name: 'Register' }).click();
        await page.getByLabel('Room').fill('F123');
        await page.getByRole('button', { name: 'Register' }).click();

        await expect(page.getByText('This room already belongs to another user')).toBeVisible();
    })
});