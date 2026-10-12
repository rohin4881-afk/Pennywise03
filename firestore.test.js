const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write user profile", async () => {
  const unauthed = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthed.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthed.collection("users").doc(ALICE_UID).set({ uid: ALICE_UID }));
});

test("Authenticated user: can create own profile, cannot create others profile", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).set({ uid: ALICE_UID, displayName: "Alice" }));
  await assertFails(alice.collection("users").doc(BOB_UID).set({ uid: BOB_UID, displayName: "Bob" }));
});

test("Transactions: owner can create transaction, other user cannot read it", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(alice.collection("users").doc(ALICE_UID).collection("transactions").add({
    userId: ALICE_UID,
    amount: 150.0,
    type: "EXPENSE",
    category: "Food",
    date: Date.now()
  }));

  await assertFails(bob.collection("users").doc(ALICE_UID).collection("transactions").get());
});
