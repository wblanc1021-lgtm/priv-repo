const { test, before, after } = require('node:test');
const assert = require('node:assert');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const fs = require('fs');

const PROJECT_ID = 'demo-no-project';
const DATABASE_ID = 'ai-studio-android-frameshi-e9accc72-abd9-4e82-be37-f2002a336381';

let testEnv;

before(async () => {
  const rules = fs.readFileSync('firestore.rules', 'utf8');
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: '127.0.0.1',
      port: 8085,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test('Unauthenticated user cannot read user profile', async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore({ databaseId: DATABASE_ID });
  await assertFails(unauthedDb.collection('users').doc('user_123').get());
});

test('Authenticated user can create their own profile', async () => {
  const aliceDb = testEnv.authenticatedContext('alice_1').firestore({ databaseId: DATABASE_ID });
  const docRef = aliceDb.collection('users').doc('alice_1');
  await assertSucceeds(
    docRef.set({
      userId: 'alice_1',
      displayName: 'Alice Architect',
      email: 'alice@example.com',
      credits: 100,
      tier: 'pro',
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test('User cannot access or modify another user profile', async () => {
  const bobDb = testEnv.authenticatedContext('bob_2').firestore({ databaseId: DATABASE_ID });
  await assertFails(bobDb.collection('users').doc('alice_1').get());
});

test('User can create and read their recast projects', async () => {
  const aliceDb = testEnv.authenticatedContext('alice_1').firestore({ databaseId: DATABASE_ID });
  const projRef = aliceDb.collection('users').doc('alice_1').collection('projects').doc('proj_99');
  await assertSucceeds(
    projRef.set({
      id: 'proj_99',
      userId: 'alice_1',
      title: 'Cinematic Recast Test',
      sourceVideoName: 'street_walk.mp4',
      characterStrength: 85,
      qualityPreset: 'high',
      status: 'draft',
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(projRef.get());
});

test('Another user cannot read or update Alice recast projects', async () => {
  const bobDb = testEnv.authenticatedContext('bob_2').firestore({ databaseId: DATABASE_ID });
  const projRef = bobDb.collection('users').doc('alice_1').collection('projects').doc('proj_99');
  await assertFails(projRef.get());
});
