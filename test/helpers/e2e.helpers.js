exports.goToRezoleoRealmAsAdmin = async function (page) {
  await page.goto('http://localhost:8080/');
  await page.getByRole('link', { name: 'Administration Console' }).click();
  await page.getByLabel('Username or email').fill('admin');
  await page.getByLabel('Username or email').press('Tab');
  await page.getByLabel('Password').fill('admin');
  await page.getByLabel('Password').press('Enter');
  await page.getByTestId('realmSelectorToggle').click();
  await page.getByRole('menuitem', { name: 'rezoleo' }).click();
};
